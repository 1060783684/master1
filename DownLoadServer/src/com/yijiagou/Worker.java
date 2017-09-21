package com.yijiagou;

import com.yijiagou.config.Configurator;
import com.yijiagou.server.Server;

import java.io.IOException;

public class Worker {
    public static void main(String[] args) throws Exception {
        try {
            Server server = Server.newInstance();
            server.bind(Configurator.getHost(),Configurator.getPort());
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
