package com.example.jamie.spam;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
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

public class Directions implements GoogleMap.CancelableCallback{

    private HashMap<TravelMode, Integer> travelColors;
    private GoogleMap map;
    private String API_KEY;
    private Context context;
    private TextView details;

    private DirectionsRoute lastDrawnRoute;

    private int animationStep;
    private List<LatLng> points;
    private int targetSpeed = 1000;
    private double scaleFactor = 0.9;

    public Directions(TextView details, Context context, String apiKey, GoogleMap map, HashMap<TravelMode, Integer> travelColors) {
        this.travelColors = travelColors;
        this.map = map;
        this.API_KEY = apiKey;
        this.context = context;
        this.details = details;

        lastDrawnRoute = null;
    }

    public void showPreview(){
        if(lastDrawnRoute != null){

            //zoom in on start
            centerCamera(lastDrawnRoute, 18);


            List<LatLng> points = lastDrawnRoute.overviewPolyline.decodePath();
            performAnimation(points);

        }
    }

    private double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double dLat = radians(lat2-lat1);
        double dLon = radians(lon2-lon1);

        lat1 = radians(lat1);
        lat2 = radians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadiusKm * c;
    }


    //starts animation
    private void performAnimation(List<LatLng> points){
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setBuildingsEnabled(true);
        this.animationStep = 0;
        this.points = points;
        animationStep(0, points);
    }

    //will get called on each step of the animation
    //calls aim camera
    private void animationStep(int progress, List<LatLng> points){
        if(progress >= points.size()){
            return;
        }

        LatLng lastPoint = null;

        if(progress == 0){
            lastPoint = points.get(0);
        } else {
            lastPoint = points.get(progress-1);
        }

        LatLng point = points.get(progress);

        float bearing = getBearing(lastPoint, point);

        double distance = distanceInKmBetweenEarthCoordinates(point.lat, point.lng, lastPoint.lat, lastPoint.lng);

        //how much should we scale the speed according to the distance
        double scale = scaleFactor * distance;

        double speed = targetSpeed - scale;

        Log.d(MainActivity.TAG, "Step: " + progress + " distance: " + distance);
        Log.d(MainActivity.TAG, "Speed: " + speed);

        aimCamera(point, bearing, (int)speed);
    }

    //performs animation
    //then calls onFinish() when done
    private void aimCamera(LatLng latlng, float bearing, int speed){
        com.google.android.gms.maps.model.LatLng properLatLng = new com.google.android.gms.maps.model.LatLng(latlng.lat, latlng.lng);
        CameraPosition start =
                new CameraPosition.Builder().target(properLatLng)
                        .zoom(18)
                        .bearing(bearing)
                        .tilt(12)
                        .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(start), speed, this);

    }

    private float getBearing(LatLng l1, LatLng l2){
        return (float) angleFromCoordinate(l1.lat, l1.lng, l2.lat, l2.lng);
    }

    private double radians(double n) {
        return n * (Math.PI / 180);
    }
    private double degrees(double n) {
        return n * (180 / Math.PI);
    }

    private double angleFromCoordinate(double startLat, double startLong, double endLat, double endLong){
        startLat = radians(startLat);
        startLong = radians(startLong);
        endLat = radians(endLat);
        endLong = radians(endLong);

        double dLong = endLong - startLong;

        double dPhi = Math.log(Math.tan(endLat/2.0+Math.PI/4.0)/Math.tan(startLat/2.0+Math.PI/4.0));
        if (Math.abs(dLong) > Math.PI){
            if (dLong > 0.0)
                dLong = -(2.0 * Math.PI - dLong);
            else
                dLong = (2.0 * Math.PI + dLong);
        }

        return (degrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
    }

    private void addMarker(LatLng latlng){

        if(latlng != null) {
            com.google.android.gms.maps.model.LatLng properLatLng = new com.google.android.gms.maps.model.LatLng(latlng.lat, latlng.lng);

            MarkerOptions marker
                    = new MarkerOptions()
                    .position(properLatLng);

            map.addMarker(marker);
        }
    }

    private void drawRoute(DirectionsRoute route) {
        lastDrawnRoute = route;

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
        centerCamera(route, 15);

        //update details label
        details.setText(getEndLocationTitle(route));
    }

    private void centerCamera(DirectionsRoute route, int zoom){

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

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
    }

    private String getEndLocationTitle(DirectionsRoute route) {
        return "Time :" + route.legs[0].duration.humanReadable + " Distance :" + route.legs[0].distance.humanReadable;
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

    //will fire after each camera animation is complete
    @Override
    public void onFinish() {
        animationStep(animationStep++, points);
    }

    @Override
    public void onCancel() {

    }


    //bigger time means longer animation = slower speed
    public void decreaseSpeed() {
        if(this.targetSpeed <= 2000){
            targetSpeed++;
        }
    }

    //smaller time = faster animation = faster speed
    public void increaseSpeed() {
        if(this.targetSpeed >= 100){
            targetSpeed--;
        }
    }

    public int getSpeed(){
        return this.targetSpeed;
    }
}
