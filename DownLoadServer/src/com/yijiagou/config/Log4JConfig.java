package com.yijiagou.config;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;


public class Log4JConfig {
    private final static String filename = "log.properties";
    private static String path;
    static {
        File file = new File("index.txt");
        String indexpath=file.getAbsolutePath();
        int a=indexpath.lastIndexOf("/",indexpath.lastIndexOf("/")-1);
        String filepath=indexpath.substring(0,a);
        path=filepath+"/conf/"+filename;
    }

    public static void load(){
        PropertyConfigurator.configure(path);
    }
}

