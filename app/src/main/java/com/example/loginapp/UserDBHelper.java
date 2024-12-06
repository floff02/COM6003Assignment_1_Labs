package com.example.loginapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class UserDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UserDB.db";
    public static final String TABLE_NAME_USERS = "users";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_SALT = "salt";

    public static final String TABLE_NAME_PATIENTS = "patients";
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_PATIENT_NAME = "name";
    public static final String COL_PATIENT_AGE = "age";
    public static final String COL_PATIENT_GENDER = "gender";

    public static final String TABLE_NAME_DOCTORS = "doctors";
    public static final String COL_DOCTOR_ID = "doctor_id";
    public static final String COL_DOCTOR_NAME = "name";
    public static final String COL_DOCTOR_SPECIALTY = "specialty";

    public static final String TABLE_NAME_APPOINTMENTS = "appointments";
    public static final String COL_APPOINTMENT_ID = "appointment_id";
    public static final String COL_DOCTOR_ID_APPOINTMENT = "doctor_id";
    public static final String COL_PATIENT_ID_APPOINTMENT = "patient_id";
    public static final String COL_APPOINTMENT_DATE = "appointment_date";


    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    //Creates tables and rows that will be used within the application, setting primary keys and foreign keys for connectivity within tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME_USERS + " (username TEXT primary key, password TEXT, salt TEXT)");
        db.execSQL("create table " + TABLE_NAME_PATIENTS + " (" + COL_PATIENT_ID + " INTEGER primary key AUTOINCREMENT, " + COL_PATIENT_NAME + " TEXT, " + COL_PATIENT_AGE + " INTEGER, " + COL_PATIENT_GENDER + " TEXT)");
        db.execSQL("create table " + TABLE_NAME_DOCTORS + " (" + COL_DOCTOR_ID + " INTEGER primary key AUTOINCREMENT, name TEXT, specialty TEXT)");
        db.execSQL("create table " + TABLE_NAME_APPOINTMENTS + " (" + COL_APPOINTMENT_ID + " INTEGER primary key AUTOINCREMENT, patient_id INTEGER, doctor_id INTEGER, appointment_date TEXT, FOREIGN KEY(patient_id) REFERENCES " + TABLE_NAME_PATIENTS + "(" + COL_PATIENT_ID + "), FOREIGN KEY(doctor_id) REFERENCES " + TABLE_NAME_DOCTORS + "(doctor_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOCTORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_APPOINTMENTS);
        onCreate(db);
    }

    //Creates the function insertUser which takes inputs username and password, calls the function generateSalt and passes the generated salt with the password into the hashPassword function, storing the username, hashedPassword and the generated salt into the users table
    public boolean insertUser(String username, String password) {
        Log.d("DBHelper", "Attempting to insert user: " + username);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, hashedPassword);
        contentValues.put(COL_SALT, salt);
        long result = db.insert(TABLE_NAME_USERS, null, contentValues);
        return result != -1;
    }
    //Creates the function checkUser which takes the username and password inputted by the user when logging in, connects to the Users table and retrieves the stored hashedpassword and the generated salt used
    //The function then calls the hashpassword function to hash the password inputted when logging in, if the results are the same its returns the value TRUE, if the passwords are different the Value FALSE is returned instead
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME_USERS + " where username=?", new String[]{username});
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
            String salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALT));
            String hashedPassword = hashPassword(password, salt);
            Log.d("CheckUser", "HashedPassword: " + hashedPassword);
            return storedPassword.equals(hashedPassword);
        }
        return false;
    }
    //Creates the function hashpassword which takes the inputs of password and salt, using the hashing algorithm SHA-256 and the generated salt combined with the password
    //this generates a byte array which represents the hashed password, this is then converted into a hexadecimal string which is then returned for registration or validation
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    //Creates the generateSalt function, this function creates a ramndom number using secure random which is used in cases where values need to be unpredictable and secure
    //Then a byte array is created that is 16 bytes long, the array is then filled with random byte from the secure random number that was generated
    //The byte array is then converted into a Base64 encoded string which is used for storage or transit to ensure data isn't lost
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    //Creates the function insertPatient which takes the inputs of name, age and gender, creates the variable contentvalues and assigns the values inputted in the function, these values are then stored within the patient table
    public boolean insertPatient(String name, int age, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PATIENT_NAME, name);
        contentValues.put(COL_PATIENT_AGE, age);
        contentValues.put(COL_PATIENT_GENDER, gender);
        long result = db.insert(TABLE_NAME_PATIENTS, null, contentValues);
        return result != -1;
    }

    //This function allows for patients stored within the patient table to edited, taking the old name, new name, age and gender from the input and assigning the new values in place of the old values in the row associated with the old name
    public boolean updatePatient(String oldName, String newName, int age, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PATIENT_NAME, newName);
        contentValues.put(COL_PATIENT_AGE, age);
        contentValues.put(COL_PATIENT_GENDER, gender);
        int result = db.update(TABLE_NAME_PATIENTS, contentValues, COL_PATIENT_NAME + " = ?", new String[]{oldName});
        return result > 0;
    }

    //This function deletes the row associated with the name passed into the function
    public boolean deletePatient(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME_PATIENTS, COL_PATIENT_NAME + " = ?", new String[]{name});
        return result > 0;
    }
    //This function calls all patient values within the patient table
    public Cursor getPatients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_PATIENTS, null);
    }

    //Creates the function insertDoctor which takes the inputs of name and specialty creates the variable contentvalues and assigns the values inputted in the function, these values are then stored within the doctors table
    public boolean insertDoctor(String name, String specialty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DOCTOR_NAME, name);
        contentValues.put(COL_DOCTOR_SPECIALTY, specialty);
        long result = db.insert(TABLE_NAME_DOCTORS, null, contentValues);
        return result != -1;
    }
    //This function allows for doctors stored within the docotr table to edited, taking the old name, new name and specialty from the input and assigning the new values in place of the old values in the row associated with the old name
    public boolean updateDoctor(String oldName, String newName, String specialty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DOCTOR_NAME, newName);
        contentValues.put(COL_DOCTOR_SPECIALTY, specialty);
        int result = db.update(TABLE_NAME_DOCTORS, contentValues, COL_DOCTOR_NAME + " = ?", new String[]{oldName});
        return result > 0;
    }
    //This function deletes the row associated with the name passed into the function
    public boolean deleteDoctor(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME_DOCTORS, COL_DOCTOR_NAME + " = ?", new String[]{name});
        return result > 0;
    }
    //This function calls all doctor values within the doctors table
    public Cursor getDoctors() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_DOCTORS, null);
    }

    //This function gets the name of a doctor by passing an ID into the function, finding the associated name with the Id and returning the name
    public String getDoctorNameById(int doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_DOCTORS + " WHERE doctor_id = ?", new String[]{String.valueOf(doctorId)});
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }
        return "";
    }
    //This function gets the name of a patient by passing an ID into the function, finding the associated name with the Id and returning the name
    public String getPatientNameById(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_PATIENTS + " WHERE patient_id = ?", new String[]{String.valueOf(patientId)});
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }
        return "";
    }
    //This function gets all appointments within appointments table and returns them
    public Cursor getAppointments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_APPOINTMENTS, null);
    }
    //This function taking inputs patientId, doctorId and appointmentdate, creating the variable contentValues assigning each input and then storing the data within the appointments table
    public boolean insertAppointment(int patientId, int doctorId, String appointmentdate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PATIENT_ID_APPOINTMENT, patientId);
        contentValues.put(COL_DOCTOR_ID_APPOINTMENT, doctorId);
        contentValues.put(COL_APPOINTMENT_DATE, appointmentdate);
        long result = db.insert(TABLE_NAME_APPOINTMENTS, null, contentValues);
        return result != -1;
    }
    //Takes an appointmentId and deletes the associated row from the appointments table
    public boolean deleteAppointment(int appointmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME_APPOINTMENTS, COL_APPOINTMENT_ID + " = ?", new String[]{String.valueOf(appointmentId)});
        return result > 0;
    }

}