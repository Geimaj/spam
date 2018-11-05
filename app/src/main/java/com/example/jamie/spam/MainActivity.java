package com.example.jamie.spam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.model.TravelMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "DEBUGZ";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int RC_SIGN_IN = 1;
    private static final int RC_PLACE_PICKER_DESTINATION = 2;
    private static final int RC_PLACE_PICKER_ORIGIN = 3;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private boolean mLocationPermissionGranted = false;

    private Place from;
    private Place destination;

    private TravelMode travelMode = TravelMode.DRIVING;

    private EditText etDestination;
    private EditText etFrom;
    private MapFragment mapFragment;
    private Button btnCar;
    private Button btnPublic;
    private Button btnBike;
    private Button btnWalk;
    private FloatingActionButton fabDirections;
    private FloatingActionButton fabPreview;

    private LinearLayout detailsLayout;

    private GoogleMap map;
    private boolean mapReady = false;

    private Directions directions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        getLocationPermission();

        //setup auth

        //init FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
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
        etFrom = (EditText) findViewById(R.id.etFrom);
        etDestination = (EditText) findViewById(R.id.etDestination);

        etDestination.setInputType(InputType.TYPE_NULL);
        etFrom.setInputType(InputType.TYPE_NULL);

        etFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPlace(RC_PLACE_PICKER_ORIGIN);
            }
        });

        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPlace(RC_PLACE_PICKER_DESTINATION);
            }
        });

        fabDirections = (FloatingActionButton) findViewById(R.id.fabGetDirections);

        fabDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (destination == null) {
                    Toast.makeText(MainActivity.this, "Select a destination", Toast.LENGTH_LONG).show();
                } else {


                    if (mLocationPermissionGranted) {
                        //next activity
                        startDirectionsProcedure(from, destination);
                    } else {
                        getLocationPermission();
                    }
                }

            }
        });

        detailsLayout = (LinearLayout) findViewById(R.id.detailsLayout);

        FloatingActionButton fabLocation = (FloatingActionButton) findViewById(R.id.fabLocation);

        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationPermissionGranted) {
                    Log.d(TAG, "GOT PERMS");
                } else {
                    getLocationPermission();
                }
            }
        });

        fabPreview = (FloatingActionButton) findViewById(R.id.fabPreview);
        fabPreview.setOnClickListener(fabPreviewListener);

        btnCar = (Button) findViewById(R.id.btnCar);
        btnPublic = (Button) findViewById(R.id.btnTrain);
        btnBike = (Button) findViewById(R.id.btnBike);
        btnWalk = (Button) findViewById(R.id.btnWalk);

        btnCar.setOnClickListener(btnCarClickListener);
        btnPublic.setOnClickListener(btnPublicClickListener);
        btnBike.setOnClickListener(btnBikeClickListener);
        btnWalk.setOnClickListener(btnWalkClickListener);

        if (isServicesVersionCorrect()) {
            init();
        }


    }

    // Click listener for car button
    View.OnClickListener btnCarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            travelMode = TravelMode.DRIVING;
            fabDirections.setImageResource(R.drawable.ic_car);
            startDirectionsProcedure(from, destination);
            detailsLayout.setVisibility(View.VISIBLE);
        }
    };

    // Click listener for  public button
    View.OnClickListener btnPublicClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            travelMode = TravelMode.TRANSIT;
            fabDirections.setImageResource(R.drawable.ic_train);
            startDirectionsProcedure(from, destination);
            detailsLayout.setVisibility(View.VISIBLE);

        }
    };

    // Click listener for bike button
    View.OnClickListener btnBikeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            travelMode = TravelMode.BICYCLING;
            fabDirections.setImageResource(R.drawable.ic_bike);
            startDirectionsProcedure(from, destination);
            detailsLayout.setVisibility(View.VISIBLE);

        }
    };

    // Click listener for walk button
    View.OnClickListener btnWalkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            travelMode = TravelMode.WALKING;
            fabDirections.setImageResource(R.drawable.ic_walk);
            startDirectionsProcedure(from, destination);
            detailsLayout.setVisibility(View.VISIBLE);

        }
    };

    //click listener for preview
    View.OnClickListener fabPreviewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            directions.showPreview();
        }
    };

    private void getCurrentLocation() {

//        @SuppressLint("MissingPermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            getLocationPermission();
            return;
        }
        Task<PlaceLikelihoodBufferResponse> placeResult =
                mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                if(likelyPlaces.get(0) != null){
                    Place p = likelyPlaces.get(0).getPlace();

                    //add marker to current location
                    addMarker(p);

                    likelyPlaces.release();
                }

                likelyPlaces.release();
            }
        });

    }

    private void addMarker(Place place){
        if(place != null) {

            MarkerOptions marker
                    = new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName().toString());


            map.addMarker(marker);
        }
    }

    private void startDirectionsProcedure(Place from, Place destination) {
        if(from == null){
            if(mLocationPermissionGranted){
                //get current location
                getCurrentLocation();
            } else {
                getLocationPermission();
            }
            return;
        }

        if(destination == null){
            Toast.makeText(getApplicationContext(), "Select a destination", Toast.LENGTH_LONG).show();
            return;
        }


        TripData tripData = new TripData(mAuth.getCurrentUser().getUid(), travelMode, from, destination);

        directions.drawRoutes(tripData);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    private void pickPlace(int PLACE_PICKER_REQUEST) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if the result is coming from the sign in
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Signed in", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Signed in canceled", Toast.LENGTH_LONG).show();
                finish();
            }

        } else if (requestCode == RC_PLACE_PICKER_DESTINATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                destination = place;
                etDestination.setText(place.getAddress());

                map.clear();
                addMarker(from);
                addMarker(destination);

                Log.d(TAG, "Chosen: " + destination.getName());

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Place pick canceled");
                etDestination.setText("");
            }

            fabDirections.requestFocus();
        } else if (requestCode == RC_PLACE_PICKER_ORIGIN) {
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data, this);
                from = place;
                etFrom.setText(place.getAddress());

                map.clear();
                addMarker(from);


            } else if(resultCode == RESULT_CANCELED){
                Log.d(TAG, "Place pick canceled");
            }

            fabDirections.requestFocus();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void onSignedInInitialized() {
//        Toast.makeText(MainActivity.this, "Signed init", Toast.LENGTH_LONG).show();
    }

    private void onSignedOutCleanup() {
//        Toast.makeText(MainActivity.this, "Signed OUT", Toast.LENGTH_LONG).show();

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

    private void init() {
        Log.d(TAG, "initializing activity...");
        getCurrentLocation();




    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;


        //fetch our colors corresponding to the travel mode
        HashMap<TravelMode, Integer> travelColors = new HashMap<>();
        travelColors.put(TravelMode.DRIVING, getColor(R.color.color_car));
        travelColors.put(TravelMode.TRANSIT, getColor(R.color.color_public));
        travelColors.put(TravelMode.BICYCLING, getColor(R.color.color_bike));
        travelColors.put(TravelMode.WALKING, getColor(R.color.color_walk));

        String apiKey = getString(R.string.google_directions_api_key);

        TextView detials = (TextView) findViewById(R.id.etTripDetails);

        directions = new Directions(detials,getApplicationContext(), apiKey, map, travelColors);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Toast.makeText(getApplicationContext(), "KEYDOWN", Toast.LENGTH_SHORT);

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            //Do something
            directions.decreaseSpeed();

            Toast.makeText(getApplicationContext(), "" + directions.getSpeed(), Toast.LENGTH_SHORT);
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            directions.increaseSpeed();
            Toast.makeText(getApplicationContext(), "" + directions.getSpeed(), Toast.LENGTH_SHORT);

        }

        return true;
    }
}
