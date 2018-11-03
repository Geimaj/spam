package com.example.jamie.spam;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Directions {

    private HashMap<TravelMode, Integer> travelColors;
    private GoogleMap map;
    private String API_KEY;
    private Context context;

    public Directions(Context context, String apiKey, GoogleMap map, HashMap<TravelMode, Integer> travelColors) {
        this.travelColors = travelColors;
        this.map = map;
        this.API_KEY = apiKey;
        this.context = context;
    }

    private void drawRoute(DirectionsRoute route) {
        Log.d(MainActivity.TAG, "Drawing Polyline");

        //create polyLineOptions to add to map
        PolylineOptions polylineOptions = new PolylineOptions();

        //loop through each step
        for (DirectionsStep step : route.legs[0].steps) {

            //get polyLine for this step
            EncodedPolyline encodedPolyline = step.polyline;

            //decode polyLine
            List<LatLng> points = encodedPolyline.decodePath();

            //loop through decoded polyLine
            for (int i = 0; i < points.size(); i++) {
                com.google.maps.model.LatLng latLng = points.get(i);

                polylineOptions.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
            }

        }

        polylineOptions.color(travelColors.get(route.legs[0].steps[0].travelMode));

        map.addPolyline(polylineOptions);

        //center camera
        centerCamera(route);
    }

    private void centerCamera(DirectionsRoute route){

            com.google.maps.model.LatLng start = route.legs[0].startLocation;
            com.google.maps.model.LatLng end = route.legs[0].endLocation;

            com.google.android.gms.maps.model.LatLng center;

//            center camera
//            LatLngBounds area;
//            try{
//                area = new LatLngBounds(new LatLng(end.lat, end.lng), new LatLng(start.lat, start.lng));
//            } catch (IllegalArgumentException e){
//                area = new LatLngBounds(new LatLng(start.lat, start.lng), new LatLng(end.lat, end.lng));
//            }

            center = new com.google.android.gms.maps.model.LatLng(start.lat, start.lng);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
    }


    //    public interface for drawing routes for all transport types on the given map
    public void drawRoutes(TripData tripData) {
        Log.d(MainActivity.TAG, "looking for the d...");

        DirectionsResult directionsResult = getDirections(tripData);

        if (directionsResult.routes.length <= 0) {
            Toast.makeText(context, "No routes found", Toast.LENGTH_LONG).show();
            Log.d(MainActivity.TAG, "NO ROUTES FOUND!!!");
            Log.d(MainActivity.TAG, "from: " + tripData.getOriginAddress());
            Log.d(MainActivity.TAG, "destination: " + tripData.getDestintaionAddress());

            return;
        }


        map.clear();

        addMarkersToMap(directionsResult);

        //loop through each route and draw on map
        Gson gson = new Gson();

//            TravelMode routeTravelMode = result.routes[0].legs[0].steps[0].travelMode;

//            Log.d(MainActivity.TAG, "reslut found for: " + result.routes[0].legs[0].steps[0].travelMode);
//            Log.d(MainActivity.TAG, gson.toJson(result));

        drawRoute(directionsResult.routes[0]);


    }

    private DirectionsResult getDirections(TripData tripData) {
        DateTime now = new DateTime();
        Log.d(MainActivity.TAG, "looking for the d...");


        DirectionsResult results = null;

        try {

            results =
                    DirectionsApi.newRequest(getGeoContext())
                            .mode(tripData.getTravelMode())
                            .origin(tripData.getOriginAddress())
                            .destination(tripData.getDestintaionAddress())
                            .departureTime(now)
                            .await();

//            return (DirectionsResult[]) results.toArray();


//            drawRoute(result, tripData);
//

//            saveTrip(tripData);

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

        return results;

    }


    private void addMarkersToMap(DirectionsResult results) {
        if(results.routes.length > 0){
            com.google.android.gms.maps.model.LatLng start =
                    new com.google.android.gms.maps.model.LatLng(
                            results.routes[0].legs[0].startLocation.lat,
                            results.routes[0].legs[0].startLocation.lng);

            com.google.android.gms.maps.model.LatLng finish =
                    new com.google.android.gms.maps.model.LatLng(
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

                map.addMarker(startMarker);
                map.addMarker(finishMarker);

        } else {
            Log.d(MainActivity.TAG, "NO ROUTES FOUND");
        }

    }


    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(API_KEY)
                .setConnectTimeout(40, TimeUnit.SECONDS)
                .setReadTimeout(20, TimeUnit.SECONDS)
                .setWriteTimeout(20, TimeUnit.SECONDS);
    }

}
