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

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zeoflow.app.Activity;
import com.zeoflow.material.elements.bottomsheet.BottomSheetBehavior;
import com.zeoflow.material.elements.bottomsheet.BottomSheetCallback;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.llHomer),
                (ignored, insets) -> {
                    windowInsets = insets;
                    return insets;
                });
        findViewById(R.id.llHomer).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .add(new BottomDialog(), "BottomDialog").commit();
        });

        View bottomSheetPersistent = findViewById(R.id.bottom_drawer);
        TextView bottomSheetText = findViewById(R.id.cat_persistent_bottomsheet_state);
        BottomSheetBehavior.from(bottomSheetPersistent)
                .addBottomSheetCallback(createBottomSheetCallback(bottomSheetText));

        setBottomSheetHeights(true);

    }

    private void setBottomSheetHeights(boolean fullScreen) {
        View bottomSheetChildView = findViewById(R.id.bottom_drawer);
        ViewGroup.LayoutParams params = bottomSheetChildView.getLayoutParams();
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetChildView);
        bottomSheetBehavior.setUpdateImportantForAccessibilityOnSiblings(fullScreen);
        boolean fitToContents = true;
        float halfExpandedRatio = 0.5f;
        int windowHeight = getWindowHeight();
        if (params != null) {
            if (fullScreen) {
                params.height = windowHeight;
                fitToContents = false;
                halfExpandedRatio = 0.7f;
            } else {
                params.height = getBottomSheetPersistentDefaultHeight();
            }
            bottomSheetChildView.setLayoutParams(params);
            bottomSheetBehavior.setFitToContents(fitToContents);
            bottomSheetBehavior.setHalfExpandedRatio(halfExpandedRatio);
        }
    }

    private int getBottomSheetPersistentDefaultHeight() {
        return getWindowHeight() * 3 / 5;
    }

    private WindowInsetsCompat windowInsets;

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // Allow Fullscreen BottomSheet to expand beyond system windows and draw under status bar.
        int height = displayMetrics.heightPixels;
        if (windowInsets != null) {
            height += windowInsets.getSystemWindowInsetTop();
            height += windowInsets.getSystemWindowInsetBottom();
        }
        return height;
    }

    private BottomSheetCallback createBottomSheetCallback(@NonNull TextView text) {
        // Set up BottomSheetCallback
        return new BottomSheetCallback(new BottomSheetCallback.IOnBottomSheet() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        text.setText("Dragging");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        text.setText("Expanded");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        text.setText("Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        BottomSheetBehavior<View> bottomSheetBehavior =
                                BottomSheetBehavior.from(bottomSheet);
                        text.setText("Half Expanded: " + bottomSheetBehavior.getHalfExpandedRatio());
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
