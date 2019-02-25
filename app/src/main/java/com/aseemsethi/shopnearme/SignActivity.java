package com.aseemsethi.shopnearme;
// A few code samples taken from http://www.androiddeft.com/2018/01/28/android-login-with-google-account

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SignActivity extends AppCompatActivity {
    private static final String TAG = "SignMainActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db;
    // The following variables are for Establishments
    Spinner spinnerDropDown;
    String ea = null;
    List<String> eaList = new ArrayList<String>();
    String[] eaArray;
    ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.signin);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        Button signOutButton = findViewById(R.id.sign_out_button);
        pDialog = new ProgressDialog(SignActivity.this);

        signInButton.setSize(SignInButton.SIZE_WIDE);// wide button style

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eaList.add("Aseem Shop");
        eaList.add("Register New Shop");
        Log.v(TAG, "List size" + eaList.size());
        addEstablishments();
        Log.v(TAG, "List size" + eaList.size());
        eaArray = new String[eaList.size()];
        eaArray = eaList.toArray(eaArray);

        // Configure Google Sign In
        // default_web_client_id is from https://console.developers.google.com/apis/credentials
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        Button continue_admin = (Button)findViewById(R.id.continue_admin);
        continue_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = mAuth.getCurrentUser();
                //DocumentReference docRef = db.collection(ea).document(user.getDisplayName());
                final String cp = "Establishments";
                DocumentReference docRef = db.collection(cp).document(ea);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "Admin User: " + document.getData());
                                if ((document.getData().get("name")).equals(user.getDisplayName())) {
                                    if ((document.getData().get("admin").equals("yes"))) {
                                        Log.d(TAG, "User is an admin");
                                        Intent i = new Intent(SignActivity.this, MainActivity.class);
                                        i.putExtra("user", user.getDisplayName());
                                        i.putExtra("ea", ea);
                                        Log.v(TAG, "Starting Admin Activity");
                                        startActivity(i);
                                    } else
                                        Log.d(TAG, "User is not an admin");
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

        spinnerDropDown = (Spinner) findViewById(R.id.spinner1);
        spinnerAdapter = new ArrayAdapter<String>(this, android.
                R.layout.simple_spinner_item, eaArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDropDown.setAdapter(spinnerAdapter);

        spinnerDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                 int position, long id) {
                // Get select item
                int sid = spinnerDropDown.getSelectedItemPosition();
                Toast.makeText(getBaseContext(), eaArray[sid],
                        Toast.LENGTH_SHORT).show();
                ea = eaArray[sid];
                final String cp = "Establishments";
                DocumentReference docRef = db.collection(cp).document(ea);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                if ((document.getData().get("Address")) != null) {
                                    String address = (String) document.getData().get("Address");
                                    Log.v(TAG, "Address: " + address);
                                    TextView t = findViewById(R.id.contactT);
                                    t.setTypeface(Typeface.DEFAULT_BOLD);
                                    t.setText("Establishment Address: \n\n");
                                    t.append(address);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void addEstablishments() {
        db.collection("Establishments").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Adding Establishment: " + document.getId() + " => " + document.getData());
                                eaList.add(document.getId());
                                eaArray = new String[eaList.size()];
                                eaArray = eaList.toArray(eaArray);
                                spinnerDropDown = (Spinner) findViewById(R.id.spinner1);
                                spinnerAdapter = new ArrayAdapter<String>(SignActivity.this, android.
                                        R.layout.simple_spinner_item, eaArray);
                                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerDropDown.setAdapter(spinnerAdapter);
                            }
                        } else {
                            Log.w(TAG, "Error getting Establishments.", task.getException());
                        }
                    }
                });
    }
    /**
     * Display Progress bar while Logging in
     */

    private void displayProgressDialog() {
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.w(TAG, "Google sign in passed");
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        displayProgressDialog();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.v(TAG, "Name: " + user.getDisplayName() + " : " + user.getUid());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed: ", Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }

                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        Log.w(TAG, "UpdateUI: Google sign in passed");
        TextView displayName = findViewById(R.id.displayName);
        ImageView profileImage = findViewById(R.id.profilePic);
        if (user != null) {
            displayName.setText(user.getDisplayName());
            displayName.setVisibility(View.VISIBLE);
            // Loading profile image
            Uri profilePicUrl = user.getPhotoUrl();
            if (profilePicUrl != null) {
                Glide.with(this).load(profilePicUrl)
                        .into(profileImage);
                Log.w(TAG, "UpdateUI: Profile pic is OK");
            } else
                Log.w(TAG, "UpdateUI: Profile pic is null");
            //profileImage.requestLayout(cen);
            profileImage.setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.continue_admin).setVisibility(View.VISIBLE);
            findViewById(R.id.textView1).setVisibility(View.VISIBLE);
            findViewById(R.id.spinner1).setVisibility(View.VISIBLE);
            findViewById(R.id.contactT).setVisibility(View.VISIBLE);
            Log.w(TAG, "UpdateUI: update name and pic");
        } else {
            displayName.setVisibility(View.GONE);
            profileImage.setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.continue_admin).setVisibility(View.GONE);
            findViewById(R.id.textView1).setVisibility(View.GONE);
            findViewById(R.id.spinner1).setVisibility(View.GONE);
            findViewById(R.id.contactT).setVisibility(View.GONE);
            Log.w(TAG, "UpdateUI: update name and pic - failed");
        }
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

}