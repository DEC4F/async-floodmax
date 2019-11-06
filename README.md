# Asynchronous Floodmax Algorithm with Synchronizer

## Description
This program implements [Asynchronous Floodmax Algorithm] in a simulated asynchronous general network using multi-threading in Java. The program reads a `connectivity.txt` file that contains a number `n` in the first line, as the number of processes, and an array `id` of size n in the second line. Additionally, line 3 - n+3 stores the graph as an adjacency matrix with 0 representing there's no edge between node i and j and 1 representing there is an edge in-bwteen. In the end, each processes will output its own id, the leader id, and total amount of messages they sent, and then terminate.

There's an extra master thread that spawns threads based on the value of n and assign ids to them accordingly. The ith element of the `id` stores the unique id of the ith threads. No thread knows the value of `n`.

## Structure
- `App.java` is the driver class that has the main method. It also reads `connectivity.txt` and spawns an instance of `MasterThread`
- `MasterThread.java` and spawns n instances of `AsyncThread`, runs those `AsyncThreads` and print the result.
- `AsyncThread.java` can send message, receive message, and run the algortihm.
- `Token.java` is a class that represents a generic token passed between processes.
- `ThreadException.java` is the general exception class for this environment.

## Compile and Run
`javac -cp async-floodmax-1.0-SNAPSHOT.jar src/main/java/edu/utdallas/cs6380/*.java`

`java -cp src/main/java/ edu.utdallas.cs6380.App input/connectivity1.txt`

## Testing
```
mvn test
```