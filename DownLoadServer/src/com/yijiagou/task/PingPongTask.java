package com.yijiagou.task;

import com.yijiagou.pojo.DChannel;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;

/**
 * Created by wangwei on 17-8-4.
 */
public class PingPongTask implements Runnable {

    private String id;
    private Map<String,DChannel> map;

    public PingPongTask(String id,Map<String,DChannel> map) {
        this.id = id;
        this.map = map;
    }

    @Override
    public void run() {
        int j = 0;
        DChannel dChannel = map.get(id);
        lable:
        while (j <= 2) {
            try {
                dChannel.writeAndFlush("0110|pin");
                dChannel.soTimeout(10000);
                int i = 0;
                while (i <= 2) {
                    String data = dChannel.readline();
                    System.out.println(data);
                    String[] datas = data.split("\\|");
                    if (datas[0].equals("0110") && datas[1].equals("pon")) {
                        if (!dChannel.isStatus()) {
                            dChannel.setStatus(true);
                        }
                        dChannel.soTimeout(0);
                        System.out.println("状态：存活");
                        break lable;
                    }
                    i++;
                }
            } catch (SocketException e) {
                //记录错误冰箱断连
                try {
                    dChannel.writeAndFlush("0110|pin");
                    j++;
                } catch (IOException e1) {
                    dChannel.setStatus(false);
                    j++;
                    System.out.println("远程io 中断 家电");

                    //记录错误 远程io 中断 冰箱
                }
            } catch (IOException e) {
                e.printStackTrace();
                //记录错误 远程io 中断 冰箱
            }
        }
    }

}
