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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity {

	public static EditText text = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		text = (EditText) findViewById(R.id.speed_input);
		text.setText("25");

		initLimitSpinner();
		initSpeedOnlyButton();
		initMapAndSpeedButton();
	}

	private void initMapAndSpeedButton() {
		Button mapAndSpeedButton = (Button) findViewById(R.id.mapAndSpeedButton);
		mapAndSpeedButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String currentLocation = "Current Location";
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				String provider = locationManager.getBestProvider(criteria,
						false);
				Location location = locationManager
						.getLastKnownLocation(provider);

				String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%s",
						location.getLatitude(), location.getLongitude(),
						currentLocation);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
				startService(new Intent(MainActivity.this,
						FloatingIconService.class));
			}
		});
	}

	private void initSpeedOnlyButton() {
		Button speedOnlyButton = (Button) findViewById(R.id.speedOnlyButton);
		speedOnlyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startService(new Intent(MainActivity.this,
						FloatingIconService.class));
			}
		});
	}

	private void initLimitSpinner() {
		Spinner limitSpinner = (Spinner) findViewById(R.id.limitSpinner);
		String[] limits = new String[] { "35", "55", "77" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item_layout, limits);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		limitSpinner.setAdapter(adapter);
		limitSpinner.setSelection(limits.length - 1);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
