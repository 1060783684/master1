package yijiagou.handler;

import yijiagou.tools.JedisUtils.JedisUtils;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import yijiagou.tools.jdbctools.JDBCTools;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by zgl on 17-7-28.
 */
//  String string ="{\"type\":\"register\",\"username\":\"xxxxx\",\"passwd\":\"xxxxxx\",\"phone\":\"21865165\"}";
public class RegisterHander extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject)msg;
        String username = (String) jsonObject.get("username");
        String passwd = (String) jsonObject.get("passwd");
        String telnum = (String) jsonObject.get("telnum");
//        String data =username+":"+passwd+":"+telnum;
        if (jsonObject.get("type").equals("register")) {
            String state = jedislpush(username, passwd);
            switch (state) {
                case "1":
                    ctx.writeAndFlush("账户存在");
                    break;
                case "3":
                    ctx.writeAndFlush("注册成功");
            }
            if (state.equals("1") == false ) {
                String sql = "insert into userinfo values(?,?)";
                int state1 = mysqladd(sql, username, passwd);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private String jedislpush(String username, String passwd) {
        String data = username + "|" + passwd;
        Jedis jedis = JedisUtils.getconnect();
        int b = 1000;
        int a = 0;
        int c = 0;
        for (; a < jedis.llen("user"); ) {
            c = a + b;
            List<String> list = jedis.lrange("user", a, c);
            for (String str : list) {
                String s[] = str.split("\\|");
                if (s[1].equals(username)) {
                    return "1";
                }
            }
            a = c + 1;
        }
        jedis.lpush("user", data);
        JedisUtils.disconnect();
        return "3";
    }

    private int mysqladd(String sql, String username, String passwd) {
        int a = 0;
        try {
            a = JDBCTools.updata(sql,username,passwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }


}
