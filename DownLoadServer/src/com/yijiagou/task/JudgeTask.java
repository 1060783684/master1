package com.yijiagou.task;

import com.yijiagou.config.Configurator;
import com.yijiagou.exception.MessageException;
import com.yijiagou.message.*;
import com.yijiagou.tools.StreamHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yijiagou.message.MessageKeyword.*;
public class JudgeTask implements Runnable {
    private Socket socket;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, Socket> map;
    private Map<String, String> sessionMap;

    private static Logger logger = Logger.getLogger("JudgeTask Log");

    public JudgeTask(Socket socket, ExecutorService workerpool,
                     ScheduledExecutorService timepool, Map<String, Socket> map, Map<String, String> sessionMap) {
        this.socket = socket;
        this.workerpool = workerpool;
        this.timepool = timepool;
        this.map = map;
        this.sessionMap = sessionMap;
    }

    public void homeDispose(String id, Socket socket) {
        //记录登录的冰箱
        this.map.put(id, socket);
        timepool.scheduleAtFixedRate(new PingPongTask(id, this.map), 1, 100, TimeUnit.SECONDS);
    }

    public boolean commandToHome(String id, String info) {
        int count = 0;
        Socket hsocket = null;
        while (count < 6) {
            hsocket = this.map.get(id.trim());
            if (hsocket != null) {
                try {
                    Writer writer = new OutputStreamWriter(hsocket.getOutputStream());
                    Reader reader = new InputStreamReader(hsocket.getInputStream());
                    boolean succe = StreamHandler.streamWrite(writer, info);
                    if(!succe){
                        count++;
                        if(count > 2){
                            socket.close();
                            map.put(id,null);
                        }
                        Thread.sleep(300);
                        continue ;
                    }
                    String data = StreamHandler.streamRead(reader);
                    CommandResponse response = new CommandResponse(data);
                    if (data != null) {
                        if (response.isOk()) {//有可能家居接受的包错误,则重新发送
                            logger.info("JudgeTask info:{ DeviceId : " + id + " , succeed : " + succe +
                                    " , commandInfo : "+info+" , commandResp : "+response+" }");
                            return true;//成功只有一个可能
                        }else {
                            logger.info("JudgeTask error:{ DeviceId : " + id + " , succeed : " + succe +
                                    " , commandInfo : "+info+" , commandResp : "+response+" }");
                            continue;
                        }
                    }
                    count++;
                } catch (IOException e) {
                    count++;
                    logger.error("JudgeTask error:{ errorInfo : "+e+" }");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    logger.error("JudgeTask error:{ errorInfo : "+e+" }");
                    e.printStackTrace();
                } catch (MessageException e) {
                    logger.error("JudgeTask error:{ errorInfo : "+e+" }");
                    count++;
                    e.printStackTrace();
                }
            } else {
                logger.error("JudgeTask error:{ errorInfo : socket not found }");
                count++;
            }
        }
        if (hsocket != null) {//成功了就在前面返回了
            try {
                hsocket.close();
            } catch (IOException e) {
            }
        }
        return false;
    }

    public String commandToHomes(String[] ids,String info){
        String data = "";
        for (int i = 0; i < ids.length; i++) {
            boolean judge = commandToHome(ids[i],info);
            if (judge) {
                data += "1";
                continue;
            }
            data += "0";
        }
        return data;
    }

    @Override
    public void run() {//记得分业务出去,给家电发送下载命令
        Writer out = null;
        Reader in = null;
        try {
            out = new OutputStreamWriter(socket.getOutputStream());
            in = new InputStreamReader(socket.getInputStream());
            String conninfo = StreamHandler.streamRead(in);//我们的第五层业务
            Message message = new Message(conninfo);

            if (CONNECTION.equals(message.getHead())) {//与家电的链接,与家电建立长链接
                DeviceRequest request = new DeviceRequest(conninfo);
                DeviceResponse response = new DeviceResponse(OK);

                boolean succe = StreamHandler.streamWrite(out, response.toString());
                //-------------判断家电连接是否成功---------------
                if (succe) {
                    socket.setSoTimeout(30000);
                    homeDispose(request.getId(), socket);
                }

                logger.info("JudgeTask info:{ connectionRequest : "+request.toString()
                        +" , connectionResponse : "+response.toString() + "succeed : "+succe+" }");
                //-------------判断家电连接是否成功---------------
            } else if (PSCOMMAND.equals(message.getHead())) {//与netty的链接,给家电发送下载命令
                PSRequest request = new PSRequest(conninfo);
                String sessionId = request.getSessionId();
                String data = sessionMap.get(sessionId);
                //-------------判断是不是上一个netty会话没有发出去的消息-------------
                if (data != null) {
                    boolean succe = StreamHandler.streamWrite(out, data);
                    if (succe) {
                        sessionMap.remove(sessionId);
                        return;
                    }
                }
                //-------------判断是不是上一个netty会话没有发出去的消息-------------
                String path = Configurator.getCdnUrl()+"/code/"+request.getDeviceType() + "/" + request.getAppId() + ".py";
                CommandRequest commandRequest = new CommandRequest(path);
                String[] ids = request.getDeviceIds();
                data = commandToHomes(ids,commandRequest.toString());

                PSResponse psResponse = new PSResponse(data);
                boolean succe = StreamHandler.streamWrite(out, psResponse.toString() + "\n");//若给ps服务器的回复不成功,则保存下来
                if (!succe)
                    sessionMap.put(sessionId, psResponse.toString());
                StreamHandler.closeReader(in);
                StreamHandler.closeWriter(out);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessageException e) {
            //各种可能的错误
            if ("Message package is null".equals(e.getMessage())) {//没有数据,连接已断开
                StreamHandler.closeReader(in);
                StreamHandler.closeWriter(out);
            }else if ("PSRequest urlsize fault".equals(e.getMessage())){
                try {
                    StreamHandler.streamWrite(out, new PSResponse(ERROR).toString());
                } catch (MessageException e1) {
                    e1.printStackTrace();
                }
                StreamHandler.closeWriter(out);
                StreamHandler.closeReader(in);
            }else if("DeviceRequest id length fault".equals(e.getMessage())){
                try {
                    StreamHandler.streamWrite(out, new DeviceResponse(ERROR).toString());
                } catch (MessageException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
