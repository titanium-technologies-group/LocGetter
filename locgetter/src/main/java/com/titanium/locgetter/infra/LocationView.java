package com.titanium.locgetter.infra;

import com.google.android.gms.common.api.Status;

/**
 * Represents view for showing errors called by {@link com.titanium.locgetter.main.LocationGetter}
 */
public interface LocationView {

    /**
     * Used to show to user settings dialog to enable location
     */
    void showLocationsSettingsDialog(Status status);

    /**
     * Used to show to user dialog with no google api available
     */
    void showNoGoogleApiAvailable(int status);

    /**
     * Shows dialog for getting permissions for permission
     *
     * @param permission  to get access to
     * @param requestCode code to catch result later in {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
     */
    void showPermissionsDialog(String permission, int requestCode);
}
