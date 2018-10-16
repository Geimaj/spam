package com.example.jamie.spam;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;

//import
import com.google.android.gms.maps.model.LatLng;

import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Place from;
    private Place destination;

    private MapFragment mapFragment;
    private GoogleMap map;

    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

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

    private void getDirections() {
        Intent i = getIntent();

        String sFrom = i.getStringExtra("from");
        String destinationName = i.getStringExtra("destination");

        Log.d(MainActivity.TAG, "directions FROM: " + sFrom);
        Log.d(MainActivity.TAG, "directions TO: " + destinationName);


        DateTime now = new DateTime();

        try {
            DirectionsResult result =
                    DirectionsApi.newRequest(getGeoContext())
                            .mode(TravelMode.DRIVING)
                            .origin(sFrom)
                            .destination(destinationName)
                            .departureTime(now)
                            .await();

            addMarkersToMap(result);

            drawRoute(result);

//            Log.d(MainActivity.TAG, getEndLocationTitle(result));

        } catch (ApiException e) {
            e.printStackTrace();
            Log.d(MainActivity.TAG, "API ERROR");
        } catch (InterruptedException e) {
            Log.d(MainActivity.TAG, "INTERRUPTED");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "IO E");
            e.printStackTrace();
        }

    }

    private void drawRoute(DirectionsResult result) {
        Log.d(MainActivity.TAG, "Drawing Polyline");

        if(mapReady){
            //create polyLineOptions to add to map
            PolylineOptions polylineOptions = new PolylineOptions();

            //loop through each step
            for (DirectionsStep step : result.routes[0].legs[0].steps) {

                //get polyLine for this step
                EncodedPolyline encodedPolyline= step.polyline;

                //decode polyLine
                List<com.google.maps.model.LatLng> points = encodedPolyline.decodePath();

                //loop through decoded polyLine
                for(int i = 0; i < points.size(); i++){
                    com.google.maps.model.LatLng latLng = points.get(i);

                    polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                }

            }

            map.addPolyline(polylineOptions);
        }

    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_directions_api_key))
                .setConnectTimeout(25, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;

        getDirections();

    }
}
