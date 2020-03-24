package com.lxh.test;

import com.sun.org.apache.regexp.internal.RE;
import org.I0Itec.zkclient.IZkDataListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 高性能锁，先创建永久节点，再创建临时顺序节点
 *
 * @author lixiaohao
 * @since 2020/3/24 14:49
 */
public class HighPerformanceZkLock extends AbstractLock {
    // 首先要创建永久节点  zkClient.createPersistent(PATH)
    public static final String PATH = "/high";
    // 当前节点路径
    private String currentPath;
    // 前一个节点路径
    private String beforePath;

    private CountDownLatch countDownLatch;

    @Override
    public void releaseLock() {
        if (null != zkClient) {
            zkClient.delete(currentPath);
            zkClient.close();
            System.out.println(Thread.currentThread().getName() + "-释放锁");
        }
    }

    @Override
    public boolean tryLock() {
        // 如果当前路径为空，尝试获取锁
        if (null == currentPath || "".equals(currentPath)) {
            // 在path下创建一个临时顺序节点
            //
            currentPath = zkClient.createEphemeralSequential(PATH + "/", "lock");
        }
        // 获取所有子节点排序
        // children [0000000201, 0000000202, 0000000203, 0000000204, 0000000205, 0000000206, 0000000207, 0000000208...]
        List<String> children = zkClient.getChildren(PATH);
        Collections.sort(children);
        // 如果当前节点是第一个节点，则获得锁
        if (currentPath.equals(PATH + "/" + children.get(0))) {
            return true;
        } else {//如果不是排名第一，就把前一个节点的名称复制给beforePath
            int pathLength = PATH.length();
            int wz = Collections.binarySearch(children, currentPath.substring(pathLength + 1));
            beforePath = PATH + "/" + children.get(wz - 1);
        }
        return false;
    }

    @Override
    public void waitLock() {
        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {

            }
            // 监听删除的锁
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                if (null != countDownLatch) {
                    countDownLatch.countDown();
                }
            }
        };
        // 监听前一个节点变化
        zkClient.subscribeDataChanges(beforePath, iZkDataListener);
        // 如果存在前一个节点则阻塞
        if (zkClient.exists(beforePath)) {
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        zkClient.unsubscribeDataChanges(beforePath, iZkDataListener);
    }
}
