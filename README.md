# Uller Android App

The Uller Android app allows users to set an alert on sold-out products and receive a notification when they become available. 
A direct link then takes the user to the corresponding website of the supplier (in a webview).
In this example, the current generation of video game consoles and graphics cards were taken as the products to be subscribed to. 
The list of products to be subscribed to can be exchanged at will and is obtained by the Android app from the Uller backend.

####
- See the [Uller Backend](https://github.com/grundhofer/uller-backend) repo to get the App running.
- See the [Uller web Front-end](https://github.com/grundhofer/uller-dashboard-flutter_web) repo for testing the notfication service.

#### Example UI/UX
![Uller Android ](https://github.com/grundhofer/uller-android/tree/main/docs/android_uller.gif)

#### Example of notification trigger from Uller Web Frontend
![Uller Android notification](https://github.com/grundhofer/uller-android/tree/main/docs/android_uller_notification.gif)

#### Prerequisites:
In order to use the Uller app, among other things, The Google Services Gradle plugin must be configured and 
the Uller backend must be put into operation.

#### Technologies:
- Jetpack Compose
- Firebase Cloud Messaging
- The Google Services Gradle Plugin
