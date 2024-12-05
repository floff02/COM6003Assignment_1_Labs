package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DoctorManagementActivity extends AppCompatActivity {
    EditText etDoctorName, etDoctorSpecialty;
    ListView lvDoctors;
    Button btnAddDoctors;
    UserDBHelper userDbHelper;
    ArrayAdapter<String> doctorAdapter;
    ArrayList<String> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_management);

        etDoctorName = findViewById(R.id.etDoctorName);
        etDoctorSpecialty = findViewById(R.id.etDoctorSpecialty);
        lvDoctors = findViewById(R.id.lvDoctors);
        btnAddDoctors = findViewById(R.id.btnAddDoctor);
        userDbHelper = new UserDBHelper(this);

        doctorList = new ArrayList<>();
        doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorList);
        lvDoctors.setAdapter(doctorAdapter);

        loadDoctors();

        btnAddDoctors.setOnClickListener(view -> {
            String name = etDoctorName.getText().toString();
            String specialty = etDoctorSpecialty.getText().toString();

            if (name.isEmpty() || specialty.isEmpty()) {
                Toast.makeText(DoctorManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean insert = userDbHelper.insertDoctor(name, specialty);
                if (insert) {
                    Toast.makeText(DoctorManagementActivity.this, "Doctor added successfully", Toast.LENGTH_SHORT).show();
                    loadDoctors();
                } else {
                    Toast.makeText(DoctorManagementActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvDoctors.setOnItemClickListener((adapterView, view, i, l) -> {
            String doctorDetails = doctorList.get(i);
            String[] details = doctorDetails.split("\n");
            String name = details[0].split(": ")[1];
            String specialty = details[1].split(": ")[1];


            etDoctorName.setText(name);
            etDoctorSpecialty.setText(specialty);

            btnAddDoctors.setText("Update Doctor");

            btnAddDoctors.setOnClickListener(v -> {
                String newName = etDoctorName.getText().toString();
                String newSpecialty = etDoctorSpecialty.getText().toString();

                if (newName.isEmpty() || newSpecialty.isEmpty()) {
                    Toast.makeText(DoctorManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    boolean update = userDbHelper.updateDoctor(name, newName, newSpecialty);
                    if (update) {
                        Toast.makeText(DoctorManagementActivity.this, "Doctor updated successfully", Toast.LENGTH_SHORT).show();
                        loadDoctors();
                    } else {
                        Toast.makeText(DoctorManagementActivity.this, "Failed to update doctor", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        lvDoctors.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String doctorDetails = doctorList.get(i);
            String name = doctorDetails.split("\\n")[0].split(": ")[1];

            new AlertDialog.Builder(DoctorManagementActivity.this)
                    .setTitle("Select Action")
                    .setItems(new CharSequence[]{"Update", "Delete"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Update
                                String[] details = doctorDetails.split("\\n");
                                String specialty = details[1].split(": ")[1];


                                etDoctorName.setText(name);
                                etDoctorSpecialty.setText(specialty);

                                btnAddDoctors.setText("Update Doctor");

                                btnAddDoctors.setOnClickListener(v -> {
                                    String newName = etDoctorName.getText().toString();
                                    String newSpecialty = etDoctorSpecialty.getText().toString();


                                    if (newName.isEmpty() || newSpecialty.isEmpty()) {
                                        Toast.makeText(DoctorManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                    } else {
                                        boolean update = userDbHelper.updateDoctor(name, newName, newSpecialty);
                                        if (update) {
                                            Toast.makeText(DoctorManagementActivity.this, "Doctor updated successfully", Toast.LENGTH_SHORT).show();
                                            loadDoctors();
                                        } else {
                                            Toast.makeText(DoctorManagementActivity.this, "Failed to update doctor", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case 1: // Delete
                                new AlertDialog.Builder(DoctorManagementActivity.this)
                                        .setTitle("Delete Doctor")
                                        .setMessage("Are you sure you want to delete this doctor?")
                                        .setPositiveButton("Yes", (dialogInterface, j) -> {
                                            boolean delete = userDbHelper.deleteDoctor(name);
                                            if (delete) {
                                                Toast.makeText(DoctorManagementActivity.this, "Doctor deleted successfully", Toast.LENGTH_SHORT).show();
                                                loadDoctors();
                                            } else {
                                                Toast.makeText(DoctorManagementActivity.this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                break;
                        }
                    })
                    .show();
            return true;
        });
    }

    private void loadDoctors() {
        doctorList.clear();
        Cursor cursor = userDbHelper.getDoctors();
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_DOCTOR_NAME));
                String specialty = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_DOCTOR_SPECIALTY));
                doctorList.add("Name: " + name +  "\nSpecialty: " + specialty);
            } while (cursor.moveToNext());
        }
        doctorAdapter.notifyDataSetChanged();
    }
}