package com.titanium.locgetter.infra;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.titanium.locgetter.exception.LocationSettingsException;
import com.titanium.locgetter.exception.NoGoogleApiException;
import com.titanium.locgetter.exception.PermissionException;

import static com.titanium.locgetter.main.Constants.ACCESS_LOCATION_PERMISSION_RESULT;
import static com.titanium.locgetter.main.Constants.REQUEST_CHECK_LOCATION_SETTINGS;

/**
 * Represents base activity for processing everything that {@link com.titanium.locgetter.main.LocationGetter} can throw
 */
public abstract class BaseLocationActivity extends AppCompatActivity implements LocationView {

    private static final int REQUEST_ERROR = 0;

    @Override
    public void showLocationsSettingsDialog(Status status) {
        try {
            status.startResolutionForResult(this, REQUEST_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showNoGoogleApiAvailable(int status) {
        Dialog errorDialog = GoogleApiAvailability.getInstance()
            .getErrorDialog(this, status, REQUEST_ERROR, dialog -> finish());
        errorDialog.show();
    }

    @Override
    public void showPermissionsDialog(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    /**
     * Parses throwable and gets appropriate message from it to show to user
     */
    protected void onLocationError(Throwable throwable) {
        if (throwable instanceof NoGoogleApiException)
            showNoGoogleApiAvailable(((NoGoogleApiException) throwable).getErrorCode());
        else if (throwable instanceof LocationSettingsException)
            showLocationsSettingsDialog(((LocationSettingsException) throwable).getStatus());
        else if (throwable instanceof PermissionException)
            showPermissionsDialog(((PermissionException) throwable).getPermission(), ((PermissionException) throwable).getRequestCode());
        throwable.printStackTrace();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_LOCATION_SETTINGS:
                onLocationSettingsResult(resultCode == RESULT_OK);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_RESULT:
                onLocationPermissionResult(grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED);
                break;
            default:
                throw new IllegalStateException("Should implement all permissions callbacks here");
        }
    }

    /**
     * Called after user granted or revoked location permissions
     *
     * @param granted result of user choice, true in case of grant, false otherwise
     */
    protected abstract void onLocationPermissionResult(boolean granted);

    /**
     * Called after user granted or revoked location settings
     *
     * @param granted result of user choice, true in case of grant, false otherwise
     */
    protected abstract void onLocationSettingsResult(boolean granted);

}
