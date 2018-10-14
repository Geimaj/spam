package com.example.jamie.spam;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.gson.Gson;

public class DirectionsActivity extends AppCompatActivity {

    private Place from;
    private Place destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Intent i = getIntent();

        String sFrom = i.getStringExtra("from");
        String destinationName = i.getStringExtra("destination");

//        from = (Place) new Gson().fromJson(sFrom, Place.class);
//        destination = new Gson().fromJson(sFrom, Place.class);

        Log.d(MainActivity.TAG, "FROM: " +  sFrom);
        Log.d(MainActivity.TAG, "DESTIATION: " +  destinationName);
    }
}
