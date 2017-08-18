package com.yijiagou.pojo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-7-29.
 */
public class DChannel {
    private Socket socket;//lianbinxiang
    private boolean status;
    private BlockingQueue<Event> eventQueue;
    private boolean listen;
    private ReentrantLock lock;//状态
    private ReentrantLock lock1;//监听
    private ReentrantLock writelock;


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        lock.lock();
        this.status = status;
        lock.unlock();
    }


    public DChannel(Socket socket, boolean status) {
        this.socket = socket;
        this.status = status;
        this.eventQueue = new ArrayBlockingQueue(20);
        this.lock = new ReentrantLock();
        this.lock1 = new ReentrantLock();
        this.writelock = new ReentrantLock();
    }

    public boolean isListen() {
        return listen;
    }

    public void setListen(boolean listen) {
        lock1.lock();
        this.listen = listen;
        lock1.unlock();
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public ReentrantLock getLock1() {
        return lock1;
    }

    public String readline() throws IOException {
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }

    public void writeAndFlush(String data) throws IOException {
        BufferedWriter out = null;
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writelock.lock();
        out.write(data);
        out.flush();
        writelock.unlock();
    }

    public void soTimeout(int time) throws SocketException {
        this.socket.setSoTimeout(time);
    }

    public void addEvent(Event event) {
        while (!this.eventQueue.offer(event)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void disposeEvent() {
        Event event1 = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            while (true) {
                event1 = this.eventQueue.poll(10, TimeUnit.SECONDS);
                if (event1 == null) {
                    lock1.lock();
                    this.setListen(false);
                    lock1.unlock();
                    return;
                }
                br = new BufferedReader(new InputStreamReader(new FileInputStream(event1.getInfo())));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String s = "";

//-------------------------------data  start-------------------------------------------------
                StringBuffer data = new StringBuffer();
                data.append("0111|lod");
                String[] nums = event1.getInfo().split("/");
                data.append(nums[nums.length-1]+"\n");
                while ((s = br.readLine()) != null) {
                    data.append(s+"\n");
                }
                data.append("$");
//-------------------------------data  end---------------------------------------------------

                writelock.lock();
                bw.write(data.toString());
                bw.flush();
                writelock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
