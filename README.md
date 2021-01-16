# Nextcloud services
Nextcloud services is a simple app to poll notifications from your Nextcloud server. 
## Screenshots
![Screenshot 1](https://github.com/Andrewerr/NextcloudServices/raw/main/img/Screenshot_scaled.png)
## Instructions
Currently login available only by putting credentials into app. So to login into your Nextcloud via this app follow steps below:
* At your Nextcloud  open settings and navigate to "Security" 
* Generate per-app password
* Enter you login and server address into the app(Enter server address without `https://` prefix)
* Enter generated per-app password
* On Nextcloud server click "Add" button to add generated password to list of authenticated devices(Additionally it is recommended to disable file access to this key)

**IMPORTANT:** Do **NOT** ommit first two steps - this may be risky for your security

## Building 
You can build this app using android studio

## Credits
* [Deck Android app](https://github.com/stefan-niedermann/nextcloud-deck) for deck logo
* [Nextcloud app](https://github.com/nextcloud/android/) for Nextcloud logo and spreed(talk) logo
