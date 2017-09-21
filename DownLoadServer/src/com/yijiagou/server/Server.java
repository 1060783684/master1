package com.yijiagou.server;

import com.yijiagou.config.Configurator;
import com.yijiagou.config.Log4JConfig;
import com.yijiagou.task.AcceptTask;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService bosspool;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, Socket> map;
    private Map<String,String> sessionMap;

    private Server() {

    }

    private void init() throws Exception {
        Configurator.init();
        Log4JConfig.load();
        this.serverSocket = new ServerSocket();
        this.bosspool = Executors.newFixedThreadPool(Configurator.getBosspoolNum());
        this.workerpool = Executors.newFixedThreadPool(Configurator.getWorkerpoolNum());
        this.timepool = Executors.newScheduledThreadPool(Configurator.getTimepoolNum());
        this.map = new ConcurrentHashMap<>();
        this.sessionMap = new ConcurrentHashMap<>();
    }

    public static Server newInstance() throws Exception {
        Server server = new Server();
        server.init();
        return server;
    }

    public Server bind(String host,int port) throws IOException {
        this.serverSocket.bind(new InetSocketAddress(host,port));
        return this;
    }

    public void run() {
        new Thread(new ConsoleListener()).start();
        for (int i = 0; i <= 8; i++)
            this.bosspool.execute(new AcceptTask(serverSocket, workerpool, timepool, map,sessionMap));
    }
}