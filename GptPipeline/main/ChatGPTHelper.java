package com.example.flashcardapp.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ChatGPTHelper {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static String API_KEY;

    private static void initializeApiKey(Context context) {
        if (API_KEY == null) {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open("config.properties");
                Properties properties = new Properties();
                properties.load(inputStream);
                API_KEY = properties.getProperty("API_KEY");
            } catch (IOException e) {
                Log.e("ChatGPTHelper", "Error loading API key: " + e.getMessage());
            }
        }
    }

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
    public static void makeChatGPTRequest(String prompt, OnChatGPTResponse callback, Context context) {

        initializeApiKey(context);

        try {
            // Create JSON request body
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "gpt-4-turbo");

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

    public static void generateMultipleQuestions(String prompt, Context context, OnChatGPTResponse callback) {

        makeChatGPTRequest(prompt, new OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parse the response as an array of questions
                    JSONArray jsonArray = new JSONArray(response);
                    callback.onSuccess(jsonArray.toString());
                } catch (Exception e) {
                    callback.onFailure("Error parsing JSON response: " + e.getMessage());
                    Log.e("ChatGPTHelper", "Error parsing JSON response", e);
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        }, context);
    }

    // Specific method for getting context for a question
    public static void getContextForQuestion(String question,Context context,  OnChatGPTResponse callback) {
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
        },context);
    }




    // Specific method for generating a related question
    public static void generateRelatedQuestion(String question, OnChatGPTResponse callback, Context context) {
        String prompt = "Generate a related question for the following: \"" + question + "\". " +
                "Provide the response in a JSON array with the following format: " +
                "[{\"question\": \"<new question>\", \"answer\": \"<corresponding answer>\", \"searchTerm\": \"<related search term>\", " +
                "\"userNote\": \"<a note about the question>\", \"topics\": [\"<topic1>\", \"<topic2>\"]}]. " +
                "Ensure the JSON array contains only one question and is valid.";
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
        }, context);
    }

}
