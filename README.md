# DisasterAid – Emergency Resource & Volunteer Network

## Project Description
DisasterAid is an Android application designed to connect people
affected by emergencies with volunteers and available resources.

## Objectives
- Help users create assistance requests.
- Connect volunteers with people who need help.
- Share emergency resources.
- Provide a simple platform for emergency support.

## Technologies Used
- Java
- XML
- Android Studio
- Firebase Authentication
- Firebase Realtime Database
- SharedPreferences
- Android Sensors

## Main Features
- User registration and login
- User profile management
- Assistance request management (CRUD)
- Volunteer management (CRUD)
- Emergency resource management (CRUD)
- Image selection from camera/gallery
- Shake detection using accelerometer
- Emergency call and sharing options
- Nearby hospital search
- User settings

## Building the APK
To generate the debug APK for testing and deployment:
1. Open the project in Android Studio or terminal.
2. Run the Gradle build command:
   ```bash
   ./gradlew assembleDebug
   ```
3. The generated APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

## Firebase Setup
This project uses Firebase Authentication and Realtime Database.

To run the project:
1. Create your own Firebase project.
2. Register your Android app using package name:
   com.example.disasteraid
3. Enable Email/Password Authentication.
4. Create a Realtime Database.
5. Download your own google-services.json.
6. Place it inside the app/ folder.

Do not upload google-services.json or private credentials
to this public repository.

## Installation
1. Clone or download this repository.
2. Open the project in Android Studio.
3. Configure Firebase using your own configuration file.
4. Sync Gradle.
5. Run the application on an emulator or Android device.

## Developer
Student Lab Project

## Version
1.0
