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
 * 里面会涵盖本地仓库、远程仓库
 */
public class TasksRepository implements TasksDataSource {

    private static TasksRepository INSTANCE = null; //看样子，要整个单例了

    private final TasksDataSource mTasksRemoteDataSource; //远程数据来源的Task引用

    private final TasksDataSource mTasksLocalDataSource; //本地数据来源的Task引用

    /**
     * This variable has package local visibility so it can be accessed from tests.
     * key为String、value为Task，每个Entry中放着Task呢
     */
    Map<String, Task> mCachedTasks; //缓存Tasks用的哈希表
    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     * 标志位，标记缓存是否无效，用于下一次强制更新请求的数据
     * 这个变量
     * 具有包本地可见性，因此可以从测试访问它（default权限）
     */
    boolean mCacheIsDirty = false;


    /** 私有的构造方法的目的：
     * Prevent direct instantiation. 预防直接实例化，就是预防用构造方法直接生成一个对象，大牛你真牛b
     * @param tasksRemoteDataSource
     * @param tasksLocalDataSource
     */
    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource) {
        mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource); //传进来的远程仓库对象，同样也实现了TasksDataSource接口
        mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);   //传进来的本地仓库对象，同样也实现了TasksDataSource接口
    }

    /**
     * 有了private构造方法，总得有返回对象的办法
     * Returns the single instance of this class, creating it if necessary.
     * 返回这个类的单个对象，如果它是必须的（非线程安全的单例，多线程下无法使用）
     * @param tasksRemoteDataSource the backend data source 远程仓库，也称为后端仓库（服务端）
     * @param tasksLocalDataSource  the device storage data source 本地仓库，即当前设备数据
     * @return the {@link TasksRepository} instance 仓库对象
     */
    public static TasksRepository getInstance(TasksDataSource tasksRemoteDataSource,
                                              TasksDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource); //调用构造方法
        }
        return INSTANCE; //返回的对象
    }

    /**
     * Used to force {@link #getInstance(TasksDataSource, TasksDataSource)} to create a new instance
     * next time it's called.
     * 让GC销毁对象，将对的静态的引用INSTANCE，赋值为null，这样GC发现对象没有引用指向它了，就把对象回收了
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * 从缓存Map中获得Tasks，本地仓库或者远程仓库，无论哪个先获得
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTasks(@NonNull final LoadTasksCallback callback) {
        checkNotNull(callback); //先检查LoadTasksCallback不为null

        // Respond immediately with cache if available and not dirty
        // 缓存Map不为空&&标志位是没有清空缓存
        if (mCachedTasks != null && !mCacheIsDirty) {
            callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values())); //从Map中将value都取出来，所有的value整合为一个List
                                                                            //然后传入ArrayList，以生成一个ArrayList对象
                                                                            //紧接着调用LoadTasksCallback的onTasksLoaded（）方法
                                                                            //将ArrayList传进去
            return; //走到这个分支，上面的语句执行完，这里直接中断
        }

        if (mCacheIsDirty) { //如果缓存中的是脏数据?还是没数据？这个标志位到底是干啥的？
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback); //如果cache数据不好，我们需要从网络（后端）拿取新的数据
        } else { //如果cache的数据比较理想, 从可以获得的本地数据中查询，如果还是不行，再从网络查询
            // Query the local storage if available. If not, query the network.
            mTasksLocalDataSource.getTasks(new LoadTasksCallback() {
                @Override
                public void onTasksLoaded(List<Task> tasks) {
                    refreshCache(tasks); //刷新缓存
                    callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values())); //把缓存Map中的Values，全部取出来，组成List，传给onTasksLoaded
                }

                /**
                 * 数据出错后
                 */
                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback); //从远程服务器获取数据
                }
            });
        }
    }

    /**
     * 保存Task的方法
     * @param task
     */
    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task); //先检查Task是否为null
        mTasksRemoteDataSource.saveTask(task); //远程服务器保存Task
        mTasksLocalDataSource.saveTask(task); //本地也保存Task

        // Do in memory cache update to keep the app UI up to date
        // 在内存缓存中更新，以保证应用的UI也更新
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>(); //要是缓存Map为null，就new一个对象是了
        }
        mCachedTasks.put(task.getId(), task); //没想到大神在内存到LinkedHashMap还保留了Task对象
    }

    /**
     *  完成Task
     * @param task
     */
    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);                      //检查Task不为null
        mTasksRemoteDataSource.completeTask(task); //远程仓库标记Task
        mTasksLocalDataSource.completeTask(task); //本地仓库标记Task

        //从传入的Task中取title、取详细描述、取TaskId，然后new一个Task
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);


        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), completedTask);
    }

    /**
     * 标记Task为complete的时候，调用的方法
     * @param taskId 传入一个String的TaskId即可
     *               也就是说如果你要为一个Task标记为complete,那么传TaskId、或者直接传Task对象都可以，大牛牛逼
     */
    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId); //先检查String的taskID，是不是为null，字符数量是否为0
        completeTask(getTaskWithId(taskId)); //马上调用一个重载的方法，他俩方法签名不一样， 名字一样，但参数列表不同，主要是类型
                                             //getTaskWithId(taskId),会通过id，返回对应的Task对象
                                             // 一个是 completeTask(String)      另一个是  completeTask(Task)
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

    /**
     * 一个重载方法，标记Task为activate
     * @param taskId 传入Task的id即可
     */
    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId); //检查String不为null
        activateTask(getTaskWithId(taskId)); //最终还是调用了 activateTask(Task)
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

            /**
             * 如果没有获得Task时，会被调用的方法
             */
            @Override
            public void onDataNotAvailable() {
                mTasksRemoteDataSource.getTask(taskId, new GetTaskCallback() { //去远程仓库拿Task
                    @Override
                    public void onTaskLoaded(Task task) {
                        // Do in memory cache update to keep the app UI up to date 使用内存缓存更新，以保证app中展示最新的数据
                        if (mCachedTasks == null) {
                            mCachedTasks = new LinkedHashMap<>(); // 有序的哈希表，默认是用插入Entry的顺序作为遍历元素时的顺序
                        }
                        mCachedTasks.put(task.getId(), task); // //向里面插入 key value、key是Task的id、value就是Task对象
                        callback.onTaskLoaded(task); //把Task对象传到回调的onnTaskLoaded方法
                    }

                    /**
                     * GetTaskCallback匿名内部类中的另一个抽象方法
                     *  如果去远程仓库中拿数据，没有拿到的化，就调用这个方法
                     *  最终还是调用传入的callback对象的onDataNotAvailable（）方法
                     */
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
                refreshCache(tasks); //刷新一下有序的哈希表，进入看看怎么刷的
                refreshLocalDataSource(tasks); //更新本地仓库数据
                callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));//把缓存的Task List，传入到LoadTasksCallback对象中的onTasksLoaded方法中
            }

            /**
             *  如果没有从远程仓库拿到数据
             *  就直接调用你LoadTasksCallback的onDateNotAvailable（）方法了
             */
            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Task> tasks) { //接受一个List
        if (mCachedTasks == null) { //这个内存缓存用的有序哈希表，服了啊
            mCachedTasks = new LinkedHashMap<>(); //new 一个 呗
        }
        mCachedTasks.clear(); //把有序哈希表中的元素全部干掉，即把所有Entry都干掉
        for (Task task : tasks) { //遍历传入的线性表
            mCachedTasks.put(task.getId(), task); //把List中的每一个Task对象，统统放到哈希表中，Task的id作为key，Task对象作为value
        }
        mCacheIsDirty = false; //更新标志位了，缓存是否为脏的，更新为false，即否
    }


    /**
     * 刷新本地仓库数据
     * @param tasks 接受一个Task组成的线性表
     */
    private void refreshLocalDataSource(List<Task> tasks) {
        mTasksLocalDataSource.deleteAllTasks(); //先把本地仓库中，所有的Task都删除掉
        for (Task task : tasks) { //然后把传入进来的List中的Task全部再放到本地仓库里
            mTasksLocalDataSource.saveTask(task); //风骚操作啊
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
