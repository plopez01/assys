package com.ac.acassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class welcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
    }

    public void startButton (View view){
        Intent myIntent = new Intent(welcomeScreen.this, basicConfig.class);
        welcomeScreen.this.startActivity(myIntent);
    }
}
