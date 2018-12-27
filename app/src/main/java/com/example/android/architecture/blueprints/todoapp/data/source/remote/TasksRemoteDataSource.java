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

package com.example.android.architecture.blueprints.todoapp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 * 远程数据仓库的实现类
 */
public class TasksRemoteDataSource implements TasksDataSource {

    private static TasksRemoteDataSource INSTANCE; //先把自己的引用，放到静态变量里

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000; //服务延迟耗时，静态常量

    private final static Map<String, Task> TASKS_SERVICE_DATA; //哈希表，静态常量

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2); //继续初始化一个有序的哈希表
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required."); //添加一个Task
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!"); //添加一个Task
    }

    /**
     * 获得一个TasksRemoteDataSource对象
     * 单线程下可用
     * @return
     */
    public static TasksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TasksRemoteDataSource(); //如果是多线程下，会创建多个TasksRemoteDataSource，造成大家拿到的不是同一个对象
        }
        return INSTANCE;
    }

    /**
     *  最简单的方式支持多线程
     * public static synchronized TasksRemoteDataSource getInstance() {
     *         if (INSTANCE == null) {
     *             INSTANCE = new TasksRemoteDataSource(); //如果是多线程下，会创建多个TasksRemoteDataSource，造成大家拿到的不是同一个对象
     *         }
     *         return INSTANCE;
     *     }
     */

    /**
     * 双重校验锁方式，效率高，使用当前类的class对象为锁，因为是静态方法
     * 但是有坑，大神说到了指令重排序，我不懂呀
     *
     * public static  TasksRemoteDataSource getInstance() {
     *                 if ( INSTANCE == null ) {
     *
     *                     synchronized(TasksRemoteDataSource.class) {
     *                           if( INSTANCE == null) {
     *                               INSTANCE = new TasksRemoteDataSource();
     *                           }
     *                      }
     *
     *                 }
     *            return INSTANCE;
     *          }
     */

    // Prevent direct instantiation.
    // 预防直接实例化
    private TasksRemoteDataSource() {
        super(); //我帮大神加上默认调用父类的构造方法
    }

    /**
     *
     * @param title 标题
     * @param description 内容
     */
    private static void addTask(String title, String description) {
        Task newTask = new Task(title, description); //new一个Task对象
        TASKS_SERVICE_DATA.put(newTask.getId(), newTask); //向哈希表中放入Task，key为Task的id、value为Task对象
    }

    /**
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     * @link LoadTasksCallback#onDataNotAvailable()} 这个方法就没有真正执行过
     * 在真正的远程数据来源实现， 如果服务器不能连接上或者服务器返回错误，这个onDataNotAvailable方法就会执行
     */
    @Override
    public void getTasks(final @NonNull LoadTasksCallback callback) {
        // Simulate network by delaying the execution. //模拟网络延迟执行
        Handler handler = new Handler(); //一个Handler对象
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTasksLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));//哈希表的values（）会返回Tasks的List，每个value都是一个Task对象
            }
        }, SERVICE_LATENCY_IN_MILLIS); //延迟5秒发出一个Runnable对象
    }

    /**
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getTask(@NonNull String taskId, final @NonNull GetTaskCallback callback) {
        final Task task = TASKS_SERVICE_DATA.get(taskId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTaskLoaded(task);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveTask(@NonNull Task task) {
        TASKS_SERVICE_DATA.put(task.getId(), task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASKS_SERVICE_DATA.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateTask(@NonNull Task task) {
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASKS_SERVICE_DATA.put(task.getId(), activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedTasks() {
        Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }
}
