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

package com.example.android.architecture.blueprints.todoapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class for a Task.
 */
@Entity(tableName = "tasks") //表结构，Task，即是表结构，每一个对象又代表一条记录
public final class Task {

    @PrimaryKey //主键
    @NonNull //value不可为空
    @ColumnInfo(name = "entryid") //字段名：entryid
    private final String mId; //id

    @Nullable
    @ColumnInfo(name = "title")
    private final String mTitle; //标题，即Task的标题

    @Nullable
    @ColumnInfo(name = "description")
    private final String mDescription; //详细描述，即Task中的内容

    @ColumnInfo(name = "completed")
    private final boolean mCompleted; //完成状态，Task的状态




    /**
     * Use this constructor to create a new active Task. //create a new active task
     * 两个参数的构造方法，还是调用了4个参数的构造方法
     * @param title       title of the task
     * @param description description of the task
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description) {
        this(title, description, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an active Task if the Task already has an id (copy of another //if the task already has an id
     * Task).
     * 三个参数的构造方法，还是调用了4个参数的构造方法
     * @param title       title of the task
     * @param description description of the task
     * @param id          id of the task
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description, @NonNull String id) {
        this(title, description, id, false);
    }

    /**
     * Use this constructor to create a new completed Task.
     * 又一个三个参数的构造方法，还是调用了4个参数的构造方法，看来参数多的构造方法，才是王道啊，最后都要调用它
     * @param title       title of the task
     * @param description description of the task
     * @param completed   true if the task is completed, false if it's active
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description, boolean completed) {
        this(title, description, UUID.randomUUID().toString(), completed);
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     * 四个参数的构造方法来了
     * @param title       title of the task 标题
     * @param description description of the task 详细描述
     * @param id          id of the task           唯一的id
     * @param completed   true if the task is completed, false if it's active 任务状态（是否活跃）
     */
    public Task(@Nullable String title, @Nullable String description,
                @NonNull String id, boolean completed) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mCompleted = completed;
    }

    /**
     * 构造方法总结（当自己去实现构造方法时，一定要先写最全状态的构造方法，其他的可以直接调用，然后不传的值，用对应的默认就好）
     *
     * 1、最全的一条记录，要id、title、description、completed状态，4个值均传入，这是构造完整记录的构造方法
     * 2、只传入title、describe、completed状态，id不传，3个参数的构造方法，最终还是调用最全记录的构造方法
     * 3、只传入title、describe、id， completed不传，同样是3个参数的构造方法，最终仍旧是调用4个记录的构造方法
     * 4、
     * 5、
     */


    /**
     * 获得id的实例方法
     * @return
     */
    @NonNull
    public String getId() {
        return mId;
    }

    /**
     * 获得标题的实例方法
     * @return
     */
    @Nullable
    public String getTitle() {
        return mTitle;
    }

    /**
     * 取标题，title不为空，就返回标题，不然就返回详细的描述
     * @return
     */
    @Nullable
    public String getTitleForList() { //要是title不为空，就从title取
        if (!Strings.isNullOrEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;  //否则就从内容里取
        }
    }

    /**
     *
     * @return 返回详细描述
     */
    @Nullable
    public String getDescription() {
        return mDescription;
    }

    /**
     *
     * @return 返回任务是否已经完成
     */
    public boolean isCompleted() {
        return mCompleted;
    }

    /**
     *
     * @return 如果不是完成状态，那就是代表是活跃状态
     */
    public boolean isActive() {
        return !mCompleted;
    }

    /**
     * 如果标题是空的，详细描述也是空的，那么Task就是空的    说明：String为null或者字符串长度为0时，isNullOrEmpty会返回true
     * @return 返回是否为空
     */
    public boolean isEmpty() { // check the task is empty 检查Task是否为空
        return Strings.isNullOrEmpty(mTitle) &&
               Strings.isNullOrEmpty(mDescription);
    }

    /**
     * 重写的equals方法，老朋友了
     * @param o 传入一个对象就对了
     * @return
     */
    @Override
    public boolean equals(Object o) {  //override the equals ,check the task
        if (this == o) return true; //要是同一个对象，直接返回true了，这个思路很好
        if (o == null || getClass() != o.getClass()) return false; //要是传进来的o指向null，或者当前的class对象与o的class不是一个，那也直接返回false
        Task task = (Task) o; //容错完了，先强转，把o转为Task
        return Objects.equal(mId, task.mId) &&  //对比id
               Objects.equal(mTitle, task.mTitle) && //对比标题
               Objects.equal(mDescription, task.mDescription); //对比详细内容
                                                               //三者都相同，返回true
    }

    /**
     *  重写的hashCode方法，老朋友
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mDescription);//直接调用Objects下的静态hashCode方法，我也是醉了
    } //override hashcode

    /**
     * 重写的toString方法
     * @return 自定义的字符串，就打印了一个Task的标题，醉了"Task with title" + "标题"
     */
    @Override
    public String toString() {
        return "Task with title " + mTitle;
    }  //override toString
}
