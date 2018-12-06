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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the statistics screen.
 * 作者牛逼啊，就一个页面，也一样要用fragment
 */
public class StatisticsFragment extends Fragment implements StatisticsContract.View {

    private TextView mStatisticsTV;

    private StatisticsContract.Presenter mPresenter;

    /**
     * 静态方法
     * @return 返回一个fragment对象嘛
     */
    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    /**
     * 从StatisticsContract.View上实现的方法
     * @param presenter
     */
    @Override
    public void setPresenter(@NonNull StatisticsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
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
        View root = inflater.inflate(R.layout.statistics_frag, container, false); //初始化View
        mStatisticsTV = (TextView) root.findViewById(R.id.statistics); //嘿嘿，拿到一个TextView
        return root;
    }

    /**
     * 生命周期方法
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start(); //fragment初始化时，直接调用presenter的start（）
    }


    /**
     * 设置是否展示进度状态
     * @param active
     */
    @Override
    public void setProgressIndicator(boolean active) {
        if (active) { //有活动行为，展示Loading字符串状态
            mStatisticsTV.setText(getString(R.string.loading)); //就是在TextView展示一个Loading
        } else {
            mStatisticsTV.setText("");  //如果active为false
        }
    }

    /**
     *  展示计数的方法
     * @param numberOfIncompleteTasks
     * @param numberOfCompletedTasks
     */
    @Override
    public void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        if (numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) { //如果活动Task和完成Task都是0的话
            mStatisticsTV.setText(getResources().getString(R.string.statistics_no_tasks)); //看来是用了一个View啊
        } else {
            String displayString = getResources().getString(R.string.statistics_active_tasks) + " "
                    + numberOfIncompleteTasks + "\n" + getResources().getString(
                    R.string.statistics_completed_tasks) + " " + numberOfCompletedTasks; //拼接字符串
            mStatisticsTV.setText(displayString);  //显示内容
        }
    }

    /**
     * 加载内容时，设置错误内容
     */
    @Override
    public void showLoadingStatisticsError() {
        mStatisticsTV.setText(getResources().getString(R.string.statistics_error));
    }

    /**
     * 判断fragment是否已经依附到Activity上
     * @return 是否依附到activity上的状态
     */
    @Override
    public boolean isActive() {
        return isAdded();
    }
}
