package com.example.androidchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.androidchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private String email, password;

    private void initUI() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_login);

        initUI();
    }

    public void btnLoginClick(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (email.equals("")) {
            etEmail.setError(getString(R.string.email_required));
            return;
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
            return;
        } else {
            // Connection à Firebase
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Rien faire

                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}