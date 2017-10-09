package codes.titanium.locgetter_sample;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.titanium.locgetter.infra.BaseLocationActivity;
import com.titanium.locgetter.main.LocationGetter;
import com.titanium.locgetter.main.LocationGetterBuilder;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseLocationActivity {

    private static final String TAG = "MainActivity";
    private LocationGetter locationGetter;
    private LocationsRvAdapter adapter;
    private Disposable locationsDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLocationGetter();
        initRv();
        initButtons();
    }

    private void initButtons() {
        Button locationsBtn = findViewById(R.id.locations_btn);
        locationsBtn.setOnClickListener(v -> onLocationUpdatesClicked(locationsBtn));
        findViewById(R.id.latest_location_btn).setOnClickListener(v -> {
            Toast.makeText(this, "Latest location = " + locationGetter.getLatestSavedLocation(), Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.one_location_btn).setOnClickListener(v -> getOneLocation());
    }

    private void getOneLocation() {
        locationGetter.getLatestLocation()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(location -> {
                ((TextView) findViewById(R.id.locations_tv)).setText(location.toString());
            }, this::onLocationError);
    }


    private synchronized void onLocationUpdatesClicked(Button locationsBtn) {
        if (locationsDisposable == null || locationsDisposable.isDisposed()) {
            locationsBtn.setText("Stop location updates");
            startLocationUpdates();
        } else {
            locationsBtn.setText("Start location updates");
            locationsDisposable.dispose();
        }

    }

    private void startLocationUpdates() {
        locationsDisposable = locationGetter.getLatestLocations()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(location -> adapter.addLocation(location)
                , this::onLocationError,
                () -> Log.d(TAG, "startLocationUpdates: completed"));
    }

    private void initRv() {
        RecyclerView locationsRv = findViewById(R.id.locations_rv);
        locationsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationsRvAdapter();
        locationsRv.setAdapter(adapter);
    }

    private void initLocationGetter() {
        locationGetter = new LocationGetterBuilder(getApplicationContext())
            .setLogger(Log::d)
            .build();
    }

    @Override
    protected void onLocationPermissionResult(boolean granted) {
        Toast.makeText(this, "Location permission granted = " + granted, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onLocationSettingsResult(boolean granted) {
        Toast.makeText(this, "Location settings granted = " + granted, Toast.LENGTH_SHORT).show();
    }

}
