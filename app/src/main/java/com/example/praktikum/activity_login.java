package com.example.praktikum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class activity_login extends Activity {
    private FirebaseAuth auth;
    private EditText inputEmail, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();

            if (email.isEmpty() || password.length() < 6) {
                pesan("Isi email dan password minimal 6 karakter");
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) bukaMain();
                        else pesan("Register gagal: " + task.getException().getMessage());
                    });
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                pesan("Email dan password wajib diisi");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) bukaMain();
                        else pesan("Login gagal: " + task.getException().getMessage());
                    });
        });

        findViewById(R.id.btnReset).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            if (email.isEmpty()) {
                pesan("Isi email terlebih dahulu");
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, task ->
                            pesan(task.isSuccessful()
                                    ? "Permintaan reset password dikirim"
                                    : "Gagal: " + task.getException().getMessage())
                    );
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) bukaMain();
    }

    private void bukaMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void pesan(String teks) {
        Toast.makeText(this, teks, Toast.LENGTH_LONG).show();
    }
}