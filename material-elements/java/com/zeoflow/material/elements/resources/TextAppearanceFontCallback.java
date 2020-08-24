

package com.zeoflow.material.elements.resources;

import android.graphics.Typeface;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.content.res.ResourcesCompat.FontCallback;


@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class TextAppearanceFontCallback {
  
  public abstract void onFontRetrieved(Typeface typeface, boolean fontResolvedSynchronously);

  
  public abstract void onFontRetrievalFailed(int reason);
}
