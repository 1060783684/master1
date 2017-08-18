package yijiagou.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_LENGTH;
import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_TYPE;

/**
 * Created by wangwei on 17-7-28.
 */
public class LoginHandler extends ChannelHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx,Object msg) throws JSONException, UnsupportedEncodingException {
        JSONObject body = (JSONObject)msg;
        String type = body.getString("type");

        if(type.equalsIgnoreCase("login")){
            String username = body.getString("username");
            String passwd = body.getString("passwd");
            String can = "false";
            if(canLogin(username,passwd)){
                can = "true";
            }
            ctx.writeAndFlush(can).addListener(ChannelFutureListener.CLOSE);
        }else {
            ctx.fireChannelRead(msg);
        }

    }

    private boolean canLogin(String username,String passwd){
        Jedis jedis = new Jedis("127.0.0.1",6379);
        jedis.connect();
        try {
            int from = 0;
            int to = 1000;
            long llen = jedis.llen("user");
            lable:while(true){
                List<String> result = jedis.lrange("user",from,to);
                for(int i = from;i < to;i++){
                    if(i < llen) {
                        String user = result.get(i);
                        String[] user0 = user.split(":");
                        if(username.equals(user0[0])){
                            if(passwd.equals(user0[2])){
                                return true;
                            }
                            return false;
                        }
                    }else{
                        break lable;
                    }
                }
                from += 1000;
                to += 1000;
            }
        }finally{
            jedis.disconnect();
        }
        return false;
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();;
        ctx.close();
    }

}
