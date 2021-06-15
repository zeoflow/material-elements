/*
 * Copyright (C) 2021 ZeoFlow SRL
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

package com.zeoflow.material.elements.sample;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.zeoflow.app.Activity;

public class MainActivity extends Activity {

    boolean light = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.llHomer).setOnClickListener(v -> {

//            int flags = getWindow().getDecorView().getSystemUiVisibility();
//            if (light) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
//                }
//            } else {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
//                }
//            }
//            getWindow().getDecorView().setSystemUiVisibility(flags);
//            light = !light;
//            changeStatusBar(light);
            getSupportFragmentManager().beginTransaction()
                    .add(new BottomDialog(), "BottomDialog").commit();
        });

    }
    public void changeStatusBar(boolean light) {
        int flags = MainActivity.this.getWindow().getDecorView().getSystemUiVisibility();
        if (light) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(flags);
    }
}
