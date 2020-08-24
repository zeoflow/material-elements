

package com.zeoflow.material.elements.shape;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({CornerFamily.ROUNDED, CornerFamily.CUT})
@Retention(RetentionPolicy.SOURCE)
public @interface CornerFamily {
  
  int ROUNDED = 0;
  
  int CUT = 1;
}
