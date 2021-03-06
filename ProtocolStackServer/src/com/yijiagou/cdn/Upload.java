package com.yijiagou.cdn;

import com.yijiagou.config.Configurator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Upload {
    public static String uploadFile(String filePath,StringBuffer fileContent,String infoPath,String infoContent){
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        String result="";
        try {
            String cdnurl = Configurator.getCDNUrl();
            String[] uri = cdnurl.split("/");
            String host = uri[0];
            String path = cdnurl.substring(cdnurl.indexOf('/')+1,cdnurl.length());

            String databody = "-----------------------------12401097644543236151477456898\r\n"+
                    "Content-Disposition: form-data; name=\"upfile\"; filename=\""+filePath+"\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n";

            String databody1 = "-----------------------------12401097644543236151477456898\r\n"+
                    "Content-Disposition: form-data; name=\"upfile1\"; filename=\""+infoPath+"\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n";

            String dataEnd = "\r\n-----------------------------12401097644543236151477456898--\r\n";

            int length = databody.getBytes("UTF-8").length +
                    dataEnd.getBytes("UTF-8").length +
                    fileContent.toString().getBytes("UTF-8").length +
                    "\r\n".getBytes("UTF-8").length+
                    databody1.getBytes("UTF-8").length+
                    infoContent.toString().getBytes("UTF-8").length;

            String dataStart = "POST /"+path+" HTTP/1.1\r\n" +
                    "Host: "+host+"\r\n" +
                    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                    "Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3\r\n" +
                    "Accept-Encoding: gzip, deflate\n" +
                    "Content-Type: multipart/form-data; boundary=---------------------------12401097644543236151477456898\r\n" +
                    "Content-Length: "+length+"\r\n" +
                    "Connection: keep-alive\r\n" +
                    "Upgrade-Insecure-Requests: 1\r\n" +
                    "\r\n";
            String[] hostinfo = host.split(":");
            String host0 = hostinfo[0];
            String post0 = hostinfo[1];

            Socket socket = new Socket(host0,Integer.parseInt(post0));
            socket.setSoTimeout(1000);
            bos = new BufferedOutputStream(socket.getOutputStream());

            bos.write(dataStart.getBytes("UTF-8"));
            bos.write(databody.toString().getBytes("UTF-8"));
            bos.write(fileContent.toString().getBytes("UTF-8"));
            bos.write("\r\n".toString().getBytes("UTF-8"));
            bos.write(databody1.toString().getBytes("UTF-8"));
            bos.write(infoContent.toString().getBytes("UTF-8"));
            bos.write(dataEnd.getBytes("UTF-8"));

            bos.flush();

            bis = new BufferedInputStream(socket.getInputStream());
            boolean b = false;
            int number = 0;
            int p = 0;
            byte[] bs = new byte[128];//用于存储临时信息
            byte[] pk = null;

            do{
                number = bis.read(bs,0,bs.length);
                p += number;
                byte[] pk0;
                if(pk == null){
                    pk0 = new byte[number];
                    System.arraycopy(bs,0,pk0,0,number);
                }else {
                    pk0 = pk;
                    b = true;
                }
                pk = new byte[p];


                System.arraycopy(pk0,0,pk,0,pk0.length);//将旧数组拷到新数组中

                if(b)
                    System.arraycopy(bs,0,pk,pk0.length,number);//将临时信息存入大数组中
            }while (number == bs.length);

            TranResponse response  = new TranResponse(pk);
            String message = response.getBody();

            System.out.println(path);
            System.out.println(response.getMessage());

            String[] messages = message.split("\n");
            if((messages[1].equalsIgnoreCase("1") || messages[1].equalsIgnoreCase("1\r")) &&
                    messages[2].equalsIgnoreCase("1") || messages[2].equalsIgnoreCase("1\r")){
                result = "1";
            }else {
                result = "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bis != null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
