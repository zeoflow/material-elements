

package com.zeoflow.material.elements.transition;

import android.animation.Animator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;


public interface VisibilityAnimatorProvider {

  
  @Nullable
  Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view);

  
  @Nullable
  Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view);
}
