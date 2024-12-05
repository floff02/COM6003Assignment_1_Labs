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

public class PatientManagementActivity extends AppCompatActivity {
    EditText etPatientName, etPatientAge, etPatientGender;
    ListView lvPatients;
    Button btnAddPatient;
    UserDBHelper userDbHelper;
    ArrayAdapter<String> patientAdapter;
    ArrayList<String> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_management);

        etPatientName = findViewById(R.id.etPatientName);
        etPatientAge = findViewById(R.id.etPatientAge);
        etPatientGender = findViewById(R.id.etPatientGender);
        lvPatients = findViewById(R.id.lvPatients);
        btnAddPatient = findViewById(R.id.btnAddPatient);
        userDbHelper = new UserDBHelper(this);

        patientList = new ArrayList<>();
        patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientList);
        lvPatients.setAdapter(patientAdapter);

        loadPatients();

        btnAddPatient.setOnClickListener(view -> {
            String name = etPatientName.getText().toString();
            String ageStr = etPatientAge.getText().toString();
            String gender = etPatientGender.getText().toString();

            if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty()) {
                Toast.makeText(PatientManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                int age = Integer.parseInt(ageStr);
                boolean insert = userDbHelper.insertPatient(name, age, gender);
                if (insert) {
                    Toast.makeText(PatientManagementActivity.this, "Patient added successfully", Toast.LENGTH_SHORT).show();
                    loadPatients();
                } else {
                    Toast.makeText(PatientManagementActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvPatients.setOnItemClickListener((adapterView, view, i, l) -> {
            String patientDetails = patientList.get(i);
            String[] details = patientDetails.split("\n");
            String name = details[0].split(": ")[1];
            String age = details[1].split(": ")[1];
            String gender = details[2].split(": ")[1];

            etPatientName.setText(name);
            etPatientAge.setText(age);
            etPatientGender.setText(gender);

            btnAddPatient.setText("Update Patient");

            btnAddPatient.setOnClickListener(v -> {
                String newName = etPatientName.getText().toString();
                String newAgeStr = etPatientAge.getText().toString();
                String newGender = etPatientGender.getText().toString();

                if (newName.isEmpty() || newAgeStr.isEmpty() || newGender.isEmpty()) {
                    Toast.makeText(PatientManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    int newAge = Integer.parseInt(newAgeStr);
                    boolean update = userDbHelper.updatePatient(name, newName, newAge, newGender);
                    if (update) {
                        Toast.makeText(PatientManagementActivity.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                        loadPatients();
                    } else {
                        Toast.makeText(PatientManagementActivity.this, "Failed to update patient", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        lvPatients.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String patientDetails = patientList.get(i);
            String name = patientDetails.split("\\n")[0].split(": ")[1];

            new AlertDialog.Builder(PatientManagementActivity.this)
                    .setTitle("Select Action")
                    .setItems(new CharSequence[]{"Update", "Delete"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Update
                                String[] details = patientDetails.split("\\n");
                                String age = details[1].split(": ")[1];
                                String gender = details[2].split(": ")[1];

                                etPatientName.setText(name);
                                etPatientAge.setText(age);
                                etPatientGender.setText(gender);

                                btnAddPatient.setText("Update Patient");

                                btnAddPatient.setOnClickListener(v -> {
                                    String newName = etPatientName.getText().toString();
                                    String newAgeStr = etPatientAge.getText().toString();
                                    String newGender = etPatientGender.getText().toString();

                                    if (newName.isEmpty() || newAgeStr.isEmpty() || newGender.isEmpty()) {
                                        Toast.makeText(PatientManagementActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                    } else {
                                        int newAge = Integer.parseInt(newAgeStr);
                                        boolean update = userDbHelper.updatePatient(name, newName, newAge, newGender);
                                        if (update) {
                                            Toast.makeText(PatientManagementActivity.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                                            loadPatients();
                                        } else {
                                            Toast.makeText(PatientManagementActivity.this, "Failed to update patient", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case 1: // Delete
                                new AlertDialog.Builder(PatientManagementActivity.this)
                                        .setTitle("Delete Patient")
                                        .setMessage("Are you sure you want to delete this patient?")
                                        .setPositiveButton("Yes", (dialogInterface, j) -> {
                                            boolean delete = userDbHelper.deletePatient(name);
                                            if (delete) {
                                                Toast.makeText(PatientManagementActivity.this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                                                loadPatients();
                                            } else {
                                                Toast.makeText(PatientManagementActivity.this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
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

    private void loadPatients() {
        patientList.clear();
        Cursor cursor = userDbHelper.getPatients();
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_PATIENT_NAME));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(UserDBHelper.COL_PATIENT_AGE));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_PATIENT_GENDER));
                patientList.add("Name: " + name + "\nAge: " + age + "\nGender: " + gender);
            } while (cursor.moveToNext());
        }
        patientAdapter.notifyDataSetChanged();
    }
}