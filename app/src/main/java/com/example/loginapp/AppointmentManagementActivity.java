package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.Calendar;


public class AppointmentManagementActivity extends AppCompatActivity {
    Spinner spPatient, spDoctor;
    Button btnAddAppointment;
    ListView lvAppointments;
    UserDBHelper userDBHelper;
    ArrayList<String> patientList, doctorList, appointmentList;
    ArrayAdapter<String> patientAdapter, doctorAdapter, appointmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_management);

        spPatient = findViewById(R.id.spPatient);
        spDoctor = findViewById(R.id.spDoctor);
        btnAddAppointment = findViewById(R.id.btnAddAppointment);
        lvAppointments = findViewById(R.id.lvAppointments);
        userDBHelper = new UserDBHelper(this);

        patientList = new ArrayList<>();
        doctorList = new ArrayList<>();
        appointmentList = new ArrayList<>();

        patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, patientList);
        doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorList);
        appointmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appointmentList);

        spPatient.setAdapter(patientAdapter);
        spDoctor.setAdapter(doctorAdapter);
        lvAppointments.setAdapter(appointmentAdapter);

        loadPatients();
        loadDoctors();
        loadAppointments();

        btnAddAppointment.setOnClickListener(view -> {
            int patientId = spPatient.getSelectedItemPosition() + 1;
            int doctorId = spDoctor.getSelectedItemPosition() + 1;

            if (patientId < 1 || doctorId < 1) {
                Toast.makeText(AppointmentManagementActivity.this, "Please select a patient and a doctor", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(AppointmentManagementActivity.this,
                            (datePicker, year, month, dayOfMonth) -> {
                                String appointmentdate = dayOfMonth + "-" + (month + 1) + "-" + year;
                                boolean insert = userDBHelper.insertAppointment(patientId, doctorId, appointmentdate);
                                if (insert) {
                                    Toast.makeText(AppointmentManagementActivity.this, "Appointment added successfully", Toast.LENGTH_SHORT).show();
                                    loadAppointments();
                                } else {
                                    Toast.makeText(AppointmentManagementActivity.this, "Failed to add appointment", Toast.LENGTH_SHORT).show();
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                } catch (Exception e) {
                    Log.e("AppointmentManagementActivity", "Error while adding appointment: " + e.getMessage());
                    Toast.makeText(AppointmentManagementActivity.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvAppointments.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(AppointmentManagementActivity.this)
                    .setTitle("Delete Appointment")
                    .setMessage("Are you sure you want to delete this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            Cursor cursor = userDBHelper.getAppointments();
                            if (cursor.moveToPosition(position)) {
                                int appointmentId = cursor.getInt(cursor.getColumnIndexOrThrow(UserDBHelper.COL_APPOINTMENT_ID));
                                boolean delete = userDBHelper.deleteAppointment(appointmentId);
                                if (delete) {
                                    Toast.makeText(AppointmentManagementActivity.this, "Appointment deleted successfully", Toast.LENGTH_SHORT).show();
                                    loadAppointments();
                                } else {
                                    Toast.makeText(AppointmentManagementActivity.this, "Failed to delete appointment", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("AppointmentManagementActivity", "Error while deleting patient: " + e.getMessage());
                            Toast.makeText(AppointmentManagementActivity.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    private void loadPatients() {
        try {
            patientList.clear();
            Cursor cursor = userDBHelper.getPatients();
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_PATIENT_NAME));
                    patientList.add(name);
                } while (cursor.moveToNext());
            }
            patientAdapter.notifyDataSetChanged();
        } catch (Exception d) {
            Log.d("AppointmentManagementActivity", "Error while loading patients: " + d.getMessage());
            Toast.makeText(AppointmentManagementActivity.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDoctors() {
        try {
            doctorList.clear();
            Cursor cursor = userDBHelper.getDoctors();
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    doctorList.add(name);
                } while (cursor.moveToNext());
            }
            doctorAdapter.notifyDataSetChanged();
        } catch (Exception d) {
            Log.d("AppointmentManagementActivity", "Error while loading doctors: " + d.getMessage());
            Toast.makeText(AppointmentManagementActivity.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAppointments() {
        try {
            appointmentList.clear();
            Cursor cursor = userDBHelper.getAppointments();
            if (cursor.moveToFirst()) {
                do {
                    int patientId = cursor.getInt(cursor.getColumnIndexOrThrow(UserDBHelper.COL_PATIENT_ID));
                    int doctorId = cursor.getInt(cursor.getColumnIndexOrThrow(UserDBHelper.COL_DOCTOR_ID));
                    String appointmentdate = cursor.getString(cursor.getColumnIndexOrThrow(UserDBHelper.COL_APPOINTMENT_DATE));
                    String patientName = userDBHelper.getPatientNameById(patientId);
                    String doctorName = userDBHelper.getDoctorNameById(doctorId);
                    appointmentList.add("Patient: " + patientName + "\nDoctor: " + doctorName + "\nDate of Appointment: " + appointmentdate);
                } while (cursor.moveToNext());
            }
            appointmentAdapter.notifyDataSetChanged();
        } catch (Exception d) {
            Log.d("AppointmentManagementActivity", "Error while loading appointments: " + d.getMessage());
            Toast.makeText(AppointmentManagementActivity.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }
}
