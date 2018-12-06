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

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 * 指定一个contract，里面有View接口、presenter接口
 */
public interface AddEditTaskContract {

    /**
     * 在AddEditTask页下的操作，会引起View的哪些变化，View的变化
     */
    interface View extends BaseView<Presenter> {

        void showEmptyTaskError(); //界面上展示Task为Empty时的错误情况

        void showTasksList(); //界面上展示Task列表吗？艹，对，其实就是把EditTask页面关闭掉，就会展示TasksList页了

        void setTitle(String title); //设置标题

        void setDescription(String description); //设置详细描述

        boolean isActive(); //判断当前的fragment是否已经依附到Activity上
    }

    /**
     *  具体的在AddEditTask下可以进行的动作或者行为，这些动作或者行为，会引起View的变化
     */
    interface Presenter extends BasePresenter {

        void saveTask(String title, String description); //保存Task

        void populateTask(); // 填充Task，翻译的准确吗我？

        boolean isDataMissing(); //当Task 丢失
    }
}
