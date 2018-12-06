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

package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 * 在View和presenter之间指定一个contract用来，哈哈，管理他们呗
 */
public interface StatisticsContract {

    /**
     *  动作之后，View的变化
     */
    interface View extends BaseView<Presenter> {

        void setProgressIndicator(boolean active); //设置显示进度条

        void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks); //展示统计

        void showLoadingStatisticsError(); //展示加载统计错误的信息

        boolean isActive(); //判断fragment是否已经依附到Activity上
    }

    /**
     * 在Statistics页下的能有哪些动作呢？只是实现BasePresenter，看来这个页面只是做了一个展示的用途
     */
    interface Presenter extends BasePresenter {

    }

}
