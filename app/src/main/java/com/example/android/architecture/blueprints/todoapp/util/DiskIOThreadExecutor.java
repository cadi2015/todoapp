package com.example.android.architecture.blueprints.todoapp.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor that runs a task on a new background thread.
 */
public class DiskIOThreadExecutor implements Executor { //Executor，线程池的底层interface
    private final Executor mDiskIO;

    /**
     * 初始化一个线程池，服了，就一个线程的线程池，好处是？
     */
    public DiskIOThreadExecutor() {
        mDiskIO = Executors.newSingleThreadExecutor(); //只有一个线程的线程池，醉了，作者牛逼，这么小一个app，啥都有
    }

    /**
     *  重写的execute，用于在线程池中执行任务，牛13
     * @param command
     */
    @Override
    public void execute(@NonNull Runnable command) {
        mDiskIO.execute(command);
    }
}
