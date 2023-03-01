# Nextcloud services
[![Java CI with Gradle](https://github.com/Andrewerr/NextcloudServices/actions/workflows/gradle.yml/badge.svg)](https://github.com/Andrewerr/NextcloudServices/actions/workflows/gradle.yml)
[![F-Droid build](https://img.shields.io/f-droid/v/com.polar.nextcloudservices.svg?logo=f-droid)](https://f-droid.org/wiki/page/com.polar.nextcloudservices/lastbuild)
![Github tag](https://img.shields.io/github/v/tag/Andrewerr/NextcloudServices?logo=github)
<br>
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75">](https://f-droid.org/en/packages/com.polar.nextcloudservices/)
<br>
Nextcloud services is a simple app to poll notifications from your Nextcloud server without using proprietary Google Play services. 
## Screenshots
![Screenshot 1](https://github.com/Andrewerr/NextcloudServices/raw/main/img/Screenshot_scaled.png)
## Instructions
WITHOUT NEXTCLOUD APP:
* At your Nextcloud  open settings and navigate to "Security" 
* Generate per-app password
* Enter you login and server address into the app(Enter server address without `https://` prefix)
* Enter generated per-app password
* On Nextcloud server click "Add" button to add generated password to list of authenticated devices(Additionally it is recommended to disable file access for this per-app password)

IMPORTANT: Do **NOT** ommit first two steps - this may be risky for your security

WITH NEXTCLOUD APP:
* Click "Log-in via Nextcloud app"
* Select account you want to use
* In next dialog click "Allow button"

## Getting bleeding-edge version
If you would like to test new features and fixes as they are developed you may download a bleeding-edge build from [Github Actions](https://github.com/Andrewerr/NextcloudServices/actions). Here is the [instruction](https://github.com/Andrewerr/NextcloudServices/actions/runs/4136114311) of how you can do it. Also please note that builds done by Actions are *not* signed, so you would need to delete your app installes from F-Droid(if you have installed it) and use `adb` to install app.

## Donate
If you like this app please donate:<br>
[![LiberaPay](https://liberapay.com/assets/widgets/donate.svg)](https://liberapay.com/Andrewerr/donate)


## Credits
* [Deck Android app](https://github.com/stefan-niedermann/nextcloud-deck) for deck logo
* [Nextcloud app](https://github.com/nextcloud/android/) for Nextcloud logo and spreed(talk) logo
* [@penguin86](https://github.com/penguin86) for fixing bugs and suggesting new ideas
* [@Donnnno](https://github.com/Donnnno) for creating app icon
* [@invissvenska](https://github.com/invissvenska) for [NumberPickerPreference](https://github.com/invissvenska/NumberPickerPreference/) (licensed under LGPL-3.0)
* [@Devansh-Gaur-1611](https://github.com/Devansh-Gaur-1611) for creating credits activity in the app
* [@freeflyk](https://github.com/freeflyk) for improvements, fixes and adding new features
