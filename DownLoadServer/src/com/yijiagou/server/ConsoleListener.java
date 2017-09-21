package com.yijiagou.server;

import com.yijiagou.config.Log4JConfig;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.util.Scanner;

public class ConsoleListener implements Runnable {
    private Scanner in;

    public ConsoleListener() {
        in = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            String comment = in.nextLine();
            if ("log change".equals(comment)) {
                Log4JConfig.load();
            }
        }
    }

}
