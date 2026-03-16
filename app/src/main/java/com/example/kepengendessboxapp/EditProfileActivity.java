// EditProfileActivity.java Berhasil
package com.example.kepengendessboxapp;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText edtNamaLengkap, edtNoWa, edtEmail, edtCurrentPass, edtNewPass;
    private Button btnSimpan;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inisialisasi View
        edtNamaLengkap = findViewById(R.id.edtNamaLengkap);
        edtNoWa = findViewById(R.id.edtNoWa);
        edtEmail = findViewById(R.id.edtEmail);
        edtCurrentPass = findViewById(R.id.edtCurrentPass);
        edtNewPass = findViewById(R.id.edtNewPass);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load data dari Firestore
        loadUserData();

        //  Setup toggle password
        setupPasswordToggle(edtCurrentPass);
        setupPasswordToggle(edtNewPass);

        // Tampilkan password secara default
        showPasswordInitially(edtCurrentPass);
        showPasswordInitially(edtNewPass);

        // Simpan perubahan
        btnSimpan.setOnClickListener(v -> simpanPerubahan());
    }

    private void loadUserData() {
        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtNamaLengkap.setText(doc.getString("NamaLengkap"));
                        edtNoWa.setText(doc.getString("NoWhatsapp"));
                        edtEmail.setText(user.getEmail()); // Hanya tampilkan
                        edtEmail.setEnabled(false); // ❌ Nonaktifkan edit
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                });
    }

    //  Tampilkan password secara default
    private void showPasswordInitially(TextInputEditText editText) {
        editText.setTransformationMethod(null); // Tampilkan karakter
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.baseline_lock_24, 0, R.drawable.eye, 0);
        editText.setTag("password_shown");
    }

    //  Toggle show/hide password
    private void setupPasswordToggle(TextInputEditText editText) {
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    boolean isShown = "password_shown".equals(editText.getTag());

                    if (isShown) {
                        // 🔴 Sembunyikan password
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.eye_off, 0);
                        editText.setTag("password_hidden");
                    } else {
                        // 🟢 Tampilkan password
                        editText.setTransformationMethod(null);
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.eye, 0);
                        editText.setTag("password_shown");
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void simpanPerubahan() {
        String nama = edtNamaLengkap.getText().toString().trim();
        String noWa = edtNoWa.getText().toString().trim();
        String currentPass = edtCurrentPass.getText().toString().trim();
        String newPass = edtNewPass.getText().toString().trim();

        boolean adaPerubahan = false;
        boolean perluReauth = false;

        // 🔹 1. Cek apakah ada perubahan data
        if (!nama.isEmpty()) adaPerubahan = true;
        if (!noWa.isEmpty()) adaPerubahan = true;
        if (!newPass.isEmpty()) {
            adaPerubahan = true;
            perluReauth = true;
        }

        if (!currentPass.isEmpty()) {
            perluReauth = true;
        }

        if (!adaPerubahan) {
            Toast.makeText(this, "Tidak ada perubahan", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 2. Validasi password baru (minimal 8 karakter)
        if (!newPass.isEmpty() && newPass.length() < 8) {
            Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 3. Jika perlu re-auth (ubah password), wajib isi password saat ini
        if (perluReauth && currentPass.isEmpty()) {
            Toast.makeText(this, "Password saat ini wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 4. Re-authenticate user jika perlu
        if (perluReauth) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> prosesUpdateData(nama, noWa, newPass))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Password saat ini salah", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Jika hanya ubah nama/no WA, langsung update
            prosesUpdateData(nama, noWa, newPass);
        }
    }

    private void prosesUpdateData(String nama, String noWa, String newPass) {
        Map<String, Object> dataFirestore = new HashMap<>();
        boolean perluUpdateFirestore = false;

        // 🔹 Update Nama Lengkap jika diisi
        if (!nama.isEmpty()) {
            dataFirestore.put("NamaLengkap", nama);
            perluUpdateFirestore = true;
        }

        // 🔹 Update No. WhatsApp jika diisi
        if (!noWa.isEmpty()) {
            dataFirestore.put("NoWhatsapp", noWa);
            perluUpdateFirestore = true;
        }

        // 🔹 Update Firestore jika ada perubahan
        if (perluUpdateFirestore) {
            firestore.collection("users")
                    .document(user.getUid())
                    .update(dataFirestore)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal update profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // 🔹 Update Password jika diisi
        if (!newPass.isEmpty()) {
            user.updatePassword(newPass)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal ubah password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // 🔹 Kembali ke profil setelah 1 detik
        new Handler().postDelayed(() -> finish(), 1000);
    }
}