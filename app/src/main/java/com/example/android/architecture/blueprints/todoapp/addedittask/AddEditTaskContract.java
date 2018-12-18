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
     * 在AddEditTask页下的操作，会引起View的一些变化，对的，业务逻辑决定了View的变化，大牛太牛了
     */
    interface View extends BaseView<Presenter> {

        void showEmptyTaskError(); //界面上展示Task为Empty时的错误情况，当业务逻辑上，打开Task页后，如果Task挂了，View上要有展示

        void showTasksList(); //界面上展示Task列表吗？对的，就是这样的业务。View上就是把EditTask页面要关闭掉，就会展示TasksList页

        void setTitle(String title); //设置标题，业务逻辑上，不同的入口进来后，AddEditTask页面要根据是新建、还是编辑、展示对应的标题

        void setDescription(String description); //设置Task详情，在业务上，当进入AddEditTask页后，如果是编辑，View上需要展示要编辑的内容

        boolean isActive(); //判断当前的fragment是否已经依附到Activity上
    }

    /**
     *  具体的在AddEditTask下可以进行的动作或者行为，这些动作或者行为，会引起View的变化
     *  就是业务逻辑
     */
    interface Presenter extends BasePresenter {

        void saveTask(String title, String description); //在新增、修改页保存Task

        void populateTask(); // 填充Task详情，翻译的准确吗我？

        boolean isDataMissing(); //业务上，还要知道数据有没有意外丢失，比如进程被回收了
    }
}
