package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class LandingPage extends AppCompatActivity {
    Button Patientbutton, Doctorbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        Patientbutton = findViewById(R.id.Patientbutton);
        Doctorbutton = findViewById(R.id.Doctorbutton);

        Patientbutton.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPage.this, PatientManagementActivity.class);
            startActivity(intent);
        });

        Doctorbutton.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPage.this, DoctorManagementActivity.class);
            startActivity(intent);
        });
    }
}