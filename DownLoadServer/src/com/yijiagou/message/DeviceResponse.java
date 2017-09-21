package com.yijiagou.message;
import com.yijiagou.exception.MessageException;

import static com.yijiagou.message.MessageKeyword.*;

public class DeviceResponse extends Message {
    public DeviceResponse(String msg) throws MessageException {
        super(CONNECTION+"|"+msg);
    }

    public String toString(){
        StringBuffer message = new StringBuffer();
        message.append(super.toString());
        message.append("$");
        return  message.toString();
    }
}
