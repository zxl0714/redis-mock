package com.github.fppt.jedismock;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Xiaolu on 2015/4/21.
 */
public class RedisService implements Runnable {

    private final ServerSocket server;
    private final RedisBase base;
    private final ServiceOptions options;

    public RedisService(ServerSocket server, RedisBase base, ServiceOptions options) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(base);
        Preconditions.checkNotNull(options);

        this.server = server;
        this.base = base;
        this.options = options;
    }

    public void run() {
        while (!server.isClosed()) {
            try {
                Socket socket = server.accept();
                Thread t = new Thread(new RedisClient(base, socket, options));
                t.start();
            } catch (IOException e) {
                // Do noting
            }
        }
    }
}
