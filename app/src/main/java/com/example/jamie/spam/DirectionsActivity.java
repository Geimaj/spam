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
//
//    private void getDirections() {
//        DateTime now = new DateTime();
//
//        try {
//            DirectionsResult result =
//                    DirectionsApi.newRequest(getGeoContext())
//                            .mode(tripData.getTravelMode())
//                            .origin(tripData.getOriginAddress())
//                            .destination(tripData.getDestintaionAddress())
//                            .departureTime(now)
//                            .await();
//
//            addMarkersToMap(result);
//
//            if(result.routes.length > 0){
//
//
//                tvTripDetails.setText(tripData.getTravelMode().toString() + "\n" + getEndLocationTitle(result));
//
//                drawRoute(result);
//
//                com.google.maps.model.LatLng start = result.routes[0].legs[0].startLocation;
//                com.google.maps.model.LatLng end = result.routes[0].legs[0].endLocation;
//
//                LatLng center;
//
//    //            LatLngBounds area;
//    //            try{
//    //                area = new LatLngBounds(new LatLng(end.lat, end.lng), new LatLng(start.lat, start.lng));
//    //            } catch (IllegalArgumentException e){
//    //                area = new LatLngBounds(new LatLng(start.lat, start.lng), new LatLng(end.lat, end.lng));
//    //            }
//
//                center = new LatLng(start.lat, start.lng);
//
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
//
//                saveTrip(tripData);
//            } else {
//                finish();
//                Toast.makeText(getApplicationContext(), "No route found", Toast.LENGTH_LONG).show();
//
//            }
//
//        } catch (ApiException e) {
//            e.printStackTrace();
//            Log.d(MainActivity.TAG, "API ERROR");
//        } catch (InterruptedException e) {
//            Log.d(MainActivity.TAG, "INTERRUPTED");
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.d(MainActivity.TAG, "IO E");
//            e.printStackTrace();
//        }
//
//    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;

//        getDirections();

    }
}
