// RegisterActivity.java berhasil
package com.example.kepengendessboxapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtRegFullName, edtRegPhone, edtRegEmail, edtRegPass, edtRegConPass;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtRegFullName = findViewById(R.id.edtRegFullName);
        edtRegPhone = findViewById(R.id.edtRegPhone);
        edtRegEmail = findViewById(R.id.edtRegEmail);
        edtRegPass = findViewById(R.id.edtRegPass);
        edtRegConPass = findViewById(R.id.edtRegConPass);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        fireDb = FirebaseFirestore.getInstance();

        setupPasswordToggle(edtRegPass);
        setupPasswordToggle(edtRegConPass);
        showPasswordInitially(edtRegPass);
        showPasswordInitially(edtRegConPass);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String fullName = edtRegFullName.getText().toString().trim();
        String phone = edtRegPhone.getText().toString().trim();
        String email = edtRegEmail.getText().toString().trim();
        String password = edtRegPass.getText().toString();
        String conPassword = edtRegConPass.getText().toString();

        // Validasi input
        if (fullName.isEmpty()) {
            edtRegFullName.setError("Nama lengkap tidak boleh kosong");
            edtRegFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            edtRegPhone.setError("Nomor telepon tidak boleh kosong");
            edtRegPhone.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtRegEmail.setError("Email tidak boleh kosong");
            edtRegEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtRegEmail.setError("Email tidak valid");
            edtRegEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtRegPass.setError("Kata sandi tidak boleh kosong");
            edtRegPass.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edtRegPass.setError("Minimal 6 karakter");
            edtRegPass.requestFocus();
            return;
        }
        if (conPassword.isEmpty()) {
            edtRegConPass.setError("Konfirmasi kata sandi tidak boleh kosong");
            edtRegConPass.requestFocus();
            return;
        }
        if (!password.equals(conPassword)) {
            edtRegConPass.setError("Kata sandi tidak cocok");
            edtRegConPass.requestFocus();
            return;
        }

        // Sembunyikan keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        // Daftar ke Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();

                        String uid = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("NamaLengkap", fullName);
                        user.put("NoWhatsapp", phone);
                        user.put("Email", email);

                        // Simpan data user ke Firestore
                        fireDb.collection("users").document(uid).set(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Data tersimpan", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        // Pindah ke LoginActivity
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Registrasi Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showPasswordInitially(EditText editText) {
        editText.setTransformationMethod(null);
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.eye, 0);
        editText.setTag("password_shown");
    }

    private void setupPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    boolean isShown = "password_shown".equals(editText.getTag());
                    if (isShown) {
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.eye_off, 0);
                        editText.setTag("password_hidden");
                    } else {
                        editText.setTransformationMethod(null);
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.eye, 0);
                        editText.setTag("password_shown");
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}