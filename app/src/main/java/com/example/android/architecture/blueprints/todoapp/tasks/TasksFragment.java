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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 * 这个Fragment是TaskActivity下真正的V，实现了最重要的interface View,即TasksContract.View，里面都是TasksFragment的本身、或者与其他组件通信后的逻辑交互（主要是View的变化）
 */
public class TasksFragment extends Fragment implements TasksContract.View { //任务列表，最最重要的Fragment

    private TasksContract.Presenter mPresenter; //V中也要拿到Presenter嘛，虽然TasksFragment作为V已经传递给P了

    private TasksAdapter mListAdapter; //适配器。。。，没办法TasksFragment就是个RecyclerView

    private View mNoTasksView; //没数据时展示的View

    private ImageView mNoTaskIcon; //没数据时展示的Icon

    private TextView mNoTaskMainView;

    private TextView mNoTaskAddView; //没有数据时，展示添加的View

    private LinearLayout mTasksView;

    private TextView mFilteringLabelView;

    public TasksFragment() { //必须有一个空的public构造方法，谁用Fragment谁知道。。。。
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() { //返回一个Fragment对象，尼玛。。。
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new TasksAdapter(new ArrayList<Task>(0), mItemListener); //初始化TasksAdapter
                                         //初始化的由数组组成的线性表，初始化容量为0，
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start(); //看来在Fragment建立完后，就会调用Presenter的start（）方法，这里面又调用了onRefresh，所以每次初始化的时候，你都看见执行下拉刷新一次
    }

    /**
     * 这个方法用于初始化在TasksFragment下的Presenter
     * @param presenter
     */
    @Override
    public void setPresenter(@NonNull TasksContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter); //只要presenter不为null，直接赋值给mPresenter
    }


