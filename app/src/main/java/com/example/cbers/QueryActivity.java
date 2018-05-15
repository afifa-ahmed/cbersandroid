package com.example.cbers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cbers.common.HttpUtils;
import com.example.cbers.common.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class QueryActivity extends AppCompatActivity {

    private SessionManager session;
    private long id;
    private EditText mQueryView;

    boolean querySubmitSuccess;
    private SubmitQueryTask mSubmitQueryTask = null;
    private String submitQueryResponseMessage = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        id = Long.parseLong(user.get(SessionManager.KEY_ID));

        Log.d("CBERS", ": User Detail - " + user);
        setContentView(R.layout.activity_query);

        mQueryView = (EditText) findViewById(R.id.queryText);
    }

    void doQuerySubmit(View view) {
        String query = mQueryView.getText().toString();
        if (query.trim().length() < 10) {
            mQueryView.setError(getString(R.string.error_invalid_query));
            mQueryView.requestFocus();
        } else {
            mSubmitQueryTask = new SubmitQueryTask(id, query);
            mSubmitQueryTask.execute((Void) null);
        }

    }

    /**
     * Represents an asynchronous to submit query.
     */
    public class SubmitQueryTask extends AsyncTask<Void, Void, Boolean> {

        private final String mQuery;
        private final long mPatientId;

        SubmitQueryTask(long patiend_id, String query) {
            mQuery = query;
            mPatientId = patiend_id;

        }

        @Override
        protected Boolean doInBackground(final Void... params) {

            Log.d("CBERS", "Trying to submit query");
            final RequestParams reqParams = new RequestParams();
            reqParams.put("patient_id", mPatientId);
            reqParams.put("patient_query", mQuery);
            reqParams.put("action", "query");

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("CBERS", "Submitting query in background.");
                    HttpUtils.post("incident", reqParams, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("CBERS", "StatusCode : " + statusCode);
                            Log.d("CBERS", "Response : " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                String status = serverResp.getString("status");
                                submitQueryResponseMessage = serverResp.getString("message");
                                Log.d("CBERS", "Status : " + status);
                                Log.d("CBERS", "Message : " + submitQueryResponseMessage);
                                if (statusCode == 200 && "success".equalsIgnoreCase(status)) {

                                    Log.d("CBERS", "Query submitted successfully.");
                                    showToast(submitQueryResponseMessage);
                                    openCurrentStatus();

                                    querySubmitSuccess = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("CBERS", "Submit Query failed in SuccessListener. " + e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {

                            Log.d("CBERS", "StatusCode FailureListener: " + statusCode);
                            Log.d("CBERS", "Response FailureListener: " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                String status = serverResp.getString("status");
                                submitQueryResponseMessage = serverResp.getString("message");
                                Log.d("CBERS", "Status FailureListener: " + status);
                                Log.d("CBERS", "Message FailureListener: " + submitQueryResponseMessage);
                                showToast(submitQueryResponseMessage);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("CBERS", "Submit Query failed in FailureListener. " + e.getMessage());
                                showToast("Something went wrong.");
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
            Log.d("CBERS", "Fetched status in background.");

            return querySubmitSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSubmitQueryTask = null;

            if (success) {
                Log.d("CBERS", "Post Execute Success.");
            } else {
                Log.d("CBERS", "In onPostExecute after failure.");
            }
        }

        @Override
        protected void onCancelled() {
            mSubmitQueryTask = null;
//            showProgress(false);
        }
    }

    void openCurrentStatus() {
        Log.d("CBERS", "Going to currentStatusActivity");
        Intent i = new Intent(getApplicationContext(), CurrentStatusActivity.class);
        startActivity(i);
        finish();
    }

    void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }


}
