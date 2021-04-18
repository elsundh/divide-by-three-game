# TakeAway Homework

## Dependencies

- java 11
- maven 3.2+
- Spring Boot 2
 
## How to run it

	$ mvn spring-boot:run

The application will run in port 8080

## API documentation

To join to a new game:

	GET http://localhost:8080/log-on

To play:

	POST http://localhost:8080/play?added=[0,1,-1]

To get game events

	GET http://localhost:8080/game-events

To leave the game
	
	POST: http://localhost:8080/log-off

