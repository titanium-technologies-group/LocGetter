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
```
dependencies {
    implementation 'codes.titanium:locgetter:1.2.0'
    implementation 'codes.titanium:connectableactivity:1.0.3'
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

* Logger to get logs of everything happening inside LocationGetter
* Accept mock locations behavior

Start getting locations using one of methods for e.g.

```
locationGetter.getLatestLocation()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(location -> {
                ((TextView) findViewById(R.id.locations_tv)).setText(location.toString());
            }, Throwable::printStackTrace);
```

More examples can be found in sample package with sample app.

Error handling
---------------
All exceptions are handled in library.

In case of no permissions -> user will be asked to give permissions, if user declines -> NoLocationPermission will be thrown

In case of turned off location -> user will be asked to turn it on, if user declines -> LocationSettingsException will be thrown

Accept mock locations behavior 
---
By default mock locations are accepted.
You can filter all mock locations that are received by location manager. Just set acceptMockLocations to false when you are building location getter.

In case if mock location will be received MockLocationException will be thrown with that mocked location and you can decide what to do with it.

Release notes
-------------
### 1.2.0
> * Fixed share observable
> * Locations settings dialog is now using default google one

### 1.1.2
> * Extracted ConnectableActivity

### 1.1.1
> * Updated documentation
> * Fixed ConnectableActivity visibility

### 1.1.0
> * Major refactor
> * Removed a lot of redundant APIs
> * No more need of activity to handle exceptions
> * Optimizations
> * Feature - mock locations filter 

### 1.0.3
> * Updated Readme
> * Updated dependencies bump

### 1.0.2
> * Fixed bug with retrolambda crash of onLocationError
> * Added some additional logging

### 1.0.0
> * Initial release
