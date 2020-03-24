package com.lxh.test;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author lixiaohao
 * @since 2020/3/24 15:25
 */
public abstract class AbstractLock {

    private static final String ZK_HOST = "localhost:2181";

    public static final int SESSION_TIMEOUT = 10000;

    protected ZkClient zkClient = new ZkClient(ZK_HOST, SESSION_TIMEOUT);

    /**
     * 模板模式，分别实现抽象方法
     * 1.简单分布式锁，有性能问题
     * 2.高性能分布式锁
     */
    public void getLock() {
        String threadName = Thread.currentThread().getName();
        if (tryLock()) {
            System.out.println(threadName + "-获取锁成功");
        } else {
            System.out.println(threadName + "-获取锁失败，等待");
            waitLock();
            // 递归 重新获取锁
            getLock();
        }
    }

    /**
     * 释放锁
     */
    public abstract void releaseLock();

    /**
     * 尝试获取锁
     *
     * @return
     */
    public abstract boolean tryLock();

    /**
     * 等待锁
     */
    public abstract void waitLock();
}
