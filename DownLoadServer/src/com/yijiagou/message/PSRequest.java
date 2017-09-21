package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

public class PSRequest extends Message{//由ps服务器发送过来,数据有可能错误
    private String sessionId;
    private int urlSize;
    private String[] deviceIds;
    private String deviceType;
    private String appId;

    //0000|session*urlsize*deviceid1#deviceid2#deviceid3..*deviceType*appid
    public PSRequest(String msg) throws MessageException {
        super(msg);
        String[] bodys = this.body.split("\\*");
        if(bodys.length != 5){
            throw new MessageException("PSRequest length fault");
        }
        this.sessionId = bodys[0];
        this.urlSize = Integer.parseInt(bodys[1]);
        int urlsize = bodys[2].length()+bodys[3].length()+bodys[4].length()+2;
        if(urlsize != this.urlSize){
            throw new MessageException("PSRequest urlsize fault");
        }
        this.deviceIds = bodys[2].split("#");
        this.deviceType = bodys[3];
        this.appId = bodys[4];
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getUrlSize() {
        return urlSize;
    }

    public String[] getDeviceIds() {
        return deviceIds;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getAppId() {
        return appId;
    }
}
