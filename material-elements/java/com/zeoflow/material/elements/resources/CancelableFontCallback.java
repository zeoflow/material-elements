

package com.zeoflow.material.elements.resources;

import android.graphics.Typeface;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.content.res.ResourcesCompat.FontCallback;


@RestrictTo(Scope.LIBRARY_GROUP)
public final class CancelableFontCallback extends TextAppearanceFontCallback {

  
  public interface ApplyFont {
    void apply(Typeface font);
  }

  private final Typeface fallbackFont;
  private final ApplyFont applyFont;
  private boolean cancelled;

  public CancelableFontCallback(ApplyFont applyFont, Typeface fallbackFont) {
    this.fallbackFont = fallbackFont;
    this.applyFont = applyFont;
  }

  @Override
  public void onFontRetrieved(Typeface font, boolean fontResolvedSynchronously) {
    updateIfNotCancelled(font);
  }

  @Override
  public void onFontRetrievalFailed(int reason) {
    updateIfNotCancelled(fallbackFont);
  }

  
  public void cancel() {
    cancelled = true;
  }

  private void updateIfNotCancelled(Typeface updatedFont) {
    if (!cancelled) {
      applyFont.apply(updatedFont);
    }
  }
}
