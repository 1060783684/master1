package yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;
import yijiagou.pojo.UserAndDevice;
import yijiagou.tools.JedisUtils.JedisUtils;
import yijiagou.tools.JedisUtils.JsonUtil;

import java.util.List;

/**
 * Created by zgl on 17-8-15.
 */
public class GetUserDeviceHandler extends ChannelHandlerAppender{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get("type");
        String uname=(String)jsonObject.get("username");
        String devicetype=(String) jsonObject.get("devicetype");
        if (actiontype.equals("download1")){
            JSONArray jsonArray = this.getUserdevices(uname,devicetype);
            ctx.writeAndFlush(jsonArray);
        }else {
            ctx.fireChannelRead(msg);
        }
    }
    private JSONArray getUserdevices(String uid,String devicetype){
        Jedis jedis = JedisUtils.getconnect();
        List<String> list=jedis.lrange("useranddevice",0,-1);
        JSONArray jsonArray=null;
        String json ="";
        for(int i=0;i<list.size();i++){
            String []str  =list.get(i).split("\\|");
            if (str[1].equals(devicetype)){
                UserAndDevice userAndDevice = new UserAndDevice(str[0]);
                json= JsonUtil.pojoToJson(userAndDevice);
                jsonArray.add(json);
            }
        }
        return jsonArray;

    }
}
