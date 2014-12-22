package com.example.speed;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FloatingIconService extends Service implements LocationListener {
	private static final String TAG = "FloatingIconService";
	private WindowManager windowManager = null;
	private TextView floatingSpeedText = null;
	private MediaPlayer player = null;
	private int speedLimit = 25;
	private Position pos = new Position();

	public LocationManager locationManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate() {
		super.onCreate();

		final WindowManager.LayoutParams params = setUpFloatingText();

		try {
			setLongClickListener();
			setTouchListener(params);
		} catch (Exception e) {
			Log.e(TAG, "exceptioin in setting up floating image", e);
		}
		this.player = MediaPlayer.create(this, R.raw.alert);
		this.player.setVolume(1.0f, 1.0f);
	}

	private void setTouchListener(final WindowManager.LayoutParams params) {
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
				case MotionEvent.ACTION_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					paramsF.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					paramsF.y = initialY
							+ (int) (event.getRawY() - initialTouchY);
					windowManager.updateViewLayout(floatingSpeedText, paramsF);
				}
				return false;
			}
		});
	}

	private void setLongClickListener() {
		floatingSpeedText
				.setOnLongClickListener(new View.OnLongClickListener() {

					@SuppressLint("NewApi")
					@Override
					public boolean onLongClick(View v) {
						PopupMenu popup = new PopupMenu(v.getContext(), v);
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.popup_menu, popup.getMenu());
						popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem item) {
								switch (item.getItemId()) {
								case R.id.close_btn_item:
									stopService(new Intent(
											getApplicationContext(),
											FloatingIconService.class));
									break;
								case R.id.save_location_item:
									Database db = new Database(
											getApplicationContext());
									db.addLocation(pos.x, pos.y);
									db.close();
									stopService(new Intent(
											getApplicationContext(),
											FloatingIconService.class));
									break;

								default:
									break;
								}
								return false;
							}
						});
						popup.show();

						return true;
					}
				});
	}

	private WindowManager.LayoutParams setUpFloatingText() {
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 200;

		floatingSpeedText = new TextView(this);
		floatingSpeedText.setText(Html
				.fromHtml("<font color='#0000FF'>0</font>"));
		floatingSpeedText.setTextSize(80);

		windowManager.addView(floatingSpeedText, params);
		return params;
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
		if (player != null)
			player.release();
		player = null;
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		float speed = location.getSpeed();
		pos.x = location.getLongitude();
		pos.y = location.getLatitude();
		int speedInMiles = (int) (speed * 3600 / 1609.34 + 0.5);
		if (this.floatingSpeedText != null) {
			this.floatingSpeedText.setText(Html
					.fromHtml("<font color='#0000FF'>"
							+ String.valueOf(speedInMiles) + "</font>"));
		}

		int limit = -1;
		try {
			limit = Integer.parseInt(MainActivity.limitSpinner
					.getSelectedItem().toString());
		} catch (Exception e) {
			Log.e(TAG, "wrong speed format");
		}
		if (limit > 0) {
			this.speedLimit = limit;
		}
		if (speedInMiles > this.speedLimit && !player.isPlaying()) {
			player.start();
			Toast.makeText(getApplicationContext(),
					String.valueOf(speedInMiles), Toast.LENGTH_SHORT).show();
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
