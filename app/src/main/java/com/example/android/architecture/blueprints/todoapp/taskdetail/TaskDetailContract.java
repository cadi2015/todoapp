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
     * 看下在TaskDetail页面下的每个业务逻辑，会对View带来一些什么变化，View上会怎么提示用户
     */
    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active); //显示加载进度条，加载Task时，如果缓慢就调用

        void showMissingTask(); //展示丢失的任务

        void hideTitle(); //隐藏标题，业务上可能会用到隐藏标题吗？

        void showTitle(String title); //展示标题，有隐藏，当然就有显示了。

        void hideDescription(); //隐藏详细内容

        void showDescription(String description); //展示详细描述

        void showCompletionStatus(boolean complete); //展示完成状态

        void showEditTask(String taskId); //展示编辑Task的页面，即跳转到EditTask页

        void showTaskDeleted(); //展示Task已经被删除的提示，业务上有在详情页删除的逻辑，View上要有体现

        void showTaskMarkedComplete(); //展示Task标记完成，业务上可以标记Task为完成状态，View上给用户一个提示

        void showTaskMarkedActive(); //展示Task标记活跃，业务上标记Task为活跃状态时，View上同样给用户一个提示

        boolean isActive(); //是否活跃，还是判断Fragment是否已经依附到Activity上面
    }

    /**
     * 在Task详情页下，我们会做什么，即当前TaskDetail页面下需求的业务逻辑会有哪些
     * 我记得小强喜欢把服务器的交互逻辑也放在Presenter里面，这样是有道理的
     */
    interface Presenter extends BasePresenter {

        void editTask();  //编辑Task，跳转到AddEditTask页面

        void deleteTask(); //删除Task，直接删除掉Task，在业务上是要关闭当前的TaskDetail页的，这些都交给View去做

        void completeTask(); //标记Task为完成状态

        void activateTask(); //标记Task为活动状态
    }
}
