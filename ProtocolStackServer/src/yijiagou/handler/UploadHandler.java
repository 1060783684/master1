package yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import yijiagou.tools.JedisUtils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by wangwei on 17-7-29.
 */
public class UploadHandler extends ChannelHandlerAdapter {
    private static final String retract = "    ";

    public void channelRead(ChannelHandlerContext ctx,Object msg){
        JSONObject jsonObject = (JSONObject)msg;
        try {
            String type = jsonObject.getString("type");
            if(type.equals("code")){
                jsonToCode(jsonObject);
                ctx.writeAndFlush("ok\n").addListener(ChannelFutureListener.CLOSE);
                System.out.println("Upload ok!");
            }else {
                ctx.fireChannelRead(msg);
            }
        } catch (JSONException e) {
            ctx.writeAndFlush("error");
            //json解析出错
            e.printStackTrace();
        }
    }

    public void jsonToCode(JSONObject jsonObject){
        try {
            JSONArray code = jsonObject.getJSONArray("code");
            Step step = new Step(0);
            JSONObject stateBlock = code.getJSONObject(0);
            String statement = stateBlock.getString("state");//可能会改
            String type = statement;
            step.addStep();
            StringBuffer codes = new StringBuffer();
            codes.append("import power\n");
            codes.append(getBlock(step,code,""));

            Jedis jedis = JedisUtils.getconnect();
            long number = 0;
            if(jedis.exists(type)) {
                number = jedis.zcard(type);
            }
            String path = "/opt/smdata/code/"+type+"/"+number+".py";
            jedis.zadd(type,1,number+"");
            JedisUtils.disconnect();

            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(codes.toString());
            out.flush();
        } catch (JSONException e) {
            //json解析出错
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public StringBuffer getBlock(Step step,JSONArray code,String retract){
        StringBuffer block = new StringBuffer();
        try {
            String retract0 = this.retract;
            retract0 += retract;

            for(int i = step.getStep();i < code.size();i = step.getStep()){
                JSONObject stateBlock = code.getJSONObject(i);
                String statement = stateBlock.getString("state");//可能会改
                if(statement.equals("if") || statement.equals("elif")){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" ");
                    step.addStep();

                    block.append(" power.getTime()");

                    stateBlock = code.getJSONObject(step.getStep());
                    block.append(stateBlock.getString("state"));//可能会改
                    block.append(" :");
                    step.addStep();//

                    block.append(this.getBlock(step,code,retract0));
                }else if(statement.equals("while")){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" True :");
                    step.addStep();
                } else if(statement.equals("else")){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" :");
                    step.addStep();
                    block.append(this.getBlock(step,code,retract0));
                }
                else if(statement.equals("end")){
                    step.addStep();
                    return block;
                }else{
                    block.append("\n");
                    block.append(retract);
                    block.append("power.");
                    block.append(statement);
                    block.append("()");
                    step.addStep();//
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return block;
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

    class Step{
        private int step;

        public Step(int step){
            this.step = step;
        }

        public void addStep(){
            this.step += 1;
        }

        public int getStep() {
            return step;
        }
    }
}
