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
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
//import com.google.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
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

        Intent i = getIntent();

        String sFrom = i.getStringExtra("from");
        String destinationName = i.getStringExtra("destination");

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

            Log.d(MainActivity.TAG, "MAP READY: " + mapReady);


            Log.d(MainActivity.TAG, getEndLocationTitle(result));

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

        Log.d(MainActivity.TAG, "FROM: " + sFrom);
        Log.d(MainActivity.TAG, "DESTIATION: " + destinationName);
    }

    private void addMarkersToMap(DirectionsResult results) {
        LatLng start =
                new LatLng(
                        results.routes[0].legs[0].startLocation.lat,
                        results.routes[0].legs[0].startLocation.lng);

        MarkerOptions startMarker =
                new MarkerOptions()
                        .position(start)
                        .title("Start");

        map.addMarker(startMarker);


        //        map.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].endLocation.lat,results.routes[0].legs[0].endLocation.lng)).title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void getDirections(String from, String destination) {

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
        Log.d(MainActivity.TAG, "MAP READY");
    }
}
