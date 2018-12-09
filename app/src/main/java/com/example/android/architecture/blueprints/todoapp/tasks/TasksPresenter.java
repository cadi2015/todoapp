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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TasksPresenter implements TasksContract.Presenter { //Tasks Presenter的实现类

    private final TasksRepository mTasksRepository; //Model

    private final TasksContract.View mTasksView; //View

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS; //Task标签，默认为ALL_TASKS

    private boolean mFirstLoad = true; //标志位，标记是否为第一次加载, 默认为true

    /**
     * 构造方法，在TasksActivity下进行的初始化
     * @param tasksRepository
     * @param tasksView
     */
    public TasksPresenter(@NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null"); //传入的M，即Repository
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null!"); //传入的V，即Fragment

        mTasksView.setPresenter(this); //这里将Presenter传到Fragment中，牛逼，因为是先创建的Fragment，所以这里靠谱，调用TasksFragment的实例方法
    }

    /**
     * 这个方法在Fragment下调用，即V下调用，目的就是加载Task，调用了loadTasks（false）方法
     */
    @Override
    public void start() {
        loadTasks(false);
    }

    /**
     * 在TasksActivity下打开的组件，即Activity后，关掉后，会调用onActivityResult，然后会调用该result（）方法
     * @param requestCode
     * @param resultCode
     */
    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        // 如果 一个 Task 成功添加， 展示一个SnackBar
        // 先判断requestCode是否为REQUEST_ADD_TASK，接着判断resultCode是否为RESULT_OK，均为True时
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage(); //调用V的展示成功保存Task的SnackBar
        }
    }

    /**
     * 加载Task的实现方法
     * @param forceUpdate 是否强制刷新
     */
    @Override
    public void loadTasks(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTasks(forceUpdate || mFirstLoad, true); //如果要求强制刷新或者firstLoad，另外要求展示加载Task时的Ui
        mFirstLoad = false;  //将首次加载Task标志位 赋值为 false
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) { //如果需要展示Loading UI
            mTasksView.setLoadingIndicator(true); //设置加载Ui
        }
        if (forceUpdate) { //如果需要强制刷新
            mTasksRepository.refreshTasks(); //调用Repository的刷新Task
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice 这是Ui自动化测试部分

        // 获取任务，new了一个数据源对象，传给getTasks（）方法，数据源对象，是个你匿名对象
        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<Task>(); //又拿一个线性表，用来过滤传进来的tasks

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle. //自动化Ui部分
                }

                // We filter the tasks based on the requestType
                for (Task task : tasks) { //哈哈遍历所有传进来的List的所有Task
                    switch (mCurrentFiltering) {  //根据不同的过滤标签
                        case ALL_TASKS:
                            tasksToShow.add(task); //加入到前面创建的要展示Task的List里面
                            break;
                        case ACTIVE_TASKS: //活动任务
                            if (task.isActive()) {  //判断Task是否为活动状态
                                tasksToShow.add(task); //如果是，加入到要展示Task的List里面
                            }
                            break;
                        case COMPLETED_TASKS: //已完成任务
                            if (task.isCompleted()) { //判断Task是否为完成状态
                                tasksToShow.add(task); //如果是，加入到要展示到线性表里
                            }
                            break;
                        default:
                            tasksToShow.add(task); //如果没有过滤标签，直接全部加入 tasksToShow里面，关键是这个枚举，有可能是空的吗？
                            break;
                    }
                }

                // The view may not be able to handle UI updates anymore
                if (!mTasksView.isActive()) { //这牛逼，还要判断Fragment有没有加入到Activity中，大神牛逼
                    return; //方法在这里中断，肯定是有目的，如果Fragment没有加入Activity中的话，直接break
                }

                if (showLoadingUI) { //如果展示过加载View
                    mTasksView.setLoadingIndicator(false); //这里把加载的View gone掉
                }

                processTasks(tasksToShow);  //把要展示的Task的List传到processTasks方法里
            }

            /**
             * Task数据如果没有获取到，回调到方法
             */
            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTasksView.isActive()) {
                    return; //如果Fragment没有加进来，直接中断方法
                }
                mTasksView.showLoadingTasksError(); //在V ，Fragment中展示加载Task错误的View
            }
        });
    }

    private void processTasks(List<Task> tasks) { //把要展示的Tasks传过来
        if (tasks.isEmpty()) { //牛币，上来就判断tasks有没有元素
            // Show a message indicating there are no tasks for that filter type.
            processEmptyTasks(); //要是一个元素也没有，就调用processEmptyTasks
        } else { //要是有元素的话
            // Show the list of tasks
            mTasksView.showTasks(tasks); //交给Fragment展示所有Task
            // Set the filter label's text.
            showFilterLabel(); //展示Fragment下的标签
        }
    }

    private void showFilterLabel() { //根据当前选中的filter，会给你展示不同的标签View
        switch (mCurrentFiltering) { //当前的过滤条件
            case ACTIVE_TASKS: //活动Task
                mTasksView.showActiveFilterLabel(); //调用V的，即Fragment下的showActiveFilterLabel（）
                break;
            case COMPLETED_TASKS: //已完成Task
                mTasksView.showCompletedFilterLabel(); //调用V的，即Fragment下的showCompletedFilterLabel()
                break;
            default:
                mTasksView.showAllFilterLabel(); //默认展示所有Task的标签
                break;
        }
    }

    private void processEmptyTasks() { //我草，连空任务时，都写的这么严谨？对啊，不同的任务类型展示不同的View
        switch (mCurrentFiltering) { //根据不同的filter，展示不同的View，作者咋这么牛逼
            case ACTIVE_TASKS:
                mTasksView.showNoActiveTasks(); //没有活动的Task时，展示中间的View
                break;
            case COMPLETED_TASKS:
                mTasksView.showNoCompletedTasks(); //没有完成的Task时，展示中间的View
                break;
            default:
                mTasksView.showNoTasks(); //没有Task时，展示中间的View
                break;
        }
    }


    /**
     * 业务逻辑，添加一条新的Task，怎么添加呢？该业务逻辑，会让TasksFragment去打开编辑页，即View上的操作
     */
    @Override
    public void addNewTask() {
        mTasksView.showAddTask();
    }

    /**
     * 打开Task详情页
     * @param requestedTask 传入要打开的Task对象
     */
    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        checkNotNull(requestedTask, "requestedTask cannot be null!"); //先判断是否为null
        mTasksView.showTaskDetailsUi(requestedTask.getId()); //拿Task的id，然后打开到Task详情页，同样是调用的Fragment下的showTaskDetailsUi方法
    }

    /**
     * 已完成任务
     * @param completedTask 传入要完成的Task
     */
    @Override
    public void completeTask(@NonNull Task completedTask) {
        checkNotNull(completedTask, "completedTask cannot be null!");
        mTasksRepository.completeTask(completedTask); //去Model里标记为已完成Task
        mTasksView.showTaskMarkedComplete(); //展示已经标记Task为completed的提示
        loadTasks(false, false);  //不知道为啥要在这里调用这个方法
    }

    /**
     * 活动Task
     * @param activeTask 要变为活动Task的Task对象
     */
    @Override
    public void activateTask(@NonNull Task activeTask) {
        checkNotNull(activeTask, "activeTask cannot be null!"); //先检查不能为空
        mTasksRepository.activateTask(activeTask); //将Task仓库中的Task先修改为活动状态，让我们进去看看
        mTasksView.showTaskMarkedActive(); //展示一个已经标记的Toast
        loadTasks(false, false); //加载Task，刷新View
    }

    /**
     *  清空已经处于Completed状态的Task
     */
    @Override
    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks(); //去仓库中清空Completed的Task，让我们进去看看都做了什么
        mTasksView.showCompletedTasksCleared();
        loadTasks(false, false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
