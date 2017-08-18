package yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;
import yijiagou.pojo.UserAndDevice;
import yijiagou.tools.JedisUtils.JedisUtils;
import yijiagou.tools.jdbctools.JDBCTools;

import java.util.List;

/**
 * Created by zgl on 17-8-15.
 */
public class AddDeviceHandler extends ChannelHandlerAppender {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get("type");
        if (actiontype.equals("adddevice")) {
            String uname = (String) jsonObject.get("uname");
            String deviceid = (String) jsonObject.get("deviceid");
            String devicetype = (String) jsonObject.get("devicetype");
            UserAndDevice userAndDevice = new UserAndDevice(uname, deviceid, devicetype);
            String a = this.insertusersdevice(userAndDevice);
            switch (a) {
                case "1":
                    ctx.writeAndFlush("该设备已经绑定到用户");
                    break;
                case "2":
                    ctx.writeAndFlush("绑定成功");
                    break;
                case "3":
                    ctx.writeAndFlush("绑定失败");
            }
            if (a.equals("1") == false && a.equals("3") == false) {
                String sql = "insert into useranddevice(?,?)";
                this.insertmysql(sql, userAndDevice);
            }
        } else {
            ctx.fireChannelRead(ctx);
        }

    }

    private String insertusersdevice(UserAndDevice userAndDevice) {
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getconnect();
            int a = 0;
            List<String> list = jedis.lrange("user", 0, -1);
            while (a < list.size()) {
                String[] str = list.get(a).split("\\|");
                if (str[1].equals(userAndDevice.getDeviceid())) {
                    return "1";
                }
            }
            String key = userAndDevice.getUname();
            String value = userAndDevice.getDevicetype() + "\\|" + userAndDevice.getDeviceid();
            jedis.lpush(key, value);
            JedisUtils.disconnect();
        } catch (Exception e) {
            //链接出错！打印日志
            return "3";
        }

        return "2";
    }

    private void insertmysql(String sql, UserAndDevice userAndDevice) {
        try {
            JDBCTools.updata(sql, userAndDevice.getUname(), userAndDevice.getDeviceid(), userAndDevice.getDevicetype());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
