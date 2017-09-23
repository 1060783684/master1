package com.yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.cdn.Upload;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-7-29.
 */
public class UploadHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(UploadHandler.class.getName());
    private SJedisPool sJedisPool;
    private static final String IF = "if";
    private static final String ELIF = "elif";
    private static final String ELSE = "else";
    private static final String WHILE = "while";
    private static final String END = "end";
    private static final String TRUE = "True";

    public UploadHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    private static final String retract = "    ";

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject jsonObject = (JSONObject) msg;
        try {
            String type = jsonObject.getString(JsonKeyword.TYPE);
            if (type.equals(JsonKeyword.CODE)) {

                String result=jsonToCode(jsonObject);
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.fireChannelRead(msg);
            }
        } catch (JSONException e) {
            logger.error(e + "===>Upload error");
            ctx.writeAndFlush("error");
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Upload error");
            ctx.writeAndFlush("error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public String jsonToCode(JSONObject jsonObject) throws Exception {
        Jedis jedis = null;
        try {
            JSONArray code = jsonObject.getJSONArray(JsonKeyword.CODE);
            String info = jsonObject.getString(JsonKeyword.INFO);
            String type = jsonObject.getString(JsonKeyword.DEVICETYPE);
            String typeinfo=type+"info";
            System.out.println(typeinfo);
            Step step = new Step(0);
            StringBuffer codes = new StringBuffer();
            codes.append("from command.power import *\n");
            codes.append(getBlock(step, code, ""));
            System.out.println(codes.toString());
            int count = 0;
            jedis = sJedisPool.getConnection();

            String info1 = "";

            if(info.length() > 10)
                info1 = info.substring(0, 10);
            else
                info1 = info;


            while (count < 3) {//重连３次
                try {
                    long appid = getTypeId(jedis,type);

                    //----------------------CDN请求 回复-----------------------------
                    StringBuffer sb = new StringBuffer();
                    sb.append(info);
                    String filepath0="/code/"+type+"/"+appid+".py";
                    String infopath = "/info/"+type+"/"+appid+".info";
                    System.out.println(codes);
                    System.out.println("type:"+type);
                    System.out.println(filepath0);
                    String result = Upload.uploadFile(filepath0,codes,infopath,info);


                    if("1".equals(result)){//成功才插入到redis和mysql中
                        //插入到redis里
                        addTypeApp(jedis,type,appid,info1);

                        //插入到mysql里
                        String sql = "insert into appinfo values(?,?,?)";
                        shortInfo(sql,appid+"",info1+"...",type);
                        sJedisPool.putbackConnection(jedis);
                        logger.info("===>upload:{type:"+type+",appid:"+appid+",appInfo:"+info1+",success:"+true+"}");
                        return "1";
                    }else {
                        throwAppid(jedis,type,appid);
                        sJedisPool.putbackConnection(jedis);
                        logger.info("===>upload:{type:"+type+",appid:"+appid+",appInfo:"+info1+",success:"+false+"}");
                        return "0";
                    }
                    //----------------------CDN请求 回复-----------------------------

                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                    logger.error(e + "===>upload");
                    count++;
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        count++;
                    }
                }
            }
        } catch (JSONException e) {
            if(jedis != null){
                sJedisPool.putbackConnection(jedis);
            }
            //json解析出错
            logger.error(e+"===>jsonToCode");
//            e.printStackTrace();
            return "error";
        }
        if(jedis != null){
            sJedisPool.putbackConnection(jedis);
        }
        return "error";

    }

    public StringBuffer getBlock(Step step, JSONArray code, String retract) {
        StringBuffer block = new StringBuffer();
        try {
            String retract0 = this.retract;
            retract0 += retract;

            for (int i = step.getStep(); i < code.size(); i = step.getStep()) {
                JSONObject stateBlock = code.getJSONObject(i);
                String statement = stateBlock.getString(JsonKeyword.COUNT);//可能会改
                if (statement.equals(IF) || statement.equals(ELIF)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" ");
                    step.addStep();

                    stateBlock = code.getJSONObject(step.getStep());
                    String methodCount = stateBlock.getString(JsonKeyword.COUNT);
                    String[] methodCounts = methodCount.split("\\|");
                    block.append("get");
                    block.append(methodCounts[1]);//可能会改
                    block.append("()");
                    if (methodCounts[1].equals("Time")) {
                        block.append(" == ");
                    }
                    ;
                    if (methodCounts[2].equals("String"))
                        block.append("\"" + methodCounts[0] + "\"");
                    else
                        block.append(methodCounts[0]);
                    block.append(" :");
                    step.addStep();

                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(WHILE)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" " + TRUE + " :");
                    step.addStep();
                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(ELSE)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" :");
                    step.addStep();
                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(END)) {
                    step.addStep();
                    return block;
                } else {
                    block.append("\n");
                    block.append(retract);

                    String[] counts = statement.split("\\|");
                    block.append(counts[0]);
                    System.out.println(step.getStep());
                    int methodNum = Integer.parseInt(counts[1]);
                    block.append("(");
                    for (int j = 0; j < methodNum; j++) {
                        step.addStep();
                        JSONObject argBlock = code.getJSONObject(step.getStep());
                        String arg = argBlock.getString(JsonKeyword.COUNT);
                        String[] args = arg.split("\\|");
                        if (args[2].equals("String"))
                            block.append("\"" + args[0] + "\"");
                        else
                            block.append(args[0]);

                        if (j != methodNum - 1) {
                            block.append(",");
                        }
                    }
                    block.append(")");
                    step.addStep();
                }
            }
        } catch (JSONException e) {
            logger.error(e + "===>upload");
            e.printStackTrace();
        }
        logger.info(code.toArray().toString() + "翻译成功===>" + block);
        return block;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private Long getTypeId(Jedis jedis,String type){//获取appid
        //获取编号
        String ltypeid = "l"+type+"id";
        String typeid = type+"id";
        if(jedis.exists(ltypeid)){//若丢弃队列里有丢弃的id则取出
            return Long.parseLong(jedis.lpop(ltypeid));
        }else if(jedis.exists(typeid)){//若没有则从正常id处获取
            return jedis.incr(typeid);
        }else{
            jedis.set(typeid,"0");
            return (long)0;
        }
    }

    private void addTypeApp(Jedis jedis,String type,long appid,String appinfo){//添加
        String typeInfo = type+"info";
        Transaction transaction=null;
        transaction=jedis.multi();//事务开启
        transaction.zadd(type, 1,appid+"");
        transaction.hset(typeInfo,appid+"",appinfo+"...");
        transaction.exec();//事务提交
    }

    private void throwAppid(Jedis jedis,String type,long appid){
        String ltypeid = "l"+type+"id";
        jedis.rpush(ltypeid,appid+"");
    }


    public int shortInfo(String sql,String appid,String info,String deviceType){
        int count = 0;
        int a = 0;
        while (true) {
            try {
                a = ConnPoolUtil.updata(sql,appid, info,deviceType);
                return a;
            } catch (Exception e) {
                logger.warn(e + "insertMysql");
                if (count++ >= 2) {
                    logger.error("访问数据库时无法提供服务===>insertMysql:"+a);
                    return a;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    logger.error("insertMysql" + e1);
                }
                continue;
            }
        }
    }

    class Step {
        private int step;

        public Step(int step) {
            this.step = step;
        }

        public void addStep() {
            this.step += 1;
        }

        public int getStep() {
            return step;
        }
    }
}
