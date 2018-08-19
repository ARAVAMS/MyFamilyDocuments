package com.aravamsinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by sivaprakash on 12/29/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}