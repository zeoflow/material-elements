

package com.zeoflow.material.elements.bottomnavigation;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({
  LabelVisibilityMode.LABEL_VISIBILITY_AUTO,
  LabelVisibilityMode.LABEL_VISIBILITY_SELECTED,
  LabelVisibilityMode.LABEL_VISIBILITY_LABELED,
  LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
})
@Retention(RetentionPolicy.SOURCE)
public @interface LabelVisibilityMode {
  
  int LABEL_VISIBILITY_AUTO = -1;

  
  int LABEL_VISIBILITY_SELECTED = 0;

  
  int LABEL_VISIBILITY_LABELED = 1;

  
  int LABEL_VISIBILITY_UNLABELED = 2;
}
