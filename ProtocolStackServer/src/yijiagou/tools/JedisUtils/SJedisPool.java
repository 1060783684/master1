package yijiagou.tools.JedisUtils;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangwei on 17-8-17.
 */
public class SJedisPool {
    private BlockingQueue<Jedis> workpool;
    private Set busy;
    private Set trunkpool;
    private int workMinNum;
    private int workMaxNum;
    private int count;
    String host;
    int port;
    public SJedisPool(int workMaxNum,int trunknum,int workMinNum,String host,int port) throws Exception {
        if(workMinNum > workMaxNum){
            throw new Exception("workMinNum surpass workMaxNum");
        }
        this.workpool = new ArrayBlockingQueue<Jedis>(workMaxNum);
        this.busy = new ConcurrentSkipListSet();
        this.trunkpool = new ConcurrentSkipListSet();
        this.workMinNum = workMinNum;
        this.workMaxNum = workMaxNum;
        this.host = host;
        this.port = port;
        for(int i = 0;i < workMinNum;i++){
            Jedis jedis = new Jedis(host,port);
            jedis.connect();
            this.workpool.put(jedis);
        }
    }

    public Jedis getConnection(){
        Jedis jedis = null;
        try {
            jedis = this.workpool.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("取超时");
        }
        if (busy.size() < workMaxNum){

        }
        return jedis;
    }

    private void newConnection(){

    }

    public void putbackConnection(Jedis jedis){

    }
}
