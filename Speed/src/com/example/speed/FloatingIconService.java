package com.example.speed;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingIconService extends Service implements LocationListener {
	private static final String TAG = "FloatingIconService";
	private WindowManager windowManager;
	private TextView floatingSpeedText;

	private static int x = 0;
	private static int y = 0;

	public LocationManager locationManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;

		if (FloatingIconService.x != 0 || FloatingIconService.y != 0) {
			params.x = x;
			params.y = y;
		} else {
			params.x = 0;
			params.y = 200;
		}
		floatingSpeedText = new TextView(this);
		floatingSpeedText.setText(Html
				.fromHtml("<font color='#0000FF'>0</font>"));
		floatingSpeedText.setTextSize(80);

		windowManager.addView(floatingSpeedText, params);

		try {
			floatingSpeedText.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.performClick();

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						return true;
					case MotionEvent.ACTION_UP:
						return true;
					case MotionEvent.ACTION_MOVE:
						paramsF.x = initialX
								+ (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY
								+ (int) (event.getRawY() - initialTouchY);
						FloatingIconService.x = paramsF.x;
						FloatingIconService.y = paramsF.y;
						windowManager.updateViewLayout(floatingSpeedText,
								paramsF);
						return true;
					}
					return false;
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "exceptioin in setting up floating image", e);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 1, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, this);
	}

	@Override
	public void onDestroy() {
		if (floatingSpeedText != null)
			windowManager.removeView(floatingSpeedText);
		floatingSpeedText = null;
		if (locationManager != null)
			locationManager.removeUpdates(this);
		locationManager = null;
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		float speed = location.getSpeed();
		int speedInMiles = (int) (speed * 3600 / 1609.34 + 0.5);
		if (this.floatingSpeedText != null) {
			this.floatingSpeedText.setText(Html
					.fromHtml("<font color='#0000FF'>"
							+ String.valueOf(speedInMiles) + "</font>"));
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast toast = Toast.makeText(getApplicationContext(),
				"provider enabled", Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}
