# Project Description

This is an attendance taking app that uses face recognition to mark students as present

# Features

- Multi-user setup: Register as a teacher and save your classes and sessions, without worrying about other users
- Face registration: Additional face registration steps to accurately provide sample images to the recognition engine
- Liveness detection: Advanced face detection algorithms to determine if detected face is an image
- In-app Configuration: Configuration for core functionalities
- Admin view: When the registered teacher is detected, an admin view comes up to allow for manual override of attendance

# Setup

Ensure [maven](https://maven.apache.org/install.html) is installed in your system

To check:
```bash
mvn --version
```
If this prints out a version, you are good to go!

# Running the app

From the root directory (`cs102-proj`), navigate to the attendance folder:
```bash
cd attendence
```

Run the app with:
```bash
mvn compile javafx:run
```

Or if you wish to run a different main file (without the UI):
```bash
mvn compile -Dexec.mainClass=g1t1.other.MainFile exec:java
```

> [!TIP]
> Alternatively, we can just do `./dev.sh` in the root folder (Using bash)

# Building

From the root directory (`cs102-proj`), navigate to the attendance folder:
```bash
cd attendence
```

Next, compile it into a [fat](https://dzone.com/articles/the-skinny-on-fat-thin-hollow-and-uber) jar with:
```bash
mvn compile package
```

To run the built application, do:
```bash
java -jar target/attendence.jar
```