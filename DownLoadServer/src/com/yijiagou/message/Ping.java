package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

import static com.yijiagou.message.MessageKeyword.*;

public class Ping extends Message{
    public Ping() throws MessageException {
        super(PINGPONG+"|pin");
    }

    public String toString(){
        StringBuffer message = new StringBuffer();
        message.append(super.toString());
        message.append("$");
        return  message.toString();
    }
}
