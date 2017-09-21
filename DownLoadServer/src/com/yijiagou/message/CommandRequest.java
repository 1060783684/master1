package com.yijiagou.message;

import com.yijiagou.exception.MessageException;
import static com.yijiagou.message.MessageKeyword.*;

public class CommandRequest extends Message{//由自己产生,发送前不会出错
    private int pathLen;
    private String path;

    public CommandRequest(String path) throws MessageException {
        super(DEVICECOMMAND+"|"+path.length()+"*"+path);
        this.pathLen = path.length();
        this.path = path;
    }

    public int getPathLen() {
        return pathLen;
    }

    public String getPath() {
        return path;
    }

    public String toString(){
        StringBuffer message = new StringBuffer();
        message.append(super.toString());
        message.append("$");
        return  message.toString();
    }
}
