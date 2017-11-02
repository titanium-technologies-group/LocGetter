LocGetter
=========
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/titanium-codes/LocGetter/blob/master/LICENSE)
[_![Download](https://api.bintray.com/packages/titanium-codes/Android/locgetter/images/download.svg) ](https://bintray.com/titanium-codes/Android/locgetter/_latestVersion)

Overview
--------
Simple multithread-friendly library for getting latest user locations using RxJava 2.


Install
-------
#### Gradle

**Step 1.** Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        jcenter()
    }
}
```
**Step 2.** Add the dependency

##### For Android studio 3.0
**Recommended**: This will use all libraries that you defined
```
dependencies {
    implementation 'com.android.support:appcompat-v7:YOUR_SUPPORT_LIBRARY_VERSION'
    implementation 'com.android.support:design:YOUR_SUPPORT_LIBRARY_VERSION'
    implementation 'io.reactivex.rxjava2:rxandroid:YOUR_RX_ANDROID_VERSION'
    implementation 'io.reactivex.rxjava2:rxjava:YOUR_RX_JAVA_2_VERSION'
    implementation 'com.google.android.gms:play-services-location:YOUR_GOOGLE_PLAY_VERSION'
    implementation ('codes.titanium:locgetter:1.0.3',{
        transitive = false
    })
}

```
**Not recommended:** This will use all dependencies from library, may increase apk size, increase methods count and lead to compile errors
```
dependencies {
    implementation 'codes.titanium:locgetter:1.0.3'
}
```

#### For Android studio 2.3
**Recommended**: This will use all libraries that you defined
```
dependencies {
    compile 'com.android.support:appcompat-v7:YOUR_SUPPORT_LIBRARY_VERSION'
    compile 'com.android.support:design:YOUR_SUPPORT_LIBRARY_VERSION'
    compile 'io.reactivex.rxjava2:rxandroid:YOUR_RX_ANDROID_VERSION'
    compile 'io.reactivex.rxjava2:rxjava:YOUR_RX_JAVA_2_VERSION'
    compile 'com.google.android.gms:play-services-location:YOUR_GOOGLE_PLAY_VERSION'
    compile ('codes.titanium:locgetter:1.0.3',{
        transitive = false
    })
}
```
**Not recommended:** This will use all dependencies from library, may increase apk size, increase methods count and lead to compile errors
```
dependencies {
    compile 'codes.titanium:locgetter:1.0.3'
}
```

Basic Usage
-----------

Main features:
* Getting updates of user locations
* Getting latest saved user location
* Getting current user location
* Catching all errors connects to getting locations

All you need is to create **LocationGetter** instance using **LocationGetterBuilder**

```
 LocationGetter locationGetter = new LocationGetterBuilder(getApplicationContext())
            .build();
```

Optional you can add to builder:

* LocationRequest to customize location updates
* GoogleApiClient to use instance of your google api client
* Logger to get logs of everything happening inside LocationGetter

Start getting locations using one of methods for e.g.

```
locationGetter.getLatestLocation()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(location -> {
                ((TextView) findViewById(R.id.locations_tv)).setText(location.toString());
            }, Throwable::printStackTrace);
```

More examples can be found in sample package with sample app.

Helper activity
---------------
You can extend your activity from **BaseLocationActivity** and get access to extended behavior with some features.

1. Catch exceptions via onLocationError to handle settings and permissions errors
2. Get callbacks with user locations permission granted/revoked and locations settings granted/revoke
3. Show dialog on google api available


Release notes
-------------
### 1.0.3
> * Updated Readme
> * Updated dependencies bump

### 1.0.2
> * Fixed bug with retrolambda crash of onLocationError
> * Added some additional logging

### 1.0.0
> * Initial release
