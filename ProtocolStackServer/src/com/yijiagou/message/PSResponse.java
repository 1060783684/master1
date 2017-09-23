package com.yijiagou.message;

import com.yijiagou.exception.MessageException;


public class PSResponse extends Message {

    //0000|err
    //
    public PSResponse(String msg) throws MessageException {
        super(msg);
    }

    @Override
    public String getHead() {
        return super.getHead();
    }

    @Override
    public String getBody() {
        return super.getBody();
    }
}
