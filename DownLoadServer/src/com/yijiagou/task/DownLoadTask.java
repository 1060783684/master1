package com.yijiagou.task;

import com.yijiagou.pojo.DChannel;

/**
 * Created by wangwei on 17-8-4.
 */
public class DownLoadTask implements Runnable {
    private DChannel dChannel;

    public DChannel getdChannel() {
        return dChannel;
    }

    public void setdChannel(DChannel dChannel) {
        this.dChannel = dChannel;
    }

    public DownLoadTask(DChannel dChannel){
        this.dChannel = dChannel;
    }

    @Override
    public void run() {
        dChannel.getLock1().lock();
        dChannel.setListen(true);
        dChannel.getLock1().unlock();
        dChannel.disposeEvent();
    }
}
