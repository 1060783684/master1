package com.yijiagou.server;

import com.yijiagou.config.Log4JConfig;
import org.apache.log4j.PropertyConfigurator;

import java.util.Scanner;


public class ConsoleListener implements Runnable {
    Scanner in;

    public ConsoleListener() {
        in = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            String comment = in.nextLine();
            if ("log change".equals(comment)) {
                Log4JConfig.load();
                System.out.println("log change ok!");
            } else {
            }
        }
    }
}
