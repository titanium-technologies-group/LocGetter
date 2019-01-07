package codes.titanium.locgetter_sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.titanium.locgetter.main.LocationGetter;
import com.titanium.locgetter.main.LocationProviderFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends FragmentActivity {

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
            locationGetter.getLatestSavedLocationSingle()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(location -> Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show(),
                            throwable -> Log.e(TAG, throwable.getMessage()));
        });
        findViewById(R.id.one_location_btn).setOnClickListener(v -> getOneLocation());
    }

    private void getOneLocation() {
        locationGetter.getLatestLocation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(location -> ((TextView) findViewById(R.id.locations_tv)).setText(location.toString()),
                        throwable -> Log.e(TAG, throwable.getMessage()));
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
                .subscribe(location -> adapter.addLocation(location), throwable -> Log.e(TAG, throwable.getMessage()));
    }

    private void initRv() {
        RecyclerView locationsRv = findViewById(R.id.locations_rv);
        locationsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationsRvAdapter();
        locationsRv.setAdapter(adapter);
    }

    private void initLocationGetter() {
        locationGetter = LocationProviderFactory.getInstance().getProvider(this);
    }

}
