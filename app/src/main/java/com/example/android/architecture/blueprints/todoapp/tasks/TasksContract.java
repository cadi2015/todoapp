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

    interface View extends BaseView<Presenter> { //View接口也不少方法，当然别忘了，还有个setPresenter（Presenter p），在BaseView

        void setLoadingIndicator(boolean active); //展示加载Loading

        void showTasks(List<Task> tasks); //加载完Task，需要展示Task，传入一个包含Task的线性表

        void showAddTask(); //这里是show，意为展示给用户看，那就是打开新增Task页面

        void showTaskDetailsUi(String taskId); //根据任务id，打开Task详情页

        void showTaskMarkedComplete();//显示Task标记成功的Toast

        void showTaskMarkedActive(); //展示Task标记为Active状态的Toast

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

    interface Presenter extends BasePresenter { //BasePresenter里面就一个start（）方法

        void result(int requestCode, int resultCode); //结果,打开第二个组件，关掉后，会回调到该方法，回调方法

        void loadTasks(boolean forceUpdate); //加载Task，支持是否强制更新

        void addNewTask(); //添加一个新的Task

        void openTaskDetails(@NonNull Task requestedTask); //打开Task详情页

        void completeTask(@NonNull Task completedTask);  //把Task标记为已完成状态

        void activateTask(@NonNull Task activeTask); //把Task标记为活动状态

        void clearCompletedTasks();  //删除列表中的已完成Task

        void setFiltering(TasksFilterType requestType); //支持设置过滤类型，应该是Task务筛选可以设定条件

        TasksFilterType getFiltering(); //得到Task过滤类型
    }
}
