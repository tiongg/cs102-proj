# Project Description

Blah blah blah write this later

# Setup

Ensure [maven](https://maven.apache.org/install.html) is installed in your system

To check:
```bash
mvn --version
```
If this prints out a version, you are good to go!

# Running the app

First, ensure we are in the right directory
```bash
cd attendence
```

Run the app with:
```bash
mvn compile javafx:run
```

Or if you wish to run a different main file:
```bash
mvn compile javafx:run -Dexec.mainClass=g1t1.other.MainFile
```

> [!TIP]
> Alternatively, we can just do `./dev.sh` in the root folder (Using bash)

# Building

First, ensure we are in the right directory
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