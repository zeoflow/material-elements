
package com.zeoflow.material.elements.animation;

import android.view.View;

import com.zeoflow.material.elements.bottomappbar.BottomAppBar;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButton;


public interface TransformationCallback<T extends View> {
  
  void onTranslationChanged(T view);

  
  void onScaleChanged(T view);
}
