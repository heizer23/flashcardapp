package com.example.flashcardapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class ChatGPTHelper {


    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    // Callback interfaces
    public interface OnChatGPTResponse {
        void onSuccess(String response);
        void onFailure(String error);
    }

    // General ChatGPT Request Method
    private static void makeChatGPTRequest(String prompt, OnChatGPTResponse callback) {
        try {
            // Create JSON request body
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", prompt);
            messagesArray.put(messageObject);

            jsonObject.put("messages", messagesArray);
            String json = jsonObject.toString();

            // Build request
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            // Send request asynchronously
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Failed to connect to GPT: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("Error: " + response.code());
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            callback.onFailure("Error constructing JSON payload: " + e.getMessage());
        }
    }

    // Specific method for getting context for a question
    public static void getContextForQuestion(String question, OnChatGPTResponse callback) {
        String prompt = "Explain why this was important, starting with the most significant reason. Write a 150-word answer with the most important reason in the first sentence, followed by a new line after that sentence. Important: Do NOT repeat details from the question or the answer! Include one additional relevant fact in the explanation.  " + question;
        makeChatGPTRequest(prompt, new OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Log the response to confirm its structure
                    Log.d("ChatGPT Response", "Full response: " + response);

                    // Parse the JSON response to get the "content" field directly
                    JSONObject jsonResponse = new JSONObject(response);
                    String content = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    // Display the content directly as the context
                    callback.onSuccess(content);  // Pass the content back to the calling activity or component
                } catch (Exception e) {
                    callback.onFailure("Error parsing JSON response: " + e.getMessage());
                    Log.e("ChatGPTHelper", "Error parsing JSON response", e);
                }
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }


    // Specific method for rephrasing a question
    public static void rephraseQuestion(String question, OnChatGPTResponse callback) {
        String prompt = "Rephrase the question: " + question;
        makeChatGPTRequest(prompt, callback);
    }

    // Specific method for generating a related question
    public static void generateRelatedQuestion(String question, OnChatGPTResponse callback, Context context) {
        String prompt = "Generate a new question related to: " + question;
        makeChatGPTRequest(prompt, new OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                // Pass the response (generated question) to EditQuestionActivity
                Intent intent = new Intent(context, EditFlashcardActivity.class);
                intent.putExtra("generated_question", response);  // Add response to intent
                context.startActivity(intent);
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

}
