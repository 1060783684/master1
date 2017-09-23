package com.yijiagou;

import com.yijiagou.config.Configurator;
import com.yijiagou.server.ConsoleListener;
import com.yijiagou.server.PSServer;

import java.util.Scanner;

/**
 * Created by wangwei on 17-7-28.
 */
public class Worker {
    public static void main(String[] args){
        PSServer psServer = null;
        try {
            psServer = PSServer.newInstance();
            psServer.bind(Configurator.getServerHost(),Configurator.getServerPort());
            psServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}