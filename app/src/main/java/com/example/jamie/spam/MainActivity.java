package com.example.jamie.spam;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUGZ";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup auth

        //init FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //signed in
                    onSignedInInitialized();
                } else {
                    //not signed in
                    //start sign in flow
                    onSignedOutCleanup();

                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());


                    startActivityForResult(
                            // Get an instance of AuthUI based on the default app
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        //diable edit texts
        final EditText etFrom = (EditText) findViewById(R.id.etFrom);
        EditText etDestination = (EditText) findViewById(R.id.etDestination);
        etDestination.setInputType(InputType.TYPE_NULL);
        etFrom.setInputType(InputType.TYPE_NULL);


        etFrom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        etDestination.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });



        if(isServicesVersionCorrect()){
            init();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if the result is coming from the sign in
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this, "Signed in", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this, "Signed in canceled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mAuthStateListener != null){
            mAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void onSignedInInitialized() {
        Toast.makeText(MainActivity.this, "Signed init", Toast.LENGTH_LONG).show();
    }

    private void onSignedOutCleanup() {
        Toast.makeText(MainActivity.this, "Signed OUT", Toast.LENGTH_LONG).show();

    }

    public boolean isServicesVersionCorrect() {
        Log.d(TAG, "checking google services version");

        int avaiable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (avaiable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(avaiable)) {
            Log.d(TAG, "resolveable VERSION ERROR");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, avaiable, ERROR_DIALOG_REQUEST);
        } else {
            Log.d(TAG, "API VERSION ERROR");
        }
            return false;
    }

    private void init(){

    }
}
