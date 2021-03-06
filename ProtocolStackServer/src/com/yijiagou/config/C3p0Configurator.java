package com.yijiagou.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class C3p0Configurator {
    private final static String filename = "c3p0.properties";
    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, String> readConfig() {
        BufferedReader br = null;
        try {
            File file = new File("index.txt");
            String indexpath=file.getAbsolutePath();
            int a=indexpath.lastIndexOf("/",indexpath.lastIndexOf("/")-1);
            String filepath=indexpath.substring(0,a);
            br = new BufferedReader(new FileReader(filepath+"/conf/"+filename));
            String str = "";
            while ((str = br.readLine()) != null) {
                String[] strings = str.split("=");
                map.put(strings[0], strings[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
