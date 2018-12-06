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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class StatisticsPresenter implements StatisticsContract.Presenter {

    private final TasksRepository mTasksRepository;

    private final StatisticsContract.View mStatisticsView;

    public StatisticsPresenter(@NonNull TasksRepository tasksRepository,
                               @NonNull StatisticsContract.View statisticsView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mStatisticsView = checkNotNull(statisticsView, "StatisticsView cannot be null!");

        mStatisticsView.setPresenter(this);
    }

    /**
     * 好吧，最先被调用的方法
     */
    @Override
    public void start() {
        loadStatistics(); //fragment下的onResume（），在首次执行时，就调用了onResume（）方法
    }

    private void loadStatistics() {
        mStatisticsView.setProgressIndicator(true); //先显示一个进度条

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice //自动化测试部分

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() { //去仓库里拿Task
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                int activeTasks = 0; //活动的Tasks，计数
                int completedTasks = 0; //完成的Tasks，计数

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                // 测试用的吧
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We calculate number of active and completed tasks
                for (Task task : tasks) {  //遍历Tasks
                    if (task.isCompleted()) { //如果task是已经完成的
                        completedTasks += 1; //completedTasks加1
                    } else {
                        activeTasks += 1; //否则activeTasks加1
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) { //如果fragment没有依附到Activity，直接return
                    return;
                }

                mStatisticsView.setProgressIndicator(false); //不展示进度条

                mStatisticsView.showStatistics(activeTasks, completedTasks); //调用fragment的show方法，把completed、active的数量传过去
            }

            /**
             * 如果没有获得数据
             */
            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return; //fragment没有依附到Activity上，直接中断方法
                }
                mStatisticsView.showLoadingStatisticsError(); //在fragment上展示加载错误的view
            }
        });
    }
}
