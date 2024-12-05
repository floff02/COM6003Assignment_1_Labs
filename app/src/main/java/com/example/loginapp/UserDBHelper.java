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




    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME_USERS + " (username TEXT primary key, password TEXT, salt TEXT)");
        db.execSQL("create table " + TABLE_NAME_PATIENTS + " (" + COL_PATIENT_ID + " INTEGER primary key AUTOINCREMENT, " + COL_PATIENT_NAME + " TEXT, " + COL_PATIENT_AGE + " INTEGER, " + COL_PATIENT_GENDER + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PATIENTS);
        onCreate(db);
    }

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
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }


    public boolean insertPatient(String name, int age, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PATIENT_NAME, name);
        contentValues.put(COL_PATIENT_AGE, age);
        contentValues.put(COL_PATIENT_GENDER, gender);
        long result = db.insert(TABLE_NAME_PATIENTS, null, contentValues);
        return result != -1;
    }

    public boolean updatePatient(String oldName, String newName, int age, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PATIENT_NAME, newName);
        contentValues.put(COL_PATIENT_AGE, age);
        contentValues.put(COL_PATIENT_GENDER, gender);
        int result = db.update(TABLE_NAME_PATIENTS, contentValues, COL_PATIENT_NAME + " = ?", new String[]{oldName});
        return result > 0;
    }

    public boolean deletePatient(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME_PATIENTS, COL_PATIENT_NAME + " = ?", new String[]{name});
        return result > 0;
    }
    public Cursor getPatients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_PATIENTS, null);
    }


}