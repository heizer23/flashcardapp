package com.example.flashcardapp.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ChatGPTActivity : AppCompatActivity() {

    private lateinit var etPrompt: EditText
    private lateinit var etTopic: EditText
    private lateinit var etInformativeText: EditText
    private lateinit var btnAccept: Button
    private lateinit var btnBack: Button
    private lateinit var rvGeneratedQuestions: RecyclerView

    private var buildMode = true // local JSON mode by default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_gpt)

        etPrompt = findViewById(R.id.et_prompt)
        etTopic = findViewById(R.id.et_topic)
        etInformativeText = findViewById(R.id.et_informative_text)
        btnAccept = findViewById(R.id.btn_accept)
        btnBack = findViewById(R.id.btn_back)
        rvGeneratedQuestions = findViewById(R.id.rv_questions)

        rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)

        btnBack.visibility = View.GONE
        btnAccept.text = "Submit"
        btnAccept.setOnClickListener { handleSubmit() }
        btnBack.setOnClickListener { resetToPromptView() }
    }

    private fun handleSubmit() {
        if (buildMode) {
            val localJson = loadLocalJson()
            if (localJson != null) {
                displayResponse(localJson)
            } else {
                Toast.makeText(this, "Failed to load local JSON", Toast.LENGTH_SHORT).show()
            }
        } else {
            handleRealSubmit()
        }
    }

    private fun handleRealSubmit() {
        val userInput = etPrompt.text.toString().trim()
        if (userInput.isNotEmpty()) {
            ChatGPTHelper.getContextForQuestion(userInput, this, object : ChatGPTHelper.OnChatGPTResponse {
                override fun onSuccess(response: String) {
                    displayResponse(response)
                }

                override fun onFailure(error: String) {
                    runOnUiThread {
                        etInformativeText.setText("Failed to connect to GPT: $error")
                        Toast.makeText(this@ChatGPTActivity, "Failed to connect to GPT", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            etPrompt.visibility = View.GONE
            btnAccept.text = "Accept"
            btnBack.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResponse(response: String) {
        try {
            val parsedContent = JSONObject(response)
            etTopic.setText(parsedContent.optString("title", "No title"))
            etInformativeText.setText(parsedContent.optString("informative_text", "No informative text"))

            val questionsArray = parsedContent.optJSONArray("questions")
            if (questionsArray != null) {
                val flashcards = mutableListOf<Flashcard>()
                for (i in 0 until questionsArray.length()) {
                    val questionObject = questionsArray.getJSONObject(i)
                    val fc = Flashcard()
                    fc.question = questionObject.optString("question")
                    fc.answer = questionObject.optString("answer")
                    flashcards.add(fc)
                }
                // We can show them in a RecyclerView if needed.
                val adapter = ChatGPTQuestionAdapter(mutableListOf(), null)
                rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)
                rvGeneratedQuestions.adapter = adapter
                // adapter.updateData(flashcards) if you want to show them.
            }
        } catch (e: Exception) {
            etInformativeText.setText("Error parsing response")
            Log.e("ChatGPTActivity", "Error parsing JSON response", e)
        }
    }

    private fun loadLocalJson(): String? {
        val sb = StringBuilder()
        try {
            val inputStream = resources.openRawResource(R.raw.sample_response)
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            }
            return sb.toString()
        } catch (e: IOException) {
            Log.e("ChatGPTActivity", "Error loading JSON", e)
            return null
        }
    }

    private fun resetToPromptView() {
        etPrompt.visibility = View.VISIBLE
        btnAccept.text = "Submit"
        btnBack.visibility = View.GONE
    }
}