    /**
     * startActivityForResult调用后，启动一个组件，会再调用该方法，有一个请求码、一个结果码，还有Intent，这里有存疑，用的太少了，这个方法
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tasks_frag, container, false);

        // Set up tasks view
        ListView listView = (ListView) root.findViewById(R.id.tasks_list);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        mTasksView = (LinearLayout) root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        mNoTasksView = root.findViewById(R.id.noTasks);
        mNoTaskIcon = (ImageView) root.findViewById(R.id.noTasksIcon);
        mNoTaskMainView = (TextView) root.findViewById(R.id.noTasksMain);
        mNoTaskAddView = (TextView) root.findViewById(R.id.noTasksAdd);
        mNoTaskAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTask();    //这是没有Task的时候，点击空白View的事件处理
            }
        });

        // Set up floating action button 就是首页那个红个的plus
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add); //在这里加了图标
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewTask(); //加了事件，同样,P中的addNewTasks（）方法最终也是调用Fragment的showAddTask（），即V的showAddTask
            }
        });

        // Set up progress indicator 这是下拉刷新的控件
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadTasks(false); //对下拉刷新的事件处理，可见是调用loadTasks（）方法，貌似不是强制刷新
            }
        });

        setHasOptionsMenu(true); //莫非这里是true，才显示右上角的OptionMenu？Fragment下还有这个逻辑哈？醉了

        return root; //返回inflate的布局
    }

    /**
     * 这里是Options点击事件的处理，尼玛，爽了
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear: //清除按钮的处理
                mPresenter.clearCompletedTasks(); //前去清空所有标记为Completed的Task
                break;
            case R.id.menu_filter: //过滤按钮的处理
                showFilteringPopUpMenu(); //因为还有一集菜单，所以这里用了这个
                break;
            case R.id.menu_refresh: //刷新按钮的处理
                mPresenter.loadTasks(true); //强制刷新展示的Task
                break;
        }
        return true;
    }

    /**
     * 尼玛，TasksActivity右上角的Item，就在这里创建的，原来只在Activity下有，没想到Fragment下也有
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    /**
     * 创建的二级菜单
     */
    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu()); //解析二级菜单

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) { //二级菜单的事件处理
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mPresenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mPresenter.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mPresenter.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on tasks in the ListView.
     * ListView每一个Item的点击事件，写的太牛逼了把，这个最后还是传递到Adapter中了
     * 牛逼
     */
    TaskItemListener mItemListener = new TaskItemListener() {
        @Override
        public void onTaskClick(Task clickedTask) {
            mPresenter.openTaskDetails(clickedTask); //打开到任务详情页
        }

        @Override
        public void onCompleteTaskClick(Task completedTask) {
            mPresenter.completeTask(completedTask); //标记为完成任务
        }

        @Override
        public void onActivateTaskClick(Task activatedTask) {
            mPresenter.activateTask(activatedTask); //标记为活动任务
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) { //标志位，代表是否活动

        if (getView() == null) { //要是Fragment下的rootView是null，直接return，中断方法
            return;
        }

        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout); //这不是下拉刷新嘛，要干啥

        // Make sure setRefreshing() is called after the layout is done with everything else. //在布局已经完成后，确保刷新被嗲用
        srl.post(new Runnable() { //靠，还用个post方法，发出一个Runnable，确保setRefreshing（）方法在布局完成后被调用过
            @Override
            public void run() {
                srl.setRefreshing(active); //醉了，还是待用了下拉刷新
            }
        });
    }

    @Override
    public void showTasks(List<Task> tasks) {
        mListAdapter.replaceData(tasks); //哈哈，把Tasks交给ListView的Adapter，替换数据，此时ListView会刷新

        mTasksView.setVisibility(View.VISIBLE); //我去要显示整个ListView了
        mNoTasksView.setVisibility(View.GONE);  //显示任务的时候，当然要gone掉没有任务的View了
    }

    /**
     * 没有标记为活动的任务时，调用该方法，里面肯定是处理View的哈，将View显示出来
     * 牛13，用同一个布局玩出了花样，没有任务时、没有活动任务时，没有完成任务时，
     */
    @Override
    public void showNoActiveTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp,
                false
        );
    }

    /**
     * 没有任务时，View的处理情况
     */
    @Override
    public void showNoTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                true //我竟然帮大神改了个bug，大神这里写的是false。。。
        );
    }

    /**
     * 没有已经完成任务时，View的处理情况
     */
    @Override
    public void showNoCompletedTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        );
    }

    /**
     * 保存任务成功时，调用的方法
     */
    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message));
    }


    /**
     * 这就是各种情况下，没有Task时，调用的方法
     * @param mainText  要显示的文案
     * @param iconRes   要显示的图标
     * @param showAddView 是否显示新增Task的View的标志位
     */
    private void showNoTasksViews(String mainText, int iconRes, boolean showAddView) {
        mTasksView.setVisibility(View.GONE);
        mNoTasksView.setVisibility(View.VISIBLE);

        mNoTaskMainView.setText(mainText);
        mNoTaskIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoTaskAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    /**
     * 展示仅有活动Task时的文案label
     */
    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    /**
     * 展示仅有已完成Task时的文案label
     */
    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    /**
     * 展示即有活动Task，又有已完成Task时的label
     */
    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    /**
     * 点击无任务时的View和红色的Plus按钮，均可跳转到新建Task的页面
     */
    @Override
    public void showAddTask() {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK); //果然使用了startActivityForResult方法，当然会回调onActivityResult了
    }


    /**
     * 点击已经存在的Task，跳抓到Task的详情页，大牛巧妙的使用show，确实点击后会展示TaskDetailsUi，牛比
     *
     * @param taskId  要Task的id，看来Task的id很重要
     */
    @Override
    public void showTaskDetailsUi(String taskId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(getContext(), TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId); //Intent把Task的id传过去了
        startActivity(intent);
    }

    /**
     * 展示一个Task被标记为Complete的提示，这里是个SnackBar
     */
    @Override
    public void showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

    /**
     * 展示一个Task标记为Active的提示，这里是个SnackBar
     */
    @Override
    public void showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    /**
     * 展示一个已完成Task被标记为Cleared的提示，这里是个SnackBar
     */
    @Override
    public void showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    /**
     *  展示一个错误的提示
     */
    @Override
    public void showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    /**
     *  嘿嘿，所有提示统一的处理，就是SnackBar
     * @param message  SnackBar中改变的 内容
     */
    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }


    /**
     * 判断是否是活跃状态？ 判断Fragment的View是否已经加载好？醉了哦
     * @return
     */
    @Override
    public boolean isActive() {
        return isAdded();
    }


    /**
     * private的静态内部类来了，是Adapter，ListView的Adapter，ListView在TasksFragment下，所以，Adapter内部类也放在这里
     */
    private static class TasksAdapter extends BaseAdapter {

        private List<Task> mTasks; //线性表，持有Task
        private TaskItemListener mItemListener; //每一条Task的监听器

        /**
         * 构造方法
         * @param tasks 线性表，里面每个元素为Task，
         * @param itemListener TaskItem 监听器
         */
        public TasksAdapter(List<Task> tasks, TaskItemListener itemListener) {
            setList(tasks);
            mItemListener = itemListener;
        }

        /**
         * 替换数据
         * @param tasks Task线性表，要新替换的数据
         */
        public void replaceData(List<Task> tasks) {
            setList(tasks); //设置为新替换的数据
            notifyDataSetChanged(); //通知数据已经发生改变，观察者模式，这时候RecyclerView要重绘了吧
        }

        /**
         * 设置数据
         * @param tasks 传入的线性表，持有Task
         */
        private void setList(List<Task> tasks) {
            mTasks = checkNotNull(tasks); //赋值给mTasks
        }

        /**
         *
         * @return 返回线性表数量，有多少个元素，即多少个Task
         */
        @Override
        public int getCount() {
            return mTasks.size();
        }

        /**
         * 还好封装好的，不然就是越界的命
         * @param i 元素下标
         * @return 返回指定索引的元素，即Task
         */
        @Override
        public Task getItem(int i) {
            return mTasks.get(i);
        }

        /**
         * 获取每一个Item的id
         * @param i 传入的Item下标
         * @return 返回的id
         */
        @Override
        public long getItemId(int i) {
            return i;
        }

        /**
         * 重要方法
         * @param i 显示是item的位置
         * @param view 嘿嘿，显示是每一个Item的View
         * @param viewGroup 是啥？还不知道，郁闷！
         * @return
         */
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view; //先做赋值
            if (rowView == null) { //如果rowView为null，开始解析一个View
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext()); //先拿到布局解析器对象，即LayoutInflater
                rowView = inflater.inflate(R.layout.task_item, viewGroup, false); //解析一条Item的View
            }

            final Task task = getItem(i); //嘿嘿，按位置，获得对应的Task

            TextView titleTV = (TextView) rowView.findViewById(R.id.title); //获取标题View
            titleTV.setText(task.getTitleForList()); //设置标题

            CheckBox completeCB = (CheckBox) rowView.findViewById(R.id.complete); //获取checkBox

            // Active/completed task UI
            completeCB.setChecked(task.isCompleted()); //初始化每一个Item的checkBox状态，根据Task的状态决定，真牛逼

            if (task.isCompleted()) { //如果Task已经完成
                rowView.setBackgroundDrawable(viewGroup.getContext() //改变item的背景色
                        .getResources().getDrawable(R.drawable.list_completed_touch_feedback)); //这牛逼
            } else { //如果Task仍是活动状态
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback)); //改变为活动时的Item颜色
            }

            completeCB.setOnClickListener(new View.OnClickListener() { //checkBox的监听器
                @Override
                public void onClick(View v) {
                    if (!task.isCompleted()) { //每当点击checkBox时，当Task是未完成时
                        mItemListener.onCompleteTaskClick(task); //调用onCompleteTaskClick方法
                    } else {
                        mItemListener.onActivateTaskClick(task); //每当点击checkBox时，当Task已经完成，调用onActivateTaskClick方法
                    }
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() { //每一个Item的点击监听器
                @Override
                public void onClick(View view) {
                    mItemListener.onTaskClick(task); //直接调用onTaskClick（）
                }
            });   //大神这牛逼，把item的所有事件，包括整条Item的交互、单个checkBox的交互，全部放到mItemListener里面，牛逼

            return rowView;
        }
    }

    /**
     * 这就是Item交互的interface，大神牛逼，牛逼，牛逼，三个牛逼
     */
    public interface TaskItemListener {

        void onTaskClick(Task clickedTask);

        void onCompleteTaskClick(Task completedTask);

        void onActivateTaskClick(Task activatedTask);
    }

}
