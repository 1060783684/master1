package com.yijiagou.task;

import com.yijiagou.exception.MessageException;
import com.yijiagou.message.CommandRequest;
import com.yijiagou.message.CommandResponse;
import com.yijiagou.message.Ping;
import com.yijiagou.message.Pong;
import com.yijiagou.tools.StreamHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static com.yijiagou.message.MessageKeyword.PINGPONG;

public class PingPongTask implements Runnable {
    private String id;
    private Map<String,Socket> map;
    private ScheduledExecutorService thisPool;

    private static Logger logger = Logger.getLogger("PingPong Log");

    public PingPongTask(String id,Map<String,Socket> map) {
        this.id = id;
        this.map = map;
        this.thisPool = thisPool;
    }

    @Override
    public void run() {
        int j = 0;
        boolean succe = false;

        lable:
        while (j <= 2) {//可能断开连接
            Socket socket = map.get(id);
            if (socket != null) {
                try {
                    Writer out = new OutputStreamWriter(socket.getOutputStream());
                    Reader in = new InputStreamReader(socket.getInputStream());
                    succe = StreamHandler.streamWrite(out,new Ping().toString());
                    if(!succe){
                        logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : send error }");
                        j++;
                        if(j > 2){
                            socket.close();
                            map.put(id,null);
                        }
                        Thread.sleep(300);
                        continue ;
                    }
                    int i = 0;
                    while (i <= 2) {
                        String data = StreamHandler.streamRead(in);//可能连接超时
                        Pong pong = new Pong(data);
                        if (PINGPONG.equals(pong.getHead())) {
                            break lable;
                        }
                        logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : recv error }");
                        i++;
                        if(i > 2){
                            j++;
                            if(j > 2){
                                socket.close();
                                map.put(id,null);
                            }
                            Thread.sleep(300);
                            continue ;
                        }
                    }
                }catch (MessageException e) {
                    j++;
                    logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : " + e + " }");
                    if(j > 2){
                        map.put(id,null);
                    }
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : " + e + " }");
                    j++;
                    if(j > 2){
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        map.put(id,null);
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue ;
                } catch (InterruptedException e) {
                    logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : " + e + " }");
                    e.printStackTrace();
                }
            }else {//socket为null,连接不存在或断开
                logger.error("PingPong error:{ DeviceId : " + id + " , errorInfo : socket not found}");
                j++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.info("PingPong Message:{ DeviceId : " + id + " , succeed : " +succe+ "}");
        }
    }

}
