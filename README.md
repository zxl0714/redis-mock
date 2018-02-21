[![GitHub release](https://img.shields.io/github/release/graknlabs/redis-mock.svg)](https://github.com/graknlabs/redis-mock/releases/latest)
[![Build Status](https://travis-ci.org/graknlabs/redis-mock.svg?branch=master)](https://travis-ci.org/graknlabs/redis-mock)
[![Slack Status](http://grakn-slackin.herokuapp.com/badge.svg)](https://grakn.ai/slack)

# Redis-Mock

Redis Mock is a simple in-memory mock of redis for java testing. 
It allows you to test any behaviour dependent on redis without having to deploy an instance of redis

## Quickstart 

Add it as a dependency in Maven as:

```xml
<dependency>
  <groupId>ai.grakn</groupId>
  <artifactId>redis-mock</artifactId>
  <version>0.1.6</version>
</dependency>
```

Create a redis server and bind it to jedis:

```
private static RedisServer server = null;

@Before
public void before() throws IOException {
  server = RedisServer.newRedisServer();  // bind to a random port
  server.start();
}

@Test
public void test() {
  ...
  Jedis jedis = new Jedis(server.getHost(), server.getBindPort());
  ...
}

@After
public void after() {
  server.stop();
  server = null;
}
```

From here test as needed

## Master and Slave

```
RedisServer master = newRedisServer();
RedisServer slave = newRedisServer();
master.setSlave(slave);
```

## Fault tolerance testing

We can make a RedisServer close connection after every serveral commands. This will cause a connection exception for clients.

```
RedisServer server = RedisServer.newRedisServer();
ServiceOptions options = new ServiceOptions();
options.setCloseSocketAfterSeveralCommands(3);
server.setOptions(options);
server.start();
```

