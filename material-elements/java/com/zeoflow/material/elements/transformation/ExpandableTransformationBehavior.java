

package com.zeoflow.material.elements.transformation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.zeoflow.material.elements.expandable.ExpandableWidget;
import com.zeoflow.material.elements.transition.MaterialContainerTransform;


@Deprecated
public abstract class ExpandableTransformationBehavior extends ExpandableBehavior {

  @Nullable private AnimatorSet currentAnimation;

  public ExpandableTransformationBehavior() {}

  public ExpandableTransformationBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  
  @NonNull
  protected abstract AnimatorSet onCreateExpandedStateChangeAnimation(
      View dependency, View child, boolean expanded, boolean isAnimating);

  @CallSuper
  @Override
  protected boolean onExpandedStateChange(
      View dependency, View child, boolean expanded, boolean animated) {
    boolean currentlyAnimating = currentAnimation != null;
    if (currentlyAnimating) {
      currentAnimation.cancel();
    }

    currentAnimation =
        onCreateExpandedStateChangeAnimation(dependency, child, expanded, currentlyAnimating);
    currentAnimation.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            currentAnimation = null;
          }
        });

    currentAnimation.start();
    if (!animated) {
      
      
      currentAnimation.end();
    }

    return true;
  }
}
