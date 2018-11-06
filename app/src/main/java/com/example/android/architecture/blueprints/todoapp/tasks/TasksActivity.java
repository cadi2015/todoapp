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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

/**
 * TasksRepository就是M（Model）了，TasksPresenter当然是P（Presenter）了，TasksFragment就是V（View） ，我分析的牛逼到位吗？
 * 我这个13装的尼玛真像…………
 */

public class TasksActivity extends AppCompatActivity {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private DrawerLayout mDrawerLayout; // 当前Activity的根布局

    private TasksPresenter mTasksPresenter; //这里要拿到P，看来要在Activity中创建P了，而M是在P中创建的，V也是在Activity中创建的

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_act); //设置Layout

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu); //设置左上角指示器
        ab.setDisplayHomeAsUpEnabled(true);  //设置显示home键？妈蛋？

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        TasksFragment tasksFragment =
                (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame); //通过一个占位View的id找Fragment
        if (tasksFragment == null) {  //上面找不到Fragment的话，就创建哈哈
            // Create the fragment
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity( //把Fragment交给Activity管理，并放到占位的FrameLayout上
                    getSupportFragmentManager(), tasksFragment, R.id.contentFrame);
        }

        // Create the presenter ,在这里将TasksFragment传过去了，在这里将fragment、tasksRepository以及和presenter绑定在一起，当然还创建一个TasksRepository
        mTasksPresenter = new TasksPresenter(
                Injection.provideTasksRepository(getApplicationContext()), tasksFragment); //创建一个P，把M也创建了，作为V的Fragment就更不用说了

        // Load previously saved state, if available.
        if (savedInstanceState != null) { //这里只是保留了Activity意外被干掉后，存储的一个filter
            TasksFilterType currentFiltering =
                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mTasksPresenter.setFiltering(currentFiltering); //然后传到p里，设置为崩溃前保留的FILTER
        }
    }

    /**
     * 进程被意外干掉、或者Activity Task中 Activity被意外干掉时，回调的方法
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mTasksPresenter.getFiltering()); //我去，序列化，把对象干到文件流里，从P里取filter，

        super.onSaveInstanceState(outState);   //还是调用一下基类的onSaveInstanceState方法
    }

    /**
     * 左上角item的监听器事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { //判断item
            case android.R.id.home: //如果是那个首页的item（就左上角那个）
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START); //打开左侧抽屉
                return true;
        }
        return super.onOptionsItemSelected(item); //没明白这个返回值是干鸡毛的
    }


    /**
     * 初始化左侧边栏的Item
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener( //给navigationView设置监听器对象
                new NavigationView.OnNavigationItemSelectedListener() { //这边直接new一个匿名监听器对象
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) { //开始判断点击的哪个Item了
                            case R.id.list_navigation_menu_item: //如果点击menu_item ，什么也不做
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.statistics_navigation_menu_item: //如果点击statistics的item
                                Intent intent =
                                        new Intent(TasksActivity.this, StatisticsActivity.class);
                                startActivity(intent); //跳转到StatisticsActivity
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);    //这是更新view的状态吗？麻痹
                        mDrawerLayout.closeDrawers(); //选中item后，关闭左侧边栏
                        return true;                  //返回true，代表事件消费了吧？谁知道呢？
                    }
                });
    }

    /**
     * 单元测试都写的这么严谨，服了老大了。
     * @return
     */
    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
