package com.example.speed;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button speedOnlyButton = (Button) findViewById(R.id.speedOnlyButton);
        speedOnlyButton.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            startService(new Intent(MainActivity.this, FloatingIconService.class));
          }
        });

        Button mapAndSpeedButton = (Button) findViewById(R.id.mapAndSpeedButton);
        mapAndSpeedButton.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            String currentLocation = "Current Location";
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%s",location.getLatitude(), location.getLongitude(), currentLocation);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            startService(new Intent(MainActivity.this, FloatingIconService.class));
          }
        });
    }
    
    @Override
    public void onDestroy() {
    	stopService(new Intent(MainActivity.this, FloatingIconService.class));
    	super.onDestroy();
    }
}
