/*
 * Copyright (C) 2021 ZeoFlow
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

package com.zeoflow.material.elements.bottomsheet;

import android.view.View;

import androidx.annotation.NonNull;

public class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback
{

    private final IOnBottomSheet listener;

    public BottomSheetCallback(IOnBottomSheet listener)
    {
        this.listener = listener;
    }
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState)
    {
        listener.onStateChanged(bottomSheet, newState);
    }
    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset)
    {
        listener.onSlide(bottomSheet, slideOffset);
    }

    public interface IOnBottomSheet
    {

        void onSlide(View view, float slideOffset);
        void onStateChanged(View view, int state);

    }

}
