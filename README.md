LocGetter
=========
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/blainepwnz/AndroidContacts/blob/master/LICENSE.txt)
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
			...
			jcenter()
    	}
	}
```
**Step 2.** Add the dependency
```
	dependencies {
		compile 'codes.titanium:locgetter:1.0.2'
	}
```

#### Maven

```
<dependency>
  <groupId>codes.titanium</groupId>
  <artifactId>locgetter</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
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

### 1.0.2
> * Fixed bug with retrolambda crash of onLocationError
> * Added some additional logging

### 1.0.0
> * Initial release
