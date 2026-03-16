// LoginActivity.java berhasil
package com.example.kepengendessboxapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPass;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;

    private static final String ADMIN_EMAIL = "kepengendessbox@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang masuk...");
        progressDialog.setCancelable(false);

        setupPasswordToggle(edtPass);
        showPasswordInitially(edtPass);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Email tidak boleh kosong");
            edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email tidak valid");
            edtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPass.setError("Password tidak boleh kosong");
            edtPass.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edtPass.setError("Minimal 6 karakter");
            edtPass.requestFocus();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            navigateBasedOnRole(user);
                        }
                    } else {
                        Toast.makeText(this, "Login Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateBasedOnRole(FirebaseUser user) {
        String email = user.getEmail();
        Intent intent = ADMIN_EMAIL.equals(email)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, MenuActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showPasswordInitially(EditText editText) {
        if (editText == null) return;
        editText.setTransformationMethod(null);
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.eye, 0);
        editText.setTag("password_shown");
    }

    private void setupPasswordToggle(EditText editText) {
        if (editText == null) return;
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