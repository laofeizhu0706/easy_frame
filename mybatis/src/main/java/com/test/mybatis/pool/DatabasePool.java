package com.test.mybatis.pool;

import com.test.mybatis.config.Configuration;

import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class DatabasePool {
    /**
     * 初始化连接数
     */
    private static final int INIT_LINK = 5;

    private static final int MAX_LINK = 20;

    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);


    /**
     * 连接池队列
     */
    private static ArrayBlockingQueue<Connection> arrayBlockingQueue;

    static {
        arrayBlockingQueue = new ArrayBlockingQueue<>(INIT_LINK);
        for (int i = 0; i < INIT_LINK; i++) {
            arrayBlockingQueue.offer(Configuration.build("database.xml"));
        }
    }

    private synchronized void extend() {
        if (arrayBlockingQueue.size() <= 20) {
            int extendNum = INIT_LINK;
            if (arrayBlockingQueue.size() > MAX_LINK - INIT_LINK) {
                extendNum = MAX_LINK - INIT_LINK;
            }
            for (int i = 0; i < extendNum; i++) {
                arrayBlockingQueue.offer(Configuration.build("database.xml"));
            }
        }
    }

    /**
     * 内部类实现单例
     */
    private static class SingletonHolder {
        private static final DatabasePool INSTANCE = new DatabasePool();
    }

    /**
     * 获得一个数据库连接池
     *
     * @return
     */
    public static DatabasePool getDatabasePool() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获得实例
     * @return
     * @throws InterruptedException
     */
    public synchronized Connection getConnection() throws InterruptedException {
        if (arrayBlockingQueue.isEmpty()) {
            if (arrayBlockingQueue.size() <= MAX_LINK) {
                extend();
                return arrayBlockingQueue.poll();
            } else {
                atomicBoolean.set(true);
                while (atomicBoolean.get()) {
                    this.wait();
                }
                return arrayBlockingQueue.poll();
            }
        } else {
            return arrayBlockingQueue.poll();
        }
    }

    /**
     * 回收
     * @param connection
     * @return
     */
    public synchronized void recycle(Connection connection) {
        if(arrayBlockingQueue.size()>=MAX_LINK) {
            atomicBoolean.set(false);
            this.notifyAll();
        } else {
            arrayBlockingQueue.offer(connection);
        }
    }

}
