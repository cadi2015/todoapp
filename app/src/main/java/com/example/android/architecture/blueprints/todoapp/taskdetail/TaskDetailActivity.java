/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

/**
 * Displays task details screen.
 * 显示Task详情的页面
 */
public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "TASK_ID"; //先就整个常量，来干什么

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //调用基类的onCreate（）方法，掉不掉的

        setContentView(R.layout.taskdetail_act); //初始化布局

        // Set up the toolbar. 初始化Tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        // Get the requested task id
        // 获取发起请求的Task id
        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID); //从Intent中拿出来就行

        TaskDetailFragment taskDetailFragment = (TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame); //通过布局id，获取Fragment

            if (taskDetailFragment == null) {// 如果没有拿到Fragment
            taskDetailFragment = TaskDetailFragment.newInstance(taskId); //那就只好创建一个Fragment了

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    taskDetailFragment, R.id.contentFrame); //将Fragment加入到Activity中
        }

        // Create the presenter 创建TaskDetail的Presenter
        new TaskDetailPresenter(
                taskId, //把Task id传过去
                Injection.provideTasksRepository(getApplicationContext()),
                taskDetailFragment); //打开本地记录的仓库
    }

    /**
     * 支持导航键？
     * @return 调用back键后，返回true
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //同back键按下效果
        return true; //返回true
    }
}
