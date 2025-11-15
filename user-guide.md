# User guide

## Registering

First, register a teacher account. This is the account that the teacher owns, and is the account that is able to create classes and start sessions.

On the login screen, click on "I don't have an account", and proceed with the onboarding steps.

## Creating a new class

Once an account has been created, we need to create a new class that students are able to join. For this, nagivate to "My classes" and press the button on the top right to create a new class. Once class information has been filled up, press on "create" and the class will be created!

## Onboarding students

Now, we need students to be part of the class. Students are the people that will have their attendance be taken by the system. The onboarding steps are very similar to that of the registration step, so just follow along. Once registration is done, a success notification should pop up!

## Viewing students

Once students are successfully onboarded, you can view a list of them via the individual class page. To access it, simply click on the class in the "My classes" page. At the top, click on the student list. The list of students registered for this class is then shown. You can remove or re-add students back through this list as well. Removing students will NOT affect previous sessions, meaning they will still show up in past reports, even if their accounts are deactivated.

## Starting a new session

To start a new session, navigate to the "Start session" page using the navbar on the side. From here, choose what class you want to start, and configure the week and time accordingly.

## During a session

During a session, the camera turns on and start recording faces to mark them as present. For this, detected faces will automatically be marked if it exceeds the configured `autoMarkThreshold` threshold. If it does not exceed this threshold, but exceeds the `detectionThreshold`, a popup will show, asking for confirmation on if the detected face is indeed them.

Additionally, when a teacher is detected, the admin panel button should enable itself. Pressing this button will switch the side panel, allowing teachers to manually edit attendance status or to end the session. Upon ending the session, all unmarked students will be marked as absent.

## Viewing past sessions

Once a session has ended, you are able to view them in the "Past reports" page. Upon clicking into a session, session specific stats are shown and you are able to generate reports on this particular session.

## Filtering & Ordering

Certain pages like "Past reports" have a filter button, allowing entries to be filtered out. Additionally, tables have built in sorting, meaning you can click on the column headers to sort by the specific column by ascending or decending order.

## Settings

Settings can be configured from the Settings page, found at the bottom left corner of the App. Here, you can configure the different thresholds in the app. Especially important is the liveness settings, which includes the variance threshold and the texture ratio threshold, as it affects picture detection during session. 

Once you are happy with the settings, press the "Save settings" button to save it.