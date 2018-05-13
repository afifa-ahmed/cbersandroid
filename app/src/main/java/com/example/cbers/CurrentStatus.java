package com.example.cbers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.cbers.common.SessionManager;

import java.util.HashMap;

public class CurrentStatus extends AppCompatActivity {

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());

        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        String id = user.get(SessionManager.KEY_ID);

        // email
        String email = user.get(SessionManager.KEY_EMAIL);

        Log.d("CBERS", ": User Detail - "+user);

        setContentView(R.layout.activity_current_status);
    }
}
