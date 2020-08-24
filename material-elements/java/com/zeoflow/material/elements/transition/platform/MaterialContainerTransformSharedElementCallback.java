

package com.zeoflow.material.elements.transition.platform;

import com.google.android.material.R;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import android.transition.Transition;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import com.zeoflow.material.elements.internal.ContextUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;


@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MaterialContainerTransformSharedElementCallback extends SharedElementCallback {

  @Nullable private static WeakReference<View> capturedSharedElement;

  private boolean entering = true;
  private boolean transparentWindowBackgroundEnabled = true;
  private boolean sharedElementReenterTransitionEnabled = false;
  @Nullable private Rect returnEndBounds;
  @Nullable private ShapeProvider shapeProvider = new ShapeableViewShapeProvider();

  
  public interface ShapeProvider {
    @Nullable
    ShapeAppearanceModel provideShape(@NonNull View sharedElement);
  }

  
  public static class ShapeableViewShapeProvider implements ShapeProvider {
    @Nullable
    @Override
    public ShapeAppearanceModel provideShape(@NonNull View sharedElement) {
      return sharedElement instanceof Shapeable
          ? ((Shapeable) sharedElement).getShapeAppearanceModel()
          : null;
    }
  }

  @Nullable
  @Override
  public Parcelable onCaptureSharedElementSnapshot(
      @NonNull View sharedElement,
      @NonNull Matrix viewToGlobalMatrix,
      @NonNull RectF screenBounds) {
    capturedSharedElement = new WeakReference<>(sharedElement);
    return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
  }

  @Nullable
  @Override
  public View onCreateSnapshotView(@NonNull Context context, @Nullable Parcelable snapshot) {
    View snapshotView = super.onCreateSnapshotView(context, snapshot);
    if (snapshotView != null && capturedSharedElement != null && shapeProvider != null) {
      View sharedElement = capturedSharedElement.get();
      if (sharedElement != null) {
        ShapeAppearanceModel shapeAppearanceModel = shapeProvider.provideShape(sharedElement);
        if (shapeAppearanceModel != null) {
          
          snapshotView.setTag(R.id.mtrl_motion_snapshot_view, shapeAppearanceModel);
        }
      }
    }
    return snapshotView;
  }

  @Override
  public void onMapSharedElements(
      @NonNull List<String> names, @NonNull Map<String, View> sharedElements) {
    if (!names.isEmpty() && !sharedElements.isEmpty()) {
      View sharedElement = sharedElements.get(names.get(0));
      if (sharedElement != null) {
        Activity activity = ContextUtils.getActivity(sharedElement.getContext());
        if (activity != null) {
          Window window = activity.getWindow();
          if (entering) {
            setUpEnterTransform(window);
          } else {
            setUpReturnTransform(activity, window);
          }
        }
      }
    }
  }

  @Override
  public void onSharedElementStart(
      @NonNull List<String> sharedElementNames,
      @NonNull List<View> sharedElements,
      @NonNull List<View> sharedElementSnapshots) {
    if (!sharedElements.isEmpty() && !sharedElementSnapshots.isEmpty()) {
      
      
      sharedElements.get(0).setTag(R.id.mtrl_motion_snapshot_view, sharedElementSnapshots.get(0));
    }

    if (!entering && !sharedElements.isEmpty() && returnEndBounds != null) {
      
      
      View sharedElement = sharedElements.get(0);
      int widthSpec = MeasureSpec.makeMeasureSpec(returnEndBounds.width(), MeasureSpec.EXACTLY);
      int heightSpec = MeasureSpec.makeMeasureSpec(returnEndBounds.height(), MeasureSpec.EXACTLY);
      sharedElement.measure(widthSpec, heightSpec);
      sharedElement.layout(
          returnEndBounds.left, returnEndBounds.top, returnEndBounds.right, returnEndBounds.bottom);
    }
  }

  @Override
  public void onSharedElementEnd(
      @NonNull List<String> sharedElementNames,
      @NonNull List<View> sharedElements,
      @NonNull List<View> sharedElementSnapshots) {
    if (!sharedElements.isEmpty()
        && sharedElements.get(0).getTag(R.id.mtrl_motion_snapshot_view) instanceof View) {
      
      sharedElements.get(0).setTag(R.id.mtrl_motion_snapshot_view, null);
    }

    if (!entering && !sharedElements.isEmpty()) {
      returnEndBounds = TransitionUtils.getRelativeBoundsRect(sharedElements.get(0));
    }

    entering = false;
  }

  
  @Nullable
  public ShapeProvider getShapeProvider() {
    return shapeProvider;
  }

  
  public void setShapeProvider(@Nullable ShapeProvider shapeProvider) {
    this.shapeProvider = shapeProvider;
  }

  
  public boolean isTransparentWindowBackgroundEnabled() {
    return transparentWindowBackgroundEnabled;
  }

  
  public void setTransparentWindowBackgroundEnabled(boolean transparentWindowBackgroundEnabled) {
    this.transparentWindowBackgroundEnabled = transparentWindowBackgroundEnabled;
  }

  
  public boolean isSharedElementReenterTransitionEnabled() {
    return sharedElementReenterTransitionEnabled;
  }

  
  public void setSharedElementReenterTransitionEnabled(
      boolean sharedElementReenterTransitionEnabled) {
    this.sharedElementReenterTransitionEnabled = sharedElementReenterTransitionEnabled;
  }

  private void setUpEnterTransform(final Window window) {
    Transition transition = window.getSharedElementEnterTransition();
    if (transition instanceof MaterialContainerTransform) {
      MaterialContainerTransform transform = (MaterialContainerTransform) transition;
      if (!sharedElementReenterTransitionEnabled) {
        window.setSharedElementReenterTransition(null);
      }
      if (transparentWindowBackgroundEnabled) {
        updateBackgroundFadeDuration(window, transform);
        transform.addListener(
            new TransitionListenerAdapter() {
              @Override
              public void onTransitionStart(Transition transition) {
                removeWindowBackground(window);
              }

              @Override
              public void onTransitionEnd(Transition transition) {
                restoreWindowBackground(window);
              }
            });
      }
    }
  }

  private void setUpReturnTransform(final Activity activity, final Window window) {
    Transition transition = window.getSharedElementReturnTransition();
    if (transition instanceof MaterialContainerTransform) {
      MaterialContainerTransform transform = (MaterialContainerTransform) transition;
      transform.setHoldAtEndEnabled(true);
      transform.addListener(
          new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
              
              if (capturedSharedElement != null) {
                View sharedElement = capturedSharedElement.get();
                if (sharedElement != null) {
                  sharedElement.setAlpha(1);
                  capturedSharedElement = null;
                }
              }

              
              activity.finish();
              activity.overridePendingTransition(0, 0);
            }
          });
      if (transparentWindowBackgroundEnabled) {
        updateBackgroundFadeDuration(window, transform);
        transform.addListener(
            new TransitionListenerAdapter() {
              @Override
              public void onTransitionStart(Transition transition) {
                removeWindowBackground(window);
              }
            });
      }
    }
  }

  
  private static void removeWindowBackground(Window window) {
    window
        .getDecorView()
        .getBackground()
        .mutate()
        .setColorFilter(
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                Color.TRANSPARENT, BlendModeCompat.CLEAR));
  }

  
  private static void restoreWindowBackground(Window window) {
    window.getDecorView().getBackground().mutate().clearColorFilter();
  }

  
  private static void updateBackgroundFadeDuration(
      Window window, MaterialContainerTransform transform) {
    window.setTransitionBackgroundFadeDuration(transform.getDuration());
  }
}
