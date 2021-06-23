# CarGameMaven
This is a simple car simulation game using maven as package manager and build script.

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
