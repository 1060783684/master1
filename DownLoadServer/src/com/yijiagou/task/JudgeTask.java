package com.yijiagou.task;

import com.yijiagou.pojo.DChannel;
import com.yijiagou.pojo.Event;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangwei on 17-8-4.
 */
public class JudgeTask implements Runnable {
    private Socket socket;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, DChannel> map;

    public JudgeTask(Socket socket, ExecutorService workerpool,
                     ScheduledExecutorService timepool, Map<String, DChannel> map) {
        this.socket = socket;
        this.workerpool = workerpool;
        this.timepool = timepool;
        this.map = map;
    }

    public void homeDispose(String id, Socket socket) {
        //记录登录的冰箱
        DChannel dChannel = new DChannel(socket, true);
        this.map.put(id, dChannel);
        timepool.scheduleAtFixedRate(new PingPongTask(id,this.map), 1, 1, TimeUnit.MINUTES);
//        timepool.scheduleAtFixedRate(new PingPongTask(id,this.map), 1, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String conninfo = in.readLine();

            System.out.println(conninfo);
            String[] infos = conninfo.split("\\|");
            System.out.println(infos[0]);
            System.out.println(infos[1]);
            String req = "1111|ok\n";
            if (infos[0].equals("1111")) {//与家电的链接
                if (infos[1] == null) {
                    req = "1111|err\n";
                }
                homeDispose(infos[1], socket);
                out.write(req);
                out.flush();
            } else if (infos[0].equals("0000")) {//与netty的链接
                //发送成功消息并启动监听队列线程
                String id = infos[1];
                String type = infos[2];
                String aid = infos[3];
                Event event = null;
                DChannel dChannel = null;
//                BufferedWriter bw = null;
                try {
//                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    event = new Event("/opt/smdata/code/"+type+"/"+aid+".py");
//                    System.out.println(event.getInfo());
                    dChannel = map.get(id);

                    dChannel.getLock().lock();
                    boolean b = dChannel.isStatus();//可能出错
                    dChannel.getLock().unlock();

                    String data = "";
                    if (b) {
                        data = "true\n";
                    } else {
                        data = "false\n";
                    }
                    out.write(data);
                    System.out.print(data);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    //记录错误 远程io 中断 netty服务器
                }finally {
                    out.close();
                }

                dChannel.addEvent(event);

                dChannel.getLock1().lock();
                if (!dChannel.isListen()) {
                    this.workerpool.execute(new DownLoadTask(dChannel));
                }
                dChannel.getLock1().unlock();

            } else {
                out.write("1001|err\n");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //记录错误 远程io 中断 未知
        }
    }
}
