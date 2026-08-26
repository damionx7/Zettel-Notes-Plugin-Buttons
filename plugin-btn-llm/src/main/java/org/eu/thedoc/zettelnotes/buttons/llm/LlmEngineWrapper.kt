package org.eu.thedoc.zettelnotes.buttons.llm

import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class LlmEngineWrapper(
    modelPath: String,
    useGpu: Boolean,
    private val maxTokens: Int
) : AutoCloseable {

    private val engine: Engine = try {
        Engine(
            EngineConfig(
                modelPath, if (useGpu) Backend.GPU() else Backend.CPU(),
                maxNumTokens = maxTokens
            ),
        )
            .also { runBlocking { it.initialize() } }
    } catch (e: Throwable) {
        if (!useGpu) throw e

        Log.w("LlmEngine", "GPU failed, using CPU", e)

        Engine(EngineConfig(modelPath, Backend.CPU()))
            .also { runBlocking { it.initialize() } }
    }

    private var conversation: Conversation? = null

    fun startConversation() {
        conversation = engine.createConversation()
    }

    fun sendMessage(prompt: String): Flow<String> {
        val convo = conversation ?: throw IllegalStateException("Conversation not started")
        val userMessage = Message.user(prompt)   // non-deprecated constructor
        return convo.sendMessageAsync(userMessage).map { message -> extractText(message) }
    }

    private fun extractText(message: Message): String {
        return message.contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString(separator = "") { it.text }
    }

    override fun close() {
        conversation?.close()
        engine.close()
    }
}