package com.titanium.locgetter.exception;

/**
 * Exception that contains all data about permission inside
 */
public class PermissionException extends RuntimeException {

    private String permission;
    private int requestCode;

    /**
     * @param permission what is missing
     * @param requestCode to get in callback in {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
     */
    public PermissionException(String permission, int requestCode) {
        this.permission = permission;
        this.requestCode = requestCode;
    }

    public String getPermission() {
        return permission;
    }

    public int getRequestCode() {
        return requestCode;
    }
}