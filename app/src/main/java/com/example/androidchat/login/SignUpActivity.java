package com.example.androidchat.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidchat.R;
import com.example.androidchat.common.NodesNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;
    private ImageView ivAvatar;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersCollectionReference = rootRef.collection("Users");
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    //Firebase Realtime database
    private DatabaseReference databaseReference;

    // Path vers les images file
    private Uri localFileUri, serverFileUri;

    private void initUI() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirmPassword);
        ivAvatar = findViewById(R.id.iv_avatar);
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
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_confirm_password_not_equals));
            return;
        } else {
            // Connection à Firebase
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            // Création de l'utilisateur dans Authentication sevice de Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                        if (localFileUri != null) {
                            updateNameAndPhoto();
                        } else {
                            updateNameOnly();
                        }
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));

//                        Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));

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

    /**
     * Méthode MAJ nom dans Realtime DB
     */
    private void updateNameOnly() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseUser.getUid();
                    /** Connexion à Realtime **/
                    databaseReference = FirebaseDatabase // Création / récupération d'un nouveau noeud "Users"
                            .getInstance() // Instance de connexion
                            .getReference() // Cherche la référence désirée à partir de la racine de la Db
                            .child(NodesNames.USERS);

                    // Création HasMap pour la gestion des données
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(NodesNames.NAME, etName.getText().toString());
                    hashMap.put(NodesNames.EMAIL, etEmail.getText().toString());
                    hashMap.put(NodesNames.ONLINE, "true");
                    hashMap.put(NodesNames.AVATAR, "");

                    // Envoi des datas vers Realtime
                    databaseReference.child(userId) // Créer un noeud avec la valuer "userId" du user courant
                            .setValue(hashMap) // Set les valeurs du user dans Realtime
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();

                                        //Lancement de l'activité suivante
                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    } else {
                                        Toast.makeText(SignUpActivity.this, getString(R.string.user_creation_failed) + task.getException(), Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onComplete: Error user creation - Message" + task.getException());
                                    }
                                }
                            });
                } else {
                    // S'il y a un problème
                    Toast.makeText(SignUpActivity.this, R.string.user_update_failed, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: Error user updating - Message" + task.getException());
                }
            }
        });
    }

    /**
     * Méthode MAJ photo et contenu dans Realtime DB
     */
    private void updateNameAndPhoto() {
        //Renomme l'image avec "userId" et le type de fichier (ici JPG)
        String strFileName = firebaseUser.getUid() + ".jpg";

        // Créer une référence du storage avec le dossier le fichier
        final StorageReference fileRef = storageReference.child("avatars_user/" + strFileName);

        //Upload vers le storage
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(SignUpActivity.this, task -> {

                    if (task.isSuccessful()) {
                        //Récupère l'URL de l'avatar dans le storage
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        serverFileUri = uri;

                                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(etName.getText().toString().trim())
                                                .build();

                                        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    String userId = firebaseUser.getUid();
                                                    /** Connexion à Realtime **/
                                                    databaseReference = FirebaseDatabase // Création / récupération d'un nouveau noeud "Users"
                                                            .getInstance() // Instance de connexion
                                                            .getReference() // Cherche la référence désirée à partir de la racine de la Db
                                                            .child(NodesNames.USERS);

                                                    // Création HasMap pour la gestion des données
                                                    HashMap<String, String> hashMap = new HashMap<>();
                                                    hashMap.put(NodesNames.NAME, etName.getText().toString());
                                                    hashMap.put(NodesNames.EMAIL, etEmail.getText().toString());
                                                    hashMap.put(NodesNames.ONLINE, "true");
                                                    hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());

                                                    // Envoi des datas vers Realtime
                                                    databaseReference.child(userId) // Créer un noeud avec la valuer "userId" du user courant
                                                            .setValue(hashMap) // Set les valeurs du user dans Realtime
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();

                                                                        //Lancement de l'activité suivante
                                                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                                    } else {
                                                                        Toast.makeText(SignUpActivity.this, getString(R.string.user_creation_failed) + task.getException(), Toast.LENGTH_SHORT).show();
                                                                        Log.d(TAG, "onComplete: Error user creation - Message" + task.getException());
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    // S'il y a un problème
                                                    Toast.makeText(SignUpActivity.this, R.string.user_update_failed, Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onComplete: Error user updating - Message" + task.getException());
                                                }
                                            }
                                        });

                                    }
                                });
                    } else {
                        Log.d(TAG, "updateNameAndPhoto - onComplete: ERROR: " + task.getException());
                    }

                }).addOnFailureListener((Exception ex) -> {
            Log.d(TAG, "updateNameAndPhoto: ERROR: " + ex.toString());
        });
    }


    /**
     * Méthode de gestion de l'avatar
     *
     * @param view
     */
    public void pickImage(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) { // Check la permission
            // Check la permission de l'application d'accéder aux photos
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // Lance la fenêtre de sélection des images
            startActivityForResult(intent, 101);
        } else {
            //Lance une demande d'autorisation d'accès aux images du device
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    // Méthode de vérification de la permission

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check la permission de l'application d'accéder aux photos
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            } else {
                Toast.makeText(this, R.string.access_permission_is_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifications
        // Le requestCode est-il bon ?
        if (requestCode == 101) {
            // Sélection image OK, sinon resultCode == RESULT_CANCELED
            if (resultCode == RESULT_OK) {
                // Path complet vers l'image sur le terminal
                localFileUri = data.getData();
                // Affecte l'Uri à l'avatar
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        initUI();
    }
}