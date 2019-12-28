/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Task;

/**
 * The Room Database that contains the Task table. //这个Room 数据库我也是第一次见啊
 */
@Database(entities = {Task.class}, version = 1) //注解指明了表的类，以及数据库版本
public abstract class ToDoDatabase extends RoomDatabase {

    private static ToDoDatabase INSTANCE;

    public abstract TasksDao taskDao();  //TasksDao作为对Task的操作一些方法，就是增删改查都在里面（业务逻辑）

    private static final Object sLock = new Object(); //创建一个对象，用作锁

    public static ToDoDatabase getInstance(Context context) {
        synchronized (sLock) { //我去，这里还用了对象锁撒，哪根线程拿到锁，哪根线程才能执行该代码块
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        ToDoDatabase.class, "Tasks.db") //还要Class对象，我去,很明显这里创建了名为Tasks.db的数据库
                        .build();
            }
            return INSTANCE;
        }
    }

}
