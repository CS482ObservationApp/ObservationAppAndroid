CDOT "Instrumented Unit" tests
CS482 - 2016

These unit tests check the core functionality of the app.


----------------------------------------
IMPORTANT NOTE:

-Tests can fail if the drupal site's server is slow to respond.


-A testing framework like Robotium would be better to use for testing, from what I have read.
 https://github.com/robotiumtech/robotium
 If extending this app, I recommend writing tests using this framework.

----------------------------------------
HOW TO RUN:

To run a test, right click one (Ex: LoginActivityTest), and select "Run: 'LoginActivityTest'".

To run a specific part of a test, right click a function name and select "Run: '*function_name*'".

*Be sure to change the "Test Artifact" under "Build Variants" (on the side bar of Android Studio) to "Android Instrumentation Tests".

----------------------------------------
Details of each test:


LoginActivityTest - checks valid username/password and invalid credentials.


NewestObservationsActivityTest - checks if newest observations are showing, clicks on an observation.


RegisterActivityTest - Checks to see if registering a username/email in use fails.  Checks to see if
                    registering a username/email not in use succeeds.


ResetPasswordActivityTest -  checks to see if an email is sent out for a valid username


SearchObservationActivityTest - tests if searching is successful.  Checks if autocomplete works.


UploadActivityTest - ensures that the UI is displayed.  Makes sure that invalid records cannot be uploaded.
                   Also checks autocomplete.


UserProfileActivityTest - Makes sure that the profile displayed is for the user currently logged in.