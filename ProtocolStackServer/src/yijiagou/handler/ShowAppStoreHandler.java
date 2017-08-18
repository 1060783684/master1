package yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import yijiagou.tools.JedisUtils.JedisUtils;
import yijiagou.tools.JedisUtils.JsonUtil;
import yijiagou.pojo.UrlAppinfo;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;

/**
 * Created by zgl on 17-8-11.
 */
public class ShowAppStoreHandler extends ChannelHandlerAppender {
//    private Set<String> set = new HashSet<>();
    private static final String PATH = "/opt/info/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get("type").equals("appstore")) {
            String jiadiantype = (String) jsonObject.get("jiadiantype");
            String page = (String) jsonObject.get("page");
            JSONArray jsonArray = Jedisgeturlinfo(jiadiantype, Integer.parseInt(page));
            ctx.writeAndFlush(jsonArray.toString());
        } else {
            ctx.fireChannelRead(ctx);
        }
    }

    private JSONArray Jedisgeturlinfo(String string, int a) {
        Jedis jedis = JedisUtils.getconnect();
        int start = (a - 1) * 10;
        int end = (a - 1) * 10 + 9;
        Set set1 = jedis.zrevrange(string, start, end);
        Iterator iterator = set1.iterator();
//        String str = "";
        String json = "";
//        int i = 0;
        JSONArray jsonArray = new JSONArray();
        try {
            while (iterator.hasNext()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(PATH + iterator.next())));
                String string1 = "";
                String string2 = "";
                while ((string1 = br.readLine()) != null) {
                    string2 += string1 + "\n";
                }
                UrlAppinfo urlAppinfo = new UrlAppinfo((String) iterator.next(), string2);
                json = JsonUtil.pojoToJson(urlAppinfo);
                jsonArray.add(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}
