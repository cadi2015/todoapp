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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and updates
 * the UI as required.
 * AddEditTask页用的Presenter
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter,
        TasksDataSource.GetTaskCallback {

    @NonNull
    private final TasksDataSource mTasksRepository; //Task仓库对象，TasksDataSource

    @NonNull
    private final AddEditTaskContract.View mAddTaskView; //AddEditTaskContract.View对象

    @Nullable
    private String mTaskId; //TaskId对象

    private boolean mIsDataMissing; //是否数据丢失的标志位

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param taskId ID of the task to edit or null for a new task
     * @param tasksRepository a repository of data for tasks
     * @param addTaskView the add/edit view
     * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
     */
    public AddEditTaskPresenter(@Nullable String taskId, @NonNull TasksDataSource tasksRepository,
            @NonNull AddEditTaskContract.View addTaskView, boolean shouldLoadDataFromRepo) {
        mTaskId = taskId; //传进来的Task id
        mTasksRepository = checkNotNull(tasksRepository); //传进来的Tasks仓库对象
        mAddTaskView = checkNotNull(addTaskView); //传进来的fragment对象，因为fragment对象实现了AddEditTaskContract.View
        mIsDataMissing = shouldLoadDataFromRepo; //从仓库加载数据时，如果数据错过了

        mAddTaskView.setPresenter(this); //调用fragment中的setPresenter方法，把当前的Presenter对象传过去。setPresenter是AddEditTaskContract.View的接口方法，fragment实现了
    }

    /**
     * fragment创建后，会在它的onResume中调用start（）方法
     *
     */
    @Override
    public void start() {
        if (!isNewTask() && mIsDataMissing) { //如果不是新的Task、又有数据丢失了
            populateTask();
        }
    }

    /**
     * 保存Task
     * @param title 标题
     * @param description 内容
     */
    @Override
    public void saveTask(String title, String description) {
        if (isNewTask()) { //如果新的Task
            createTask(title, description); //创建Task
        } else {
            updateTask(title, description); //不是新的Task，那就是更新Task
        }
    }

    /**
     * 这个方法是，从仓库获取Task吗？populate这个单词要怎么解释，确实是，如果不是新的Task，那就从仓库中拿Task
     */
    @Override
    public void populateTask() {
        if (isNewTask()) { //如果是新的Task
            throw new RuntimeException("populateTask() was called but task is new."); //抛出RuntimeException异常，提示为"populateTask() was called but task is new."
        }
        mTasksRepository.getTask(mTaskId, this); //旧的Task，通过TaskId，调用Task仓库对象的getTask
        //getTask方法需要一个GetTaskCallback对象，我们传this进去就可以了，因为 AddEditTaskPresenter 实现了GetTaskCallback接口
    }

    /**
     * GetTaskCallback下的接口方法，这个方法在mTasksRepository中的getTask方法中会被调用哦
     * @param task 要传入的Task对象
     */
    @Override
    public void onTaskLoaded(Task task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) { //如果fragment已经依附到Activity中
            mAddTaskView.setTitle(task.getTitle()); //调用其设置标题
            mAddTaskView.setDescription(task.getDescription()); //调用其设置详细描述
        }
        mIsDataMissing = false; //并且把标志
    }

    /**
     *  当数据，即Task没有获得到的时候
     */
    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {  //首先fragment得依附到Activity上吧
            mAddTaskView.showEmptyTaskError(); //展示一个错误的Task Error
        }
    }

    /**
     *  判断数据是否丢失
     * @return 丢失状态标志位
     */
    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    /**
     * 判断是否为新的Task，如果mTaskId为null的话，则为新的Task
     * @return
     */
    private boolean isNewTask() {
        return mTaskId == null;
    }

    /**
     * 创建Task的方法
     * @param title 标题
     * @param description 详细内容
     */
    private void createTask(String title, String description) {
        Task newTask = new Task(title, description); //先new个Task对象，传入了标题和描述
        if (newTask.isEmpty()) { //判断Task是否为空
            mAddTaskView.showEmptyTaskError(); //如果为空，展示空Task的Error提示
        } else {  //不为空
            mTasksRepository.saveTask(newTask); //放入Task仓库中，存储Task
            mAddTaskView.showTasksList(); //马上finish掉当前的edit页，展示TasksList页
        }
    }

    /**
     * 更新Task的方法
     * @param title 要更新的标题
     * @param description 要更新的详细描述
     */
    private void updateTask(String title, String description) {
        if (isNewTask()) { //如果是新的Task
            throw new RuntimeException("updateTask() was called but task is new."); //直接抛出RuntimeException异常，给的描述："updateTask() was called but task is new."
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId)); //去仓库保存Task
        mAddTaskView.showTasksList(); // After an edit, go back to the list. //更新完了之后，back返回到Task列表页
    }
}
