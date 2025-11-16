package com.example.flashcardapp.main

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Properties

object ChatGPTHelper {

    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private var API_KEY: String? = null

    interface OnChatGPTResponse {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private fun initializeApiKey(context: Context) {
        if (API_KEY == null) {
            try {
                val assetManager: AssetManager = context.assets
                assetManager.open("config.properties").use { inputStream ->
                    val properties = Properties()
                    properties.load(inputStream)
                    API_KEY = properties.getProperty("API_KEY")
                }
            } catch (e: IOException) {
                Log.e("ChatGPTHelper", "Error loading API key: ${'$'}{e.message}")
            }
        }
    }

    fun makeChatGPTRequest(prompt: String, callback: OnChatGPTResponse, context: Context) {
        initializeApiKey(context)
        val localKey = API_KEY
        if (localKey == null) {
            callback.onFailure("API_KEY is null")
            return
        }
        try {
            val jsonObject = JSONObject()
            jsonObject.put("model", "gpt-4-turbo")

            val messagesArray = JSONArray()
            val messageObject = JSONObject()
            messageObject.put("role", "user")
            messageObject.put("content", prompt)
            messagesArray.put(messageObject)
            jsonObject.put("messages", messagesArray)

            val json = jsonObject.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body: RequestBody = RequestBody.create(mediaType, json)

            val request = Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer ${'$'}localKey")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure("Failed to connect to GPT: ${'$'}{e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful) {
                            // Replace deprecated response.body() with response.body
                            val responseBody = response.body?.string() ?: ""
                            callback.onSuccess(responseBody)
                        } else {
                            // Use .code property if desired, or .code() still works in OkHttp 4.x
                            callback.onFailure("Error: ${'$'}{response.code}")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            callback.onFailure("Error constructing JSON payload: ${'$'}{e.message}")
        }
    }

    fun generateMultipleQuestions(prompt: String, context: Context, callback: OnChatGPTResponse) {
        makeChatGPTRequest(prompt, object : OnChatGPTResponse {
            override fun onSuccess(response: String) {
                try {
                    val jsonArray = JSONArray(response)
                    callback.onSuccess(jsonArray.toString())
                } catch (e: Exception) {
                    callback.onFailure("Error parsing JSON response: ${'$'}{e.message}")
                    Log.e("ChatGPTHelper", "Error parsing JSON response", e)
                }
            }

            override fun onFailure(error: String) {
                callback.onFailure(error)
            }
        }, context)
    }

    fun getContextForQuestion(question: String, context: Context, callback: OnChatGPTResponse) {
        val prompt = "Explain why this was important, starting with the most significant reason. " +
                "Write a 150-word answer with the most important reason in the first sentence, followed by a new line after that sentence. " +
                "Important: Do NOT repeat details from the question or the answer! " +
                "Include one additional relevant fact in the explanation.  ${'$'}question"

        makeChatGPTRequest(prompt, object : OnChatGPTResponse {
            override fun onSuccess(response: String) {
                try {
                    Log.d("ChatGPT Response", "Full response: ${'$'}response")
                    val jsonResponse = JSONObject(response)
                    val content = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    callback.onSuccess(content)
                } catch (e: Exception) {
                    callback.onFailure("Error parsing JSON response: ${'$'}{e.message}")
                    Log.e("ChatGPTHelper", "Error parsing JSON response", e)
                }
            }

            override fun onFailure(error: String) {
                callback.onFailure(error)
            }
        }, context)
    }

    fun generateRelatedQuestion(question: String, callback: OnChatGPTResponse, context: Context) {
        val prompt = "Generate a related question for the following: \"${'$'}question\". " +
                "Provide the response in a JSON array with the following format: " +
                "[{\"question\": \"<new question>\", \"answer\": \"<corresponding answer>\", \"searchTerm\": \"<related search term>\", \"userNote\": \"<a note about the question>\", \"topics\": [\"<topic1>\", \"<topic2>\"]}]. " +
                "Ensure the JSON array contains only one question and is valid."

        makeChatGPTRequest(prompt, object : OnChatGPTResponse {
            override fun onSuccess(response: String) {
                val intent = Intent(context, EditFlashcardActivity::class.java)
                intent.putExtra("generated_question", response)
                context.startActivity(intent)
            }

            override fun onFailure(error: String) {
                callback.onFailure(error)
            }
        }, context)
    }
}
