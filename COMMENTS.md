## Requirements
1) Maven (http://maven.apache.org/install.html)
2) Java8+ (https://openjdk.java.net/projects/jdk8/)

**NOTE:** Application tested with openjdk version 1.8.0_242

## Build

Ensure that maven is installed (http://maven.apache.org/install.html)

Then run the following commands:
```
mvn clean package
```

## Run
```
java -jar /Users/martint/Uber/proto/target/proto-1.0-SNAPSHOT-jar-with-dependencies.jar -transactionFile <TRANSACTION_FILE> -userId <USER_ID>

-transactionFile: transaction file to parse. REQUIRED.
-userId: Id of the user whose balance to check. OPTIONAL. Default: 2456938384156277127

```

### Example
```
java -jar target/proto-1.0-SNAPSHOT-jar-with-dependencies.jar -transactionFile txnlog.dat
```