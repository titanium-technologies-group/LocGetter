package com.titanium.locgetter.exception

import android.location.Location

/**
 * Thrown when mock locations are restricted, but still received
 * Contains this mocked location
 */
class MockLocationException(val location: Location) : RuntimeException()