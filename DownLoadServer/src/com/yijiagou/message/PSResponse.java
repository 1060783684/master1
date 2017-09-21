package com.yijiagou.message;

import com.yijiagou.exception.MessageException;
import static com.yijiagou.message.MessageKeyword.*;

public class PSResponse extends Message{//由自己生成发送前数据不会出错
    //0000|1100;(body中0或1的个数代表家电的个数,1代表传输成功,0代表传输失败)
    public PSResponse(String msg) throws MessageException {
        super(PSCOMMAND+"|"+msg);
    }
}
