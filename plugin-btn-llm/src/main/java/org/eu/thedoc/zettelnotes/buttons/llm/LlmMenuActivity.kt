package org.eu.thedoc.zettelnotes.buttons.llm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity

class LlmMenuActivity : BaseActivity() {

    companion object {
        const val INTENT_EXTRA_TEXT = "intent-extra-text"
        const val INTENT_EXTRA_RESULT = "intent-extra-result"
        const val ERROR_STRING = "intent-error"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent.getStringExtra(INTENT_EXTRA_TEXT)
        if (text.isNullOrEmpty()) {
            setResult(RESULT_CANCELED, Intent().putExtra(ERROR_STRING, "No text selected"))
            finish()
            return
        }

        val actions = LlmAction.entries.toTypedArray()
        val labels = actions.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("AI Actions")
            .setItems(labels) { _, which ->
                val processIntent = Intent("org.eu.thedoc.zettelnotes.intent.buttons.llm.process").apply {
                    putExtra(LlmProcessActivity.INTENT_EXTRA_TEXT, text)
                    putExtra(LlmProcessActivity.INTENT_EXTRA_ACTION, actions[which].name)
                }
                startActivityForResult(processIntent, 100)
            }
            .setOnCancelListener {
                setResult(RESULT_CANCELED, Intent().putExtra(ERROR_STRING, "Cancelled"))
                finish()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setResult(resultCode, data)
        finish()
    }
}