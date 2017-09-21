package com.yijiagou.message;

public class MessageKeyword {
    public static final String PSCOMMAND = "0000";//ps服务器发来的命令报头,也是回馈ps服务器时的报头
    public static final String CONNECTION = "0001";//家居连接时发送的报头,也是回复家居时的报头
    public static final String DEVICECOMMAND = "0010";//给家居发送的命令报头,也是家居回复时的报头
    public static final String PINGPONG = "0011";//与家居pingpong时的报头
    public static final String OK = "ok";
    public static final String ERROR = "err";
}
