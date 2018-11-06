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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

/**
 * Main entry point for accessing tasks data.
 * <p>
 * For simplicity, only getTasks() and getTask() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new task is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface TasksDataSource {

    /**
     * 默认静态的内部接口
     */
    interface LoadTasksCallback { //我去，读取多个Task的回调，也要单拉出来一个接口，牛掰

        void onTasksLoaded(List<Task> tasks); //传入Tasks，加载Tasks

        void onDataNotAvailable(); //当数据没有获得时，回调的方法
    }

    /**
     * 又一个默认静态的内部接口，好处就是不用单独写一个interface文件了，其他与写一个interface文件一样
     */
    interface GetTaskCallback {  //这是获取Task的interface回调，牛掰

        void onTaskLoaded(Task task); //加载一个Task

        void onDataNotAvailable(); //当数据没有获得
    }

    void getTasks(@NonNull LoadTasksCallback callback); //返回所有的Tasks

    void getTask(@NonNull String taskId, @NonNull GetTaskCallback callback); //获得一条Task

    void saveTask(@NonNull Task task); //保存一条Task

    void completeTask(@NonNull Task task); //修改一条Task为完成状态

    void completeTask(@NonNull String taskId); //透过taskId也可以将一条Task，标记为完成状态

    void activateTask(@NonNull Task task); //活动Task，通过一条Task对象

    void activateTask(@NonNull String taskId); //活动Task，通过taskId找到

    void clearCompletedTasks(); //清空所有已完成的Task

    void refreshTasks(); //刷新Tasks

    void deleteAllTasks(); //删除所有的Tasks

    void deleteTask(@NonNull String taskId); //通过taskId，删除一条Task
}
