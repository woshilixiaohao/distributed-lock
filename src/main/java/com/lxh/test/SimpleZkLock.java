package com.lxh.test;

import org.I0Itec.zkclient.IZkDataListener;

import java.util.concurrent.CountDownLatch;

/**
 * @author lixiaohao
 * @since 2020/3/24 13:45
 */
public class SimpleZkLock extends AbstractLock {

    private static final String NODE_NAME = "/test_simple_lock";

    private CountDownLatch countDownLatch;

    @Override
    public void releaseLock() {
        if (null != zkClient) {
            zkClient.delete(NODE_NAME);
            zkClient.close();
            System.out.println(Thread.currentThread().getName() + "-释放锁");
        }
    }

    @Override
    public boolean tryLock() {
        if (null == zkClient) {
            return false;
        }
        try {
            zkClient.createEphemeral(NODE_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void waitLock() {
        // 监听器
        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {

            }

            // 节点被删时回调
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                if (null != countDownLatch) {
                    countDownLatch.countDown();
                }
            }
        };
        // 注册监听
        zkClient.subscribeDataChanges(NODE_NAME, iZkDataListener);
        // 如果存在则阻塞
        if (zkClient.exists(NODE_NAME)) {
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
                System.out.println(Thread.currentThread().getName() + "-等待获取锁");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
