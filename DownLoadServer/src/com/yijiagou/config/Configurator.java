package com.yijiagou.config;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configurator {
    public static Map<String, String> configs;
    private static final String  INDEXPATH = "index.txt";
    private static final String POOLSPATH = "/conf/threadpools.properties";
    private static final String SERVERPATH = "/conf/server.properties";

    static {
        configs = new ConcurrentHashMap<>();
    }

    public static void init() throws Exception {
        BufferedReader in = null;
        BufferedReader br = null;
        File file = new File(INDEXPATH);
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(0, absolutePath.lastIndexOf("/", absolutePath.lastIndexOf("/") - 1));
        try {
            in = new BufferedReader(new FileReader(path + POOLSPATH));
            String conf = "";
            while ((conf = in.readLine()) != null) {
                String[] pair = conf.split("=");
                if (pair.length != 2) {
                    throw new Exception("config error");
                } else {
                    configs.put(pair[0], pair[1]);
                }
            }

            br = new BufferedReader(new FileReader(path + SERVERPATH));
            String str = "";
            while ((str = br.readLine()) != null) {
                String[] strings = str.split("=");
                if (strings.length != 2) {
                    throw new Exception("config error");
                } else {
                    configs.put(strings[0], strings[1]);
                }
            }
            getCdnUrl();//判断CDN url 是否存在
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    public static String getHost() throws Exception {
        String host = configs.get(ConfigKeyword.SERVER_HOST);
        if (host == null) {
            throw new Exception("Server host Not Found");
        }
        return host;
    }

    public static int getPort() throws Exception {
        String str = configs.get(ConfigKeyword.SERVER_PORT);
        int port = 0;
        try {
            if (str != null) {
                port = Integer.parseInt(str);
            } else {
                throw new Exception("Server port Not Found");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Server port format error!");
        }
        return port;
    }

    public static int getBosspoolNum() throws Exception {
        String str = configs.get(ConfigKeyword.BOSSPOOL_NUM);
        int num = 0;
        try {
            if (str == null) {
                throw new Exception("Bosspool num Not Found");
            }
            num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new Exception("Bosspool num format error!");
        }
        return num;
    }

    public static int getWorkerpoolNum() throws Exception {
        String str = configs.get(ConfigKeyword.WORKERPOOL_NUM);
        int num = 0;
        try {
            if (str == null) {
                throw new Exception("Workerpool num Not Found");
            }
            num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new Exception("Workerpool num format error!");
        }
        return num;
    }

    public static int getTimepoolNum() throws Exception {
        String str = configs.get(ConfigKeyword.TIMEPOOL_NUM);
        int num = 0;
        try {
            if (str == null) {
                throw new Exception("Timepool num Not Found");
            }
            num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new Exception("Timepool num format error!");
        }
        return num;
    }

    public static String getCdnUrl() throws Exception {
        String url = configs.get(ConfigKeyword.SERVER_CDN_URL);
        if (url == null) {
            throw new Exception("CDN url Not Found");
        }
        return url;
    }
}
