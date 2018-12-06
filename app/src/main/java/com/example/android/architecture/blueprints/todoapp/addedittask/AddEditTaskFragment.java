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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends Fragment implements AddEditTaskContract.View {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID"; //嘿嘿，放入Bundle时，用的key

    private AddEditTaskContract.Presenter mPresenter;  //嘿嘿，Presenter

    private TextView mTitle; //一个View的引用，标题

    private TextView mDescription; //一个View的引用，详细内容

    /**
     * 创建fragment对象的静态方法，倒是挺轻松的哈
     * @return
     */
    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment(); //new一个构造方法嘛
    }

    /**
     * 默认的构造方法，必须得有，没招
     */
    public AddEditTaskFragment() {
        // Required empty public constructor
    }

    /**
     * 生命周期方法
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start(); //在这里调用Presenter的start（）方法
        //有没有担心mPresenter没有初始化完成，这个完全不用担心，因为是在AddEditTaskActivity的onCreate
        //那就更没有必要担心，Activity的onResume（）方法没有调用完的话，fragment的onResume怎么会执行呢，在速度上，肯定presenter不会是null嘛
        //我这么理解比较风骚
    }

    /**
     * 嘿嘿，就是这个方法，在AddEditTaskPresenter的构造方法里调用，而AddEditTaskPresenter是在AddEditTaskActivity创建的哦
     * @param presenter
     */
    @Override
    public void setPresenter(@NonNull AddEditTaskContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    /**
     * fragment的生命周期方法，在activity的onCreate（）调用完后，会调用这个，它是在onCreateView方法后面调用的
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState); //老规矩，基类到还是必须调用的

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done); //从依附的Activity中获取View，这是一个FAB的控件
        fab.setImageResource(R.drawable.ic_done); //给fab设置图标
        fab.setOnClickListener(new View.OnClickListener() { //给fab设置点击事件监听器对象
            @Override
            public void onClick(View v) {
                mPresenter.saveTask(mTitle.getText().toString(), mDescription.getText().toString()); //调用mPresenter的saveTask方法，传入title和内容
            }
        });
    }

    /**
     * 生命周期方法
     * @param inflater LayoutInflater对象，用于解析布局xml文件
     * @param container ViewGroup对象，View的容器对象
     * @param savedInstanceState 一个Bundle对象
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false); //初始化root view
        mTitle = (TextView) root.findViewById(R.id.add_task_title); //获取view title
        mDescription = (TextView) root.findViewById(R.id.add_task_description); //获取view 详细description
        setHasOptionsMenu(true); //设置展示optionsMenu
        return root; //返回初始化后的View
    }

    /**
     *  展示一个空Task的Error提示，展示一个Snackbar
     */
    @Override
    public void showEmptyTaskError() {
        Snackbar.make(mTitle, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
    }

    /**
     *  展示TasksList，怎么展示呢，把当前的EditTaskActivity，finish掉
     */
    @Override
    public void showTasksList() {
        getActivity().setResult(Activity.RESULT_OK); //首先设置结果码
        getActivity().finish(); //finish掉整个Activity
    }

    /**
     * 设置标题
     * @param title
     */
    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    /**
     *  设置详细内容
     * @param description
     */
    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }

    /**
     * 判断一下fragment是否已经依附到Activity上
     * @return
     */
    @Override
    public boolean isActive() {
        return isAdded();
    }
}
