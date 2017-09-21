package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

public class DeviceRequest extends Message{
    private int idLen;
    private String id;

    //0001|idLen*id
    public DeviceRequest(String msg) throws MessageException {
        super(msg);
        String[] msgs = this.body.split("\\*");
        if(msgs.length != 2){
            throw new MessageException("DeviceRequest body length fault");
        }
        try {
            this.idLen = Integer.parseInt(msgs[0]);
            this.id = msgs[1];
            if(idLen != id.length()){
                throw new MessageException("DeviceRequest id length fault");
            }
        }catch (NumberFormatException e){
            throw new MessageException("DeviceRequest body format fault");
        }
    }

    public String getId(){
        return this.id;
    }
}
