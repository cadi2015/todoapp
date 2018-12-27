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

/**
 * Used with the filter spinner in the tasks list.
 * 作用在Tasks列表下的过滤下拉框
 * 亲爱的枚举类
 */
public enum TasksFilterType {
    /**
     * Do not filter tasks.
     * 不要过滤Tasks
     */
    ALL_TASKS, //每个作为TaskFilterType的对象，  在枚举中的的index为 0

    /**
     * Filters only the active (not completed yet) tasks.
     * 过滤为只有活动状态的Tasks
     */
    ACTIVE_TASKS,              //index == 1

    /**
     * Filters only the completed tasks.
     * 过滤为只有完成状态的Tasks
     */
    COMPLETED_TASKS  //在枚举中，最后一个对象是不用加逗号的 index == 2
}
