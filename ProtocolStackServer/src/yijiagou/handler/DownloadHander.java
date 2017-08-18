package yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by zgl on 17-7-29.
 */

public class DownloadHander extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject jsonObject = (JSONObject)msg;
        String type =(String) jsonObject.get("type");
        String jiadiantype=(String) jsonObject.get("jiadiantype");
        String jiadianid=(String) jsonObject.get("jiadianid");
        String appid =(String)jsonObject.get("appid");

        if(type.equals("download")){
//            Jedis jedis=JedisUtils.getconnect();
//            String info=jedis.get(id);
//            String infos[]=info.split("\\|");
            String url="0000"+"|"+jiadianid+"|"+jiadiantype+"|"+appid;
            String ip="127.0.0.1";
            int port=9999;
            BufferedWriter bw = null;
            BufferedReader br = null;
            try {
                Socket socket = bind(ip,port);
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                bw.write(url+"\n");
                bw.flush();

                String data = br.readLine();

                ctx.writeAndFlush(data+"\n");
                System.out.println("Download ok!");
            }catch (SocketTimeoutException e){
                //打印日志 timeout
            }catch (IOException e) {
                //远程链接断开异常
                e.printStackTrace();
            }finally {
                try {
                    bw.close();
                    br.close();
                } catch (IOException e) {
                    bw = null;
                    e.printStackTrace();
                }
            }
//            jedis.disconnect();
        }else {
            ctx.fireChannelRead(ctx);
        }

    }

    private Socket bind(String ip,int port) throws IOException{
        return new Socket(ip,port);
    }


}
