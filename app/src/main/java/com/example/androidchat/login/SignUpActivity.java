package com.example.androidchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.androidchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersCollectionReference = rootRef.collection("Users");
    FirebaseUser firebaseUser;
    //Firebase Realtime database
    private DatabaseReference databaseReference;




    private void initUI() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirmPassword);
    }

    public void btnSignUpClick(View view) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        User user = new User(name, email, password);

        if (name.equals("")) {
            etName.setError(getString(R.string.name_required));
            return;
        } else if (email.equals("")) {
            etEmail.setError(getString(R.string.email_required));
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.email_format_invalid));
            return;
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
            return;
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            return;
        }else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_confirm_password_not_equals));
            return;
        }else{
            // Connection à Firebase
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            // Création de l'utilisateur dans Authentication sevice de Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                        Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));

//                        String id = java.util.UUID.randomUUID().toString();
//                        user.id = firebaseUser.getUid();
//                        // Nouveau utilisateur Firesotre - Instancie le document représentant le nouveau USER
//                        /*IMPORTANT: Si l'ID est existant l'utilisateur existant sera mis à jour*/
//                        DocumentReference newUserRef = usersCollectionReference.document(user.id);
//
//                        newUserRef.set(user).addOnCompleteListener(userCreationTask -> {
//                            if (userCreationTask.isSuccessful()) {
//                                Log.d(TAG, "createUserInFirestore: Success");
//                                Toast.makeText(SignUpActivity.this, "User " + user.name + " has been successfully created !", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Log.d(TAG, "createUserInFirestore: Error");
//                            }
//                        });

                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.signup_failed) + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateNameOnly(){
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    String userId = firebaseUser.getUid();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        initUI();
    }
}