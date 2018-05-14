package com.example.cbers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.example.cbers.common.HttpUtils;
import com.example.cbers.common.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CurrentStatus extends AppCompatActivity {

    SessionManager session;
    boolean fetchStatusSucces;
    private GetStatusTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        String id = user.get(SessionManager.KEY_ID);
        String email = user.get(SessionManager.KEY_EMAIL);

        Log.d("CBERS", ": User Detail - "+user);

        setContentView(R.layout.activity_current_status);

        final ListView listview = (ListView) findViewById(R.id.statusList);
        String[] values = new String[] { "Temperature: 98", "Heart Rate: 80", "Blood Pressure: 80-110",
                "Sugar: 130"};

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.navigation_logout){
            session.logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
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

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    //Code that uses AsyncHttpClient in your case ConsultaCaract()
                    HttpUtils.post("patientLog?patient_id"+mPatientId, null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            // If the response is JSONObject instead of expected JSONArray
                            Log.d("CBERS", "StatusCode : " + statusCode);
                            Log.d("CBERS", "Response : " + response);
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                long ptId = serverResp.getLong("id");
                                Log.d("CBERS", "Patient Id : " + ptId);
                                if (statusCode == 200) {
                                    // TODO ADAPTER CODE
                                    fetchStatusSucces = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
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


            return fetchStatusSucces;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
//            showProgress(false);

            if (success) {
                finish();
            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
//            showProgress(false);
        }
    }
}
