/*
 * Copyright (C) 2020 ZeoFlow
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

package com.zeoflow.material.elements.internal;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Utils class for {@link Context}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ContextUtils
{

  /**
   * Returns the {@link Activity} given a {@link Context} or null if there is no {@link Activity},
   * taking into account the potential hierarchy of {@link ContextWrapper ContextWrappers}.
   */
  @Nullable
  public static Activity getActivity(Context context)
  {
    while (context instanceof ContextWrapper)
    {
      if (context instanceof Activity)
      {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return null;
  }
}
