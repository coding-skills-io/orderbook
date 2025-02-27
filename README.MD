# OrderBook
Simple application to stimulate market orders

## How to run
Make sure you have [Java 21 installed](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

1. execute the command `./gradlew test` It should execute test cases
2. execute the command `./gradlew shadowJar` It should generate the jar file
3. execute the command `java -jar build/libs/app.jar` Now, you have the app running locally on port 8080. If you run other thing on this port, just change it from Main class to something else.

## How to test locally
1. `curl -X GET "http://localhost:8080/orders"` this command should return existing market orders. It can be loaded on browser too
2. `curl -X POST "https://api.example.com/orders" \
  -H "Content-Type: application/json" \
  -d '{"type": "BUY", "quantity": 130, "accountId": "1233"}'` this command should execute buy order. You can switch it to sell and execute accordingly by looking at existing orders.

After completing an order, it will be removed from DB. But if you restart the app, DB should be initialized from scratch.

## Notes
- This is basic API demonstrating how netty.io is used for non-blocking programming
- We could use Hibernate or other similar framework to work with SQL, but for simplicity JDBC was used
- Some more tests could have been added. E.g. to test FIFO case
- Cases such as 415 Rest status codes are not handled for not overengineering this simple app
- Partial fills are not there
