# RacingSimTrackGen2D
This is a simple car simulation game using maven as package manager and build script.

The main feature of this project is that it converts raster images into racetracks where it's possible to drive around and play with the collision physics.

Turn this image:
![Raster image of racetrack. White is track area and black is non-drivable area](track.png "Racetrack in black and white")

Into this:
![Screenshot of the result after running the simulator with the track above](example.png "Racetrack after converted by simulator program")

## Requirements
Assumes the following is installed and added to path:
 - Java JDK 1.8.0 (Tested on javac 1.8.0_121 on windows)
 - Maven. (Tested on maven 3.8.1)

## Building
Simply run `mvn compile` to build the project.

Create a package of the dependencies with: `mvn package`

### Running
`java -cp target/cargamemaven-0.0.1-SNAPSHOT.jar org.isachsen.ulrich.game.CarGameMaven`

The jar file can also be run directly:
`java -jar target/cargamemaven-0.0.1-SNAPSHOT.jar`

## About
This project was started in August 2017, although the initial commmit was done June 2021.
