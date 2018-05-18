package com.example.cbers.common;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.cbers.LoginActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_ID = "id";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String email, long id) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putLong(KEY_ID, id);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();

        sendToServer(id);
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_ID, pref.getLong(KEY_ID, -1) + "");

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    private void sendToServer(long patient_id) {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean newToken = preferences.getBoolean("newToken", false);
        if (newToken) {
            String token = preferences.getString("cbers_token", null);
            Log.d("CBERS", "No token found Posting Token in background. ");

            final RequestParams reqParams = new RequestParams();
            reqParams.put("patient_id", patient_id);
            reqParams.put("token", token);

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("CBERS", "Posting Token in background. " + reqParams);
                    HttpUtils.post("fcm", reqParams, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("CBERS", "StatusCode : " + statusCode);
                            Log.d("CBERS", "Response : " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                String status = serverResp.getString("status");
                                String message = serverResp.getString("message");
                                Log.d("CBERS", "Status : " + status);
                                Log.d("CBERS", "Message : " + message);
                                if (statusCode == 200 && "success".equalsIgnoreCase(status)) {

                                    Log.d("CBERS", "Token Posted successfully.");
                                    SharedPreferences.Editor editor = preferences.edit();

                                    // Save to SharedPreferences
                                    editor.putBoolean("newToken", false);
                                    editor.apply();
                                    Log.d("CBERS", "New Token boolean set to false.");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("CBERS", "Posting Token failed in SuccessListener. " + e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {

                            Log.d("CBERS", "StatusCode FailureListener: " + statusCode);
                            Log.d("CBERS", "Response FailureListener: " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                String status = serverResp.getString("status");
                                String message = serverResp.getString("message");
                                Log.d("CBERS", "Status FailureListener: " + status);
                                Log.d("CBERS", "Message FailureListener: " + message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("CBERS", "Posting Token failed in FailureListener. " + e.getMessage());
                            }
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                            // Pull out the first event on the public timeline

                        }
                    });
                }
            };
            mainHandler.post(myRunnable);
            Log.d("CBERS", "Posted token in background.");
        } else {
            Log.d("CBERS", "Token found skipping... ");
        }
    }
}