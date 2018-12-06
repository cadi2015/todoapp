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

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

/**
 * Show statistics for tasks.
 * 展示Tasks的统计页，这么牛逼
 * 入口在 TasksActivity下的options item上
 * 看来TasksActivity、StatisticsActivity确实是
 */
public class StatisticsActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;  //View引用

    /**
     * 生命周期方法
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.statistics_act); //初始化布局

        // Set up the toolbar.初始化tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.statistics_title); //设置标题
        ab.setHomeAsUpIndicator(R.drawable.ic_menu); //设置图标
        ab.setDisplayHomeAsUpEnabled(true); //设置显示Home按钮可以返回吗？

        // Set up the navigation drawer. 初始化DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark); //设置状态栏颜色
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view); //获取DrawerLayout顶部的那块View
        if (navigationView != null) {
            setupDrawerContent(navigationView); //初始化DrawerLayout左侧边栏的所有View
        }

        StatisticsFragment statisticsFragment = (StatisticsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame); //通过Layout的id，获取fragment
        if (statisticsFragment == null) {  //如果fragment为null
            statisticsFragment = StatisticsFragment.newInstance(); //创建一个fragment对象
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), //将fragment依附到Activity下指定Layout的id处
                    statisticsFragment, R.id.contentFrame); //不通xml文件中的View，id可以相同，Android可以识别出来
        }

        new StatisticsPresenter( //这里创建Presenter
                Injection.provideTasksRepository(getApplicationContext()), statisticsFragment); //创建TasksRepository，
    }

    /**
     * 点击Options item，会调用该方法
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //home键
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START); //当从Tool bar 选择 home图标时，打开左侧抽屉
                return true; //返回true
        }
        return super.onOptionsItemSelected(item); //调用基类的方法，还不知道里面干了啥
    }

    /**
     * 设置左侧边栏的View
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                NavUtils.navigateUpFromSameTask(StatisticsActivity.this); //这个方法值得深究一下啊
                                break; //感觉是返回同一个Task栈中的上一个Activity啊
                            case R.id.statistics_navigation_menu_item:
                                // Do nothing, we're already on that screen
                                break; //点击当前页的item，什么都不做，我们已经在这个屏幕了
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true); //当drawerLayout的item被选择的时候，色纸menuItem的checked状态为true
                        mDrawerLayout.closeDrawers(); //关闭左侧边栏
                        return true; //返回true，事件被消费了吗
                    }
                });
    }
}
