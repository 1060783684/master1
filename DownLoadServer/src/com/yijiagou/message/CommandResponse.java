package com.yijiagou.message;
import com.yijiagou.exception.MessageException;

import static com.yijiagou.message.MessageKeyword.*;

public class CommandResponse extends Message{//由家电发过来,数据可能产生错误
    private boolean isOk;
    private boolean isError;

    public CommandResponse(String msg) throws MessageException {
        super(msg);
        if(OK.equals(this.body)){
            this.isOk = true;
        }else {
            this.isError = true;
        }
    }

    public boolean isOk(){
        return this.isOk;
    }

    public boolean isError(){
        return this.isError;
    }
}
