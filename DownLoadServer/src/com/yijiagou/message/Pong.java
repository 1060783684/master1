package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

import static com.yijiagou.message.MessageKeyword.*;

public class Pong extends Message {
    public Pong(String msg) throws MessageException {
        super(msg);
    }
}
