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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.BaseView;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.BasePresenter;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface TasksContract { //非要把Presenter和View放在一个interface里啊

    /**
     * View下全部是根据业务逻辑，View应该怎么显示的方法
     * 比如加载Task时，View要先有loading
     * 比如从数据库加载完Task到内存后，View要展示Task List
     * 比如要新建一个Task，View上就是要展示EditTask页给用户
     * 要查看Task的详细情况，View上就是展示Task详情页给用户
     * 标记Task为活动状态时，View上要给用户一个提示
     * 标记Task为完成状态时，View上也要给用户一个提示
     * 当业务上没有Task时，View也要展示一个空白图片给用户
     * 当加载Task出错时，View上也要给用户一个出错的图片给用户
     * 根据过滤标签，View展示对应的Task，比如已完成、正在活动的过滤标签
     * 当过滤Task没有时，View上有个容错，提示用户没有已完成Task、或者没有正在活动的Task，这些展示的都是个图片提示
     * 当用户添加Task成功后，View上也要有个提示，告诉用户你成功了
     * View上还有一个判断Task是否为活动状态（其实是View是否加载完成，不是指Task真正活动的意思）
     * 当用户要选择过滤标签时，View上要弹出一个二级菜单给用户
     */
    interface View extends BaseView<Presenter> { //View接口也不少方法，当然别忘了，还有个setPresenter（Presenter p），在BaseView

        void setLoadingIndicator(boolean active); //展示加载Loading

        void showTasks(List<Task> tasks); //加载完Task，需要展示Task，传入一个包含Task的线性表

        void showAddTask(); //这里是show，意为展示给用户看，那就是打开新增Task页面

        void showTaskDetailsUi(String taskId); //根据任务id，打开Task详情页

        void showTaskMarkedComplete();//显示Task标记成功的Toast

        void showTaskMarkedActive(); //展示Task标记为Active状态成功的Toast

        void showCompletedTasksCleared(); //展示一个已完成Task被标记为Cleared的提示，这里是个SnackBar

        void showLoadingTasksError();  //展示加载Task时出错的View

        void showNoTasks(); //展示没有Task时的view

        void showActiveFilterLabel(); //展示过滤为已活动Task的标签

        void showCompletedFilterLabel(); //展示过滤为已完成Task的标签

        void showAllFilterLabel(); //展示过滤为所有Task的标签

        void showNoActiveTasks(); //展示选择过滤后，没有正活动Task时的View

        void showNoCompletedTasks(); //展示选择过滤后，没有已完成Task的View

        void showSuccessfullySavedMessage(); //展示保存任务成功时的View,一个SnackBar

        boolean isActive(); //判断是否是活动状态，貌似是Fragment是否为Attached状态

        void showFilteringPopUpMenu(); //过滤Task时要展示的二级菜单
    }

    /**
     * Presenter下全部是业务逻辑（就是我的写业务case啊）支持的功能，比如要新建Task、设置过滤类型、删除标记为完成的任务、标记Task为完成
     * 标记Task为活动、查看Task详情、加载Task到页面
     */
    interface Presenter extends BasePresenter { //BasePresenter里面就一个start（）方法

        void result(int requestCode, int resultCode); //结果,打开第二个组件，关掉后，第二个组件传递过来值，然后会回调到该方法，这是个回调方法

        void loadTasks(boolean forceUpdate); //加载Task，支持是否强制更新,在加载Task的业务逻辑上，View上会有很多变化
                                             //加载未完成时，要展示loading、加载完成后，loading隐藏，显示Task，如果有提示，就弹出toast
                                             //这些全部放到View上去处理

        void addNewTask(); //添加一个新的Task,业务逻辑上是要打开编辑Task页

        void openTaskDetails(@NonNull Task requestedTask); //打开Task详情页，具体的业务是点击RecyclerView中的Item

        void completeTask(@NonNull Task completedTask);  //把一个Task标记为已完成状态

        void activateTask(@NonNull Task activeTask); //把一个Task标记为活动状态

        void clearCompletedTasks();  //删除列表中标记的已完成的Task

        void setFiltering(TasksFilterType requestType); //支持设置过滤的类型，应该是Task在筛选时可以设定条件

        TasksFilterType getFiltering(); //得到过滤Task的分类类型
    }
}
