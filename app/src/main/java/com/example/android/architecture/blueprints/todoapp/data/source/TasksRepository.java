/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data.source;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 * Tasks仓库类，实现了TasksDataSource接口
 */
public class TasksRepository implements TasksDataSource {

    private static TasksRepository INSTANCE = null;

    private final TasksDataSource mTasksRemoteDataSource;

    private final TasksDataSource mTasksLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Task> mCachedTasks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource) {
        mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TasksRepository} instance
     */
    public static TasksRepository getInstance(TasksDataSource tasksRemoteDataSource,
                                              TasksDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TasksDataSource, TasksDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTasks(@NonNull final LoadTasksCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedTasks != null && !mCacheIsDirty) {
            callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mTasksLocalDataSource.getTasks(new LoadTasksCallback() {
                @Override
                public void onTasksLoaded(List<Task> tasks) {
                    refreshCache(tasks);
                    callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.saveTask(task);
        mTasksLocalDataSource.saveTask(task);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), task); //没想到大神在内存到LinkedHashMap还保留了Task对象
    }

    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.completeTask(task);
        mTasksLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        completeTask(getTaskWithId(taskId));
    }

    /**
     *  将Task更新为Active状态
     * @param task  要更新的Task对象
     */
    @Override
    public void activateTask(@NonNull Task task) {
        checkNotNull(task); //先判断Task对象是否为null
        mTasksRemoteDataSource.activateTask(task); //先去标记远程仓库中的Task，作者大牛是用一个LinkedHashMap在内存中模拟的，理解成服务器上的Task即可
        mTasksLocalDataSource.activateTask(task);  //再去标记本地仓库（数据库中）的Task

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId()); //然后new一个新的Task对象，要把传入的Task的title、描述、id都给新的Task对象

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) { //如果内存中缓存的mCachedTask为空
            mCachedTasks = new LinkedHashMap<>(); //new一个LinkedHashMap对象
        }
        mCachedTasks.put(task.getId(), activeTask); //用task的id作为key，Task对象作为value，放入到缓存的Map中
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        activateTask(getTaskWithId(taskId));
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks(); //先清空模拟的远程仓库中的Task
        mTasksLocalDataSource.clearCompletedTasks(); //再次清苦本地数据库中保存的Task

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>(); //如果缓存的mCachedTasks对象为空，那就new一个好了，如果没有缓存的Task，那肯定为null啊
        }

        Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator(); //先拿cachedTasks的由Map.Entry组成的Set，然后再找Set的迭代器
        while (it.hasNext()) {  //开始遍历，每一个元素为Map.Entry
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) { //value就是Task，如果Task的状态为Completed
                it.remove(); //干掉该元素，元素为整个Map.Entry,即从LinkedHashMap中干掉一个元素
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     * 从本地数据中，获取Task
     * taskId 传入的Task id，一个String对象
     * callback 一个GetTaskCallback对象
     * 对这个方法有点懵逼啊
     */
    @Override
    public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
        checkNotNull(taskId); //先检查是否为null
        checkNotNull(callback); //继续检查callback

        Task cachedTask = getTaskWithId(taskId); //通过id，获取到Map中缓存的Task对象，让我们进去看看getTaskWithId（id）方法是怎么做的

        // Respond immediately with cache if available
        if (cachedTask != null) { //如果获取到Map中缓存的Task后
            callback.onTaskLoaded(cachedTask); //调用GetTaskCallback中的 onTaskLoaded（）方法
            return; //方法结束掉
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        // 牛逼，方法内调用getTask（id，callback），递归用法啊，大神牛逼啊
        mTasksLocalDataSource.getTask(taskId, new GetTaskCallback() { //就用传入的taskId，一个GetTaskCallback的匿名对象（也可成为匿名内部类，毕竟实现了GetTaskCallback接口嘛）
            @Override
            public void onTaskLoaded(Task task) { //这Task，传入的是Map中缓存的那个Task啊
                // Do in memory cache update to keep the app UI up to date
                if (mCachedTasks == null) {
                    mCachedTasks = new LinkedHashMap<>();
                }
                mCachedTasks.put(task.getId(), task); //只有Task不为null的时候，才会调用这个方法嘛
                callback.onTaskLoaded(task);
            }

            @Override
            public void onDataNotAvailable() {
                mTasksRemoteDataSource.getTask(taskId, new GetTaskCallback() {
                    @Override
                    public void onTaskLoaded(Task task) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedTasks == null) {
                            mCachedTasks = new LinkedHashMap<>();
                        }
                        mCachedTasks.put(task.getId(), task);
                        callback.onTaskLoaded(task);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    /**
     * 刷新Tasks，更新标志位为true
     */
    @Override
    public void refreshTasks() {
        mCacheIsDirty = true;
    }

    /**
     *  删除所有Task
     */
    @Override
    public void deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks(); //先把远程仓库（服务器）的Tasks都干掉
        mTasksLocalDataSource.deleteAllTasks(); //再把本地数据库中都Tasks都干掉

        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>(); //为了防止mCachedTasks为空，就加了判断
        }
        mCachedTasks.clear(); //清空内存中缓存的Task
    }

    /**
     * 根据taskId删除Task
     * @param taskId 要传入的TaskId
     */
    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId)); //先删除远程仓库的Task
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId)); //再删除本地数据库中的Task

        mCachedTasks.remove(taskId); //如果缓存中也有的话，连内存缓存中的也要干掉，完美
    }

    /**
     * 从远程仓库获得Tasks
     * @param callback 一个LoadTasksCallback对象
     */
    private void getTasksFromRemoteDataSource(@NonNull final LoadTasksCallback callback) {
        mTasksRemoteDataSource.getTasks(new LoadTasksCallback() { //调用远程仓库的任务
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                refreshCache(tasks);
                refreshLocalDataSource(tasks);
                callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Task> tasks) {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.clear();
        for (Task task : tasks) {
            mCachedTasks.put(task.getId(), task);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Task> tasks) {
        mTasksLocalDataSource.deleteAllTasks();
        for (Task task : tasks) {
            mTasksLocalDataSource.saveTask(task);
        }
    }

    /**
     *
     * @param id 要传入的Task id
     * @return Task对象
     */
    @Nullable
    private Task getTaskWithId(@NonNull String id) {
        checkNotNull(id); //还是检查id是否为null
        if (mCachedTasks == null || mCachedTasks.isEmpty()) { //要检查缓存的Map哈，如果Map为null或者Map里面有0个Map.Entry
            return null;  //直接返回null
        } else {
            return mCachedTasks.get(id); //从Map中，通过id（即是Map中的key），获取Task
        }
    }
}
