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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the task detail screen.
 * Task详情页最主要的UI，就在这个类里面
 * 当然这个Fragment一定是要实现TaskDetailContract.View接口的，View的变化，全在里面
 * Let us do it
 */
public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    @NonNull
    private static final String ARGUMENT_TASK_ID = "TASK_ID"; //参数 TASK ID

    @NonNull
    private static final int REQUEST_EDIT_TASK = 1; //请求编辑TASK的，请求码为1

    private TaskDetailContract.Presenter mPresenter; //TaskDetailContract.Presenter的引用在此

    private TextView mDetailTitle; //View

    private TextView mDetailDescription; //View

    private CheckBox mDetailCompleteStatus; //View

    /**
     * 创建Fragment的方法
     * @param taskId 要传入Task的id哦
     * @return
     */
    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle arguments = new Bundle(); //创建Bundle对象
        arguments.putString(ARGUMENT_TASK_ID, taskId); //把Task的id放到Bundle里面， ARGUMENT_TASK_ID就是它的key哦
        TaskDetailFragment fragment = new TaskDetailFragment();//new一个Fragment对象
        fragment.setArguments(arguments); //给Fragment设置Bundle
        return fragment; //返回创建的Fragment对象
                         //至于为啥要它携带上Task id，后面就会知道了
    }

    /**
     * 生命周期方法，参考Fragment生命周期
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start(); //调用了start()方法，我们看下mPresenter在哪里初始化的
    }

    /**
     * 生命周期方法
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.taskdetail_frag, container, false); //初始化布局
        setHasOptionsMenu(true);  //设置显示Options菜单
        mDetailTitle = (TextView) root.findViewById(R.id.task_detail_title); //拿到View
        mDetailDescription = (TextView) root.findViewById(R.id.task_detail_description); //拿到View
        mDetailCompleteStatus = (CheckBox) root.findViewById(R.id.task_detail_complete);//拿到View

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);//拿到View

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.editTask();
            }
        });//哈哈，设置点击事件监听器对象

        return root; //返回View
    }

    /**
     * 这个方法是TaskDetailContact.View下的一个方法，我们实现了它，就是它来初始化Presenter嘛
     * 进去看看在哪里调用的该方法，牛逼
     * @param presenter 参数就是presenter嘛
     */
    @Override
    public void setPresenter(@NonNull TaskDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    /**
     * Options菜单下，item的点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { //获取item的id
            case R.id.menu_delete: //如果id是menu_delete
                mPresenter.deleteTask(); //调用Presenter的删除Task
                return true; //直接返回true
        }
        return false; //返回false
    }

    /**
     * 创建OptionMenu菜单时，会调用的方法
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
    }

    /**
     * 设置加载提示
     * @param active 活跃状态
     */
    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));
        }
    }

    /**
     * 隐藏详细描述，就是把View GONE掉嘛
     */
    @Override
    public void hideDescription() {
        mDetailDescription.setVisibility(View.GONE);
    }

    /**
     * 隐藏标题，就是tile的View GONE掉嘛
     */
    @Override
    public void hideTitle() {
        mDetailTitle.setVisibility(View.GONE);
    }

    /**
     *  展示详细描述， View首先View.VISIBLE，总得显示出来
     *              View然后设置Text，牛逼
     * @param description
     */
    @Override
    public void showDescription(@NonNull String description) {
        mDetailDescription.setVisibility(View.VISIBLE);
        mDetailDescription.setText(description);
    }

    /**
     *  展示完成状态
     * @param complete
     */
    @Override
    public void showCompletionStatus(final boolean complete) {
        Preconditions.checkNotNull(mDetailCompleteStatus);

        mDetailCompleteStatus.setChecked(complete); //先把checkBox设置为选中状态
        mDetailCompleteStatus.setOnCheckedChangeListener( //然后为checkBox设置监听器，当checkBox状态改变时的监听器
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mPresenter.completeTask(); //如果是选中状态，调用presenter，标记Task为完成状态
                        } else {
                            mPresenter.activateTask(); //如果是非选中的状态，调用presenter的activateTask（），标记Task为活跃状态
                        }
                    }
                });
    }

    /**
     *  很明显，这是要跳转到编辑Task页啊，用show也没错，毕竟最后展示的是他
     * @param taskId
     */
    @Override
    public void showEditTask(@NonNull String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    /**
     *  Task删除，把Activity finish掉就好了
     */
    @Override
    public void showTaskDeleted() {
        getActivity().finish();
    }

    /**
     *  展示Task标记为完成状态，其实这里是弹出一个Snackbar
     */
    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     *  展示Task标记为活动状态，其实这里就是弹出一个Snackbar
     */
    @Override
    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * 调用startActivityForResult后，关掉栈顶的Activity后，会回调这个方法
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        Intent对象
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) { //判断请求码为REQUEST_EDIT_TASK
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) { //判断结果码为上一个Activity正常关闭
                getActivity().finish(); //得到TaskDetailAcFragment的Activity后，关闭Activity
            }
        }
    }

    /**
     * 展示标题
     * @param title 传入要展示的标题
     */
    @Override
    public void showTitle(@NonNull String title) {
        mDetailTitle.setVisibility(View.VISIBLE); //设置View为VISBLE
        mDetailTitle.setText(title); //设置标题
    }


    /**
     * 展示丢失的Task
     */
    @Override
    public void showMissingTask() {
        mDetailTitle.setText(""); //设置标题为""
        mDetailDescription.setText(getString(R.string.no_data)); //设置内容为"No data"
    }

    /**
     * 判断fragment是否为活动状态吧？
     * @return
     */
    @Override
    public boolean isActive() {
        return isAdded();
    }

}
