package com.example.cbers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.cbers.common.HttpUtils;
import com.example.cbers.common.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.LoaderManager.LoaderCallbacks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CurrentStatusActivity extends AppCompatActivity {

    SessionManager session;

    boolean fetchStatusSucces;
    private GetStatusTask mFetchStatusTask = null;

    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        long id = Long.parseLong(user.get(SessionManager.KEY_ID));
        String email = user.get(SessionManager.KEY_EMAIL);

        Log.d("CBERS", ": User Detail - " + user);

        setContentView(R.layout.activity_current_status);


        mFetchStatusTask = new GetStatusTask(email, id);
        mFetchStatusTask.execute((Void) null);
        lv = (ListView) findViewById(R.id.statusList);

//        TextView t = findViewById(R.id.doctorAdvice);
//        t.setText("You are going to die soon, please prepare for funeral. You should have take care, when you had chance. Now nothing can be done.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_logout) {
            session.logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    void openQueryForm(View view) {
        Intent intent = new Intent(this, QueryActivity.class);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous to fetch current status.
     */
    public class GetStatusTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final long mPatientId;

        GetStatusTask(String email, long patiend_id) {
            mEmail = email;
            mPatientId = patiend_id;

        }

        @Override
        protected Boolean doInBackground(final Void... params) {

            Log.d("CBERS", "Trying to fetch current status");
            final RequestParams reqParams = new RequestParams();
            reqParams.put("patient_id", mPatientId);

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("CBERS", "Fetching status in background.");
                    HttpUtils.get("patientStatusLog", reqParams, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("CBERS", "StatusCode : " + statusCode);
                            Log.d("CBERS", "Response : " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                long ptId = serverResp.getLong("id");
                                Log.d("CBERS", "Patient Id : " + ptId);
                                if (statusCode == 200) {
                                    // TODO ADAPTER CODE
                                    listItems.add("Temparature: " + serverResp.getInt("temperature"));
                                    listItems.add("Heart Rate: " + serverResp.getInt("heartRate"));
                                    listItems.add("Blood Pressure: " + serverResp.getString("bloodPressure").replace("-", "/"));
                                    listItems.add("Sugar: " + serverResp.getInt("bloodSugar"));
                                    Log.d("CBERS", "Response : " + listItems);

                                    adapter = new ArrayAdapter<String>(getApplicationContext(),
                                            android.R.layout.simple_list_item_1,
                                            listItems);
                                    lv.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    Log.d("CBERS", "Adapter Set");
                                    fetchStatusSucces = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("CBERS", "Fetching status failed. " + e.getMessage());
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

            return fetchStatusSucces;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchStatusTask = null;

            if (success) {
                Log.d("CBERS", "Post Execute Success.");
//                finish();
            } else {
                Log.d("CBERS", "In onPostExecute after failure.");
            }
        }

        @Override
        protected void onCancelled() {
            mFetchStatusTask = null;
//            showProgress(false);
        }
    }
}
