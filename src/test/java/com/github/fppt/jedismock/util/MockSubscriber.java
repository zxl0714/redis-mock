package com.github.fppt.jedismock.util;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MockSubscriber extends JedisPubSub {

    private String latestReceivedFromChannel;
    private String latestReceivedMessage;
    private final AtomicInteger msgCount = new AtomicInteger();
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onMessage(String channel, String message) {
        latestReceivedFromChannel = channel;
        latestReceivedMessage = message;
        latch.countDown();
        msgCount.incrementAndGet();
    }

    public String latestChannel() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return latestReceivedFromChannel;
    }

    public String latestMessage() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return latestReceivedMessage;
    }

    public int getMsgCount() {
        return msgCount.get();
    }

}
