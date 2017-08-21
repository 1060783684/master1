package com.yijiagou.server;

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

/**
 * Created by wangwei on 17-7-30.
 */
public class Server {
    private ServerSocket serverSocket;
    private ExecutorService bosspool;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, Socket> map;
    private Map<String,String> sessionMap;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket();
        this.bosspool = Executors.newFixedThreadPool(8);
        this.workerpool = Executors.newFixedThreadPool(1024);
        this.timepool = Executors.newScheduledThreadPool(1024);
        this.map = new ConcurrentHashMap<>();
        this.sessionMap = new ConcurrentHashMap<>();
    }

    public void newInstance() {

    }

    public Server bind(int port) throws IOException {
        this.serverSocket.bind(new InetSocketAddress(port));
        return this;
    }

    public void run() {
        for (int i = 0; i <= 8; i++)
            this.bosspool.execute(new AcceptTask(serverSocket, workerpool, timepool, map,sessionMap));
    }
}


//class ListenTask implements Runnable{
//
//    private ExecutorService workerpool;
//    private Socket socket;//netty服务器
//    private Map<String,DChannel> map;
//    private String id;
//    private String url;
//
//    public ListenTask(ExecutorService workerpool, Socket socket, Map<String, DChannel> map, String id, String url) {
//        this.workerpool = workerpool;
//        this.socket = socket;
//        this.map = map;
//        this.id=id;
//        this.url=url;
//    }
//
//    @Override
//    public void run() {
//        Event event = null;
//        DChannel dChannel= null;
//        BufferedWriter bw =null;
//        try {
//            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            event = new Event(url);
//            dChannel = map.get(id);
//            boolean b = dChannel.isStatus();
//            String data = "";
//            if (b) {
//                data = "true";
//            } else {
//                data = "false";
//            }
//            bw.write(data);
//            bw.flush();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        dChannel.addEvent(event);
//        dChannel.getLock1().lock();
//        if(!dChannel.isListen()){
//            this.workerpool.execute(new DownLoadTask());
//        }
//        dChannel.getLock1().unlock();
//    }
//}

