package com.example.jamie.spam;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;

//import
import com.google.android.gms.maps.model.LatLng;

import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Place from;
    private Place destination;

    private MapFragment mapFragment;
    private TextView tvTripDetails;

    private GoogleMap map;

    private TripData tripData;

    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Intent i = getIntent();
        tripData = (TripData) i.getParcelableExtra("tripData");


        Log.d(MainActivity.TAG, "directions from " + tripData.getOriginAddress());
        Log.d(MainActivity.TAG, "directions to " + tripData.getDestintaionAddress());




        tvTripDetails = (TextView) findViewById(R.id.tvTripDetails);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.directionsMap);
        mapFragment.getMapAsync(this);

    }

    private void addMarkersToMap(DirectionsResult results) {
        if(results.routes.length > 0){
            LatLng start =
                    new LatLng(
                            results.routes[0].legs[0].startLocation.lat,
                            results.routes[0].legs[0].startLocation.lng);

            LatLng finish =
                    new LatLng(
                            results.routes[0].legs[0].endLocation.lat,
                            results.routes[0].legs[0].endLocation.lng);


            MarkerOptions startMarker =
                    new MarkerOptions()
                            .position(start)
                            .title("Start");

            MarkerOptions finishMarker =
                    new MarkerOptions()
                        .position(finish)
                        .title("finish");

            if(mapReady){
                map.addMarker(startMarker);
                map.addMarker(finishMarker);

            }
        } else {
            Log.d(MainActivity.TAG, "NO ROUTES FOUND");
        }

    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }


    private void saveTrip(TripData tripData) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("trip");

        myRef.push().setValue(tripData);

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;

//        getDirections();

    }
}
