/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application. 通过的线程池，作用app中，作者咋这么牛i 吧
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests). //在网络请求之后磁盘读取不能一直等待，意思是有网络请求时，磁盘读取也不能停啊，那样体验多烂，所以用到多线程
 * 理解的还不透啊，继续加油
 */
public class AppExecutors { //线程池管理类，里面写了三个线程池，刺激，每个线程池都有自己的用途

    private static final int THREAD_COUNT = 3; //线程池中的线程数量

    private final Executor diskIO; //磁盘线程池吗？

    private final Executor networkIO; //网络线程池吗？

    private final Executor mainThread; //ui线程，每个Runnable交给ui线程处理，每个Task都是如此

    @VisibleForTesting
    AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO; //newFixedTreadPool，这是啥线程池来着？妈蛋，定长线程池吧？指定要3根线程
        this.mainThread = mainThread; //ui线程、这尼玛能算Executor嘛……，把Runnable交给ui线程
    }

    public AppExecutors() {
        this(new DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT), //newFixedThreadPool，是定长的线程池吗？果然是
                new MainThreadExecutor());
    }

    /**
     *
     * @return diskIO对象
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     *
     * @return networkIO对象
     */
    public Executor networkIO() {
        return networkIO;
    }

    /**
     *
     * @return ui线程对象
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * 静态内部类
     */
    private static class MainThreadExecutor implements Executor { //还整个静态内部类，服了
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper()); //UI线程的Handler啊，服了

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);  //我看无法是把Runnable放入到MessageQueue中啊，虽然在里面Runnable会被转换为Message
        } //Handler的post方法干了啥了，发过去一个Runnable对象，就是放到MessageQueue里面，交给Ui线程执行啊。。。
    }
}
