package org.eu.thedoc.zettelnotes.buttons.llm

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper
import java.io.File

class LlmProcessActivity : BaseActivity() {

    companion object {
        const val INTENT_EXTRA_TEXT = "intent-extra-text"
        const val INTENT_EXTRA_ACTION = "intent-extra-action"
        const val INTENT_EXTRA_RESULT = "intent-extra-result"
        const val ERROR_STRING = "intent-error"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var progressDialog: ProgressDialog

    private var sourceText: String? = null
    private lateinit var action: LlmAction
    private var launchedForResult = false
    private var outputBuffer = StringBuilder()

    private var engineWrapper: LlmEngineWrapper? = null
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var inferenceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        launchedForResult = callingPackage != null

        progressDialog = ProgressDialog(this).apply {
            setMessage("Starting...")
            setCancelable(true)
            setOnCancelListener {
                inferenceJob?.cancel()
                finishWithError("Cancelled")
            }
        }

        if (!prefs.getBoolean("prefs_enable", true)) {
            finishWithError("Plugin disabled")
            return
        }

        sourceText = intent.getStringExtra(INTENT_EXTRA_TEXT)
        val actionName = intent.getStringExtra(INTENT_EXTRA_ACTION)
        if (sourceText.isNullOrEmpty() || actionName == null) {
            finishWithError("Missing input")
            return
        }
        action = LlmAction.valueOf(actionName)

        val useRemote = prefs.getBoolean("prefs_use_remote", false)

        if (!useRemote && !hasEnoughRam()) {
            finishWithError("Device does not have enough RAM for on-device AI. Enable a remote server in settings instead.")
            return
        }

        progressDialog.show()

        if (useRemote) {
            runRemoteInference()
        } else {
            ensureModelThenRun()
        }
    }

    private fun hasEnoughRam(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val info = android.app.ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.totalMem > 3L * 1024 * 1024 * 1024
    }

    private fun getSelectedModel(): LlmModel {
        val name = prefs.getString("prefs_model", LlmModel.BALANCED.name)
        return try {
            LlmModel.valueOf(name ?: LlmModel.BALANCED.name)
        } catch (e: Exception) {
            LlmModel.BALANCED
        }
    }

    private fun getMaxTokens(): Int = prefs.getInt("prefs_max_tokens", 512)
    private fun getUseGpu(): Boolean = prefs.getBoolean("prefs_use_gpu", true)

    private fun getTemperature(): Float = try {
        prefs.getString("prefs_temperature", "0.7")?.toFloat() ?: 0.7f
    } catch (e: Exception) {
        0.7f
    }

    private fun isOnWifi(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun ensureModelThenRun() {
        val model = getSelectedModel()

        if (ModelManager.isDownloaded(this, model)) {
            progressDialog.setMessage("Loading model...")
            initializeAndRun(model)
            return
        }

        val wifiOnly = prefs.getBoolean("prefs_wifi_only_download", true)
        if (wifiOnly && !isOnWifi()) {
            finishWithError("Model not downloaded. Connect to Wi-Fi or disable \"Download on Wi-Fi only\" in settings.")
            return
        }

        progressDialog.setMessage("Downloading model (${model.approxBytes / 1024 / 1024} MB)...")
        progressDialog.isIndeterminate = false
        progressDialog.max = 100

        ModelManager.download(this, model, object : ModelManager.ProgressListener {
            override fun onProgress(percent: Int) {
                runOnUiThread { progressDialog.progress = percent }
            }

            override fun onComplete(modelFile: File) {
                runOnUiThread {
                    progressDialog.isIndeterminate = true
                    progressDialog.setMessage("Loading model...")
                    initializeAndRun(model)
                }
            }

            override fun onError(e: Exception) {
                runOnUiThread { finishWithError("Model download failed: ${e.message}") }
            }
        })
    }

    private fun initializeAndRun(model: LlmModel) {
        activityScope.launch {
            try {
                val modelFile = ModelManager.getModelFile(this@LlmProcessActivity, model)
                val wrapper = withContext(Dispatchers.IO) {
                    LlmEngineWrapper(modelFile.absolutePath, getUseGpu(), getMaxTokens())
                        .also { it.startConversation() }
                }
                engineWrapper = wrapper
                runInference()
            } catch (e: Exception) {
                finishWithError("Model load failed: ${e.message}")
            }
        }
    }

    private fun runInference() {
        val engine = engineWrapper ?: return
        outputBuffer = StringBuilder()
        progressDialog.setMessage("Generating...")

        val prompt = action.buildPrompt(sourceText ?: "")

        inferenceJob?.cancel()
        inferenceJob = engine.sendMessage(prompt)
            .onEach { chunk -> outputBuffer.append(chunk) }
            .onCompletion { cause -> if (cause == null) finishWithSuccess() }
            .catch { e -> finishWithError("Inference failed: ${e.message}") }
            .launchIn(activityScope)
    }

    private fun runRemoteInference() {
        val url = prefs.getString("prefs_remote_url", "") ?: ""
        val apiKey = prefs.getString("prefs_remote_api_key", "") ?: ""

        if (url.isEmpty()) {
            finishWithError("Remote endpoint URL not configured in settings")
            return
        }

        progressDialog.setMessage("Contacting server...")
        val prompt = action.buildPrompt(sourceText ?: "")

        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RemoteLlmClient.chat(url, apiKey, prompt, getMaxTokens(), getTemperature())
                }
                outputBuffer.append(response)
                finishWithSuccess()
            } catch (e: Exception) {
                finishWithError("Remote request failed: ${e.message}")
            }
        }
    }

    private fun finishWithSuccess() {
        progressDialog.dismiss()
        val result = outputBuffer.toString()

        AlertDialog.Builder(this)
            .setTitle(action.label)
            .setMessage(result)
            .setPositiveButton("Copy") { _, _ ->
                if (launchedForResult) {
                    setResult(RESULT_OK, Intent().putExtra(INTENT_EXTRA_RESULT, result))
                } else {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("LLM output", result))

                    ToastsHelper.showToast(this, "Copied to clipboard")
                }
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun finishWithError(message: String) {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }

        println(message = message)

        ToastsHelper.showToast(this, "Error: $message")
        if (launchedForResult) {
            setResult(RESULT_CANCELED, Intent().putExtra(ERROR_STRING, message))
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        inferenceJob?.cancel()
        engineWrapper?.close()
        activityScope.cancel()
    }
}