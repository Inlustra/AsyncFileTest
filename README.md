# Async File Test

A simple program demonstrating an Asynchronous addition to a Consumer.

This example features a Logging component that writes to a file upon addition.

## Examples

### Starting the consumer

```java
    LogConsumer consumer = new LogConsumer();
    Thread thread = new Thread(consumer); //Given a context, this could be simplified
    thread.start();
```

### Adding to the consumer

```java
    consumer.log(String string);
```

## Future Development

* A broker could be created to delegate the addition to the LogConsumer if the write count is too high
* A test framework could be built to ensure that it is functioning correctly
