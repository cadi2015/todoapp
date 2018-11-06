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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 * 在View和presenter之间指定的连接器contract接口，把View和presenter放在一起，牛逼
 */
public interface TaskDetailContract {

    /**
     * 看下View的一些变化
     */
    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active); //设置加载进度条

        void showMissingTask(); //展示错过的任务

        void hideTitle(); //隐藏标题

        void showTitle(String title); //展示标题

        void hideDescription(); //隐藏详细内容

        void showDescription(String description); //展示详细描述

        void showCompletionStatus(boolean complete); //展示完成状态

        void showEditTask(String taskId); //展示编辑Task的页面，即跳转到EditTask页

        void showTaskDeleted(); //展示Task已经被删除的情况

        void showTaskMarkedComplete(); //展示Task标记完成

        void showTaskMarkedActive(); //展示Task标记活跃

        boolean isActive(); //是否活跃
    }

    /**
     * 在Task详情页下，我们会做什么
     */
    interface Presenter extends BasePresenter {

        void editTask();  //编辑Task

        void deleteTask(); //删除Task

        void completeTask(); //标记为完成的Task

        void activateTask(); //标记为活动的Task
    }
}
