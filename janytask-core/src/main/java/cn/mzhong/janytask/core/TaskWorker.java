package cn.mzhong.janytask.core;

import cn.mzhong.janytask.executor.TaskExecutor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度器
 *
 * @author mzhong
 * @date 2019年7月18日
 * @since 1.0.1
 */
public class TaskWorker extends Thread {

    protected volatile boolean isShutdown = false;

    protected ExecutorService executors = Executors.newCachedThreadPool(new ThreadFactory() {
        int cnt = 0;

        public Thread newThread(Runnable r) {
            return new Thread(r, "janytask-executor" + (cnt++));
        }
    });

    protected Set<TaskExecutor> taskExecutors = new HashSet<TaskExecutor>();

    public void addExecutor(TaskExecutor executor) {
        this.taskExecutors.add(executor);
    }

    public void addExecutors(Set<TaskExecutor> executors) {
        this.taskExecutors.addAll(executors);
    }

    public TaskWorker() {
        this.setName("janytask-worker");
    }

    @Override
    public void run() {
        while (true) {
            long current = System.currentTimeMillis();
            Iterator<TaskExecutor> iterator = taskExecutors.iterator();
            while (iterator.hasNext()) {
                if (this.isShutdown) {
                    break;
                }
                TaskExecutor next = iterator.next();
                // 如果当前执行者繁忙，则跳过
                if (next.isReady()) {
                    next.setBusy();
                    executors.execute(next);
                }
            }
            long sleep = 1000 - (System.currentTimeMillis() - current);
            if (sleep > 0) {
                try {
                    sleep(sleep);
                } catch (InterruptedException e) {
                    // pass
                }
            }
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public boolean shutdownAndAwait() throws InterruptedException {
        this.isShutdown = true;
        Iterator<TaskExecutor> iterator = taskExecutors.iterator();
        while (iterator.hasNext()) {
            TaskExecutor next = iterator.next();
            synchronized (next) {
                next.notify();
            }
        }
        this.executors.shutdown();
        return this.executors.awaitTermination(1000, TimeUnit.SECONDS);
    }
}
