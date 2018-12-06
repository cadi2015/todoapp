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

import android.os.Bundle;
import android.support.annotation.Nullable;
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
 * Displays an add or edit task screen.
 * 哈哈，编辑Task页
 */
public class AddEditTaskActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_TASK = 1; //请求码，呵呵，常量的干活

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"; //从Bundle里面恢复对象的时候，要用到的key

    private AddEditTaskPresenter mAddEditTaskPresenter; //Presenter的引用

    private ActionBar mActionBar; //View的引用，这里是ActionBar

    /**
     * 生命周期方法，老朋友了，牛逼
     * @param savedInstanceState 当Activity对象被意外销毁时，序列化的对象会存储在Bundle里，以再从Bundle里获取
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //必须先call基类的onCreate方法
        setContentView(R.layout.addtask_act); //设置布局

        // Set up the toolbar. 初始化Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame); //通过id找fragment,醉了，大写F都不让用，艹

        String taskId = getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID); //从TaskDetailFragment中传递过来的Intent中取Task id，key用的就是这个AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID

        setToolbarTitle(taskId); //设置toolBar的标题

        if (addEditTaskFragment == null) { //如果没有拿到fragment
            addEditTaskFragment = AddEditTaskFragment.newInstance(); //这边当然要创建一个fragment了

            if (getIntent().hasExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID)) { //如果Intent中包含ARGUMENT_EDIT_TASK_ID这个key
                Bundle bundle = new Bundle(); //new一个Bundle
                bundle.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId); //Bundle对象中放入taskId
                addEditTaskFragment.setArguments(bundle); //然后把这个Bundle放入到fragment里
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), //将fragment依附到activity中
                    addEditTaskFragment, R.id.contentFrame);  //需要传入fragmentManager、fragment、还有布局中的占位id
        }

        boolean shouldLoadDataFromRepo = true; //标志位，是否从仓库中读取数据

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) { //若Bundle不为null，那就是说，是从上次意外的Activity对象中恢复
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY); //根据key，从Bundle中取标志位，并赋值给shouldLoadDataFromRepo
        }

        // Create the presenter 创建presenter
        mAddEditTaskPresenter = new AddEditTaskPresenter(
                taskId, //传入taskId对象，其实是一个String对象
                Injection.provideTasksRepository(getApplicationContext()), //传入TasksRepository对象
                addEditTaskFragment, //传入fragment对象
                shouldLoadDataFromRepo); //传入一个标志位，是否从仓库中读取数据
    }

    /**
     * 设置ToolbarTitle的标题
     * @param taskId
     */
    private void setToolbarTitle(@Nullable String taskId) {
        if(taskId == null) { //先判断，如果taskId为null
            mActionBar.setTitle(R.string.add_task); //标题就写为New TO-DO
        } else {
            mActionBar.setTitle(R.string.edit_task); //如果taskId不是null，那就是Edit TO-DO
        }
    }

    /**
     * 如果Activity对象被意外回收，无论是进程被撸掉，还是Activity Task栈被撸掉，都会调用该方法
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddEditTaskPresenter.isDataMissing());//把想保留的对象放进Bundle中
        super.onSaveInstanceState(outState);
    }

    /**
     * 又看见这个方法了，鬼知道怎么回事
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * 单元测试用的
     * @return
     */
    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

}
