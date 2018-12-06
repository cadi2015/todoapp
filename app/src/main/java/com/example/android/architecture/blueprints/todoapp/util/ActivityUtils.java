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

package com.example.android.architecture.blueprints.todoapp.util;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This provides methods to help Activities load their UI.
 * 这个提供的方法帮助Activity加载他们的ui，ui指的是fragment
 */
public class ActivityUtils {

    /**
     *  将fragment依附到Activity上
     * @param fragmentManager  一个fragmentManager对象
     * @param fragment 一个fragment对象
     * @param frameId 要加入的ViewId
     */
    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                              @NonNull Fragment fragment, int frameId) {
        checkNotNull(fragmentManager); //检查fragmentManager不为null
        checkNotNull(fragment); //检查fragment不为null
        FragmentTransaction transaction = fragmentManager.beginTransaction(); //获得fragmentTransaction对象
        transaction.add(frameId, fragment); //将fragment加入到指定的viewId上
        transaction.commit(); //提交选择
    }

}
