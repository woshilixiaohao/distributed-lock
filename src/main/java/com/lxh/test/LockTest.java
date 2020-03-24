package com.lxh.test;

/**
 * @author lixiaohao
 * @since 2020/3/24 14:13
 */
public class LockTest {

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            new Thread(()->{
                AbstractLock abstractLock = new HighPerformanceZkLock();
//                AbstractLock abstractLock = new SimpleZkLock();
                abstractLock.getLock();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(ORDER_ID++);
                abstractLock.releaseLock();
            }).start();
        }
    }

    public static int ORDER_ID = 0;

}
