

package com.zeoflow.material.elements.badge;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.annotation.XmlRes;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.zeoflow.material.elements.drawable.DrawableUtils;
import com.zeoflow.material.elements.internal.TextDrawableHelper;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.resources.TextAppearance;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.bottomnavigation.BottomNavigationView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;


public class BadgeDrawable extends Drawable implements TextDrawableHelper.TextDrawableDelegate
{

  
  @IntDef({
    TOP_END,
    TOP_START,
    BOTTOM_END,
    BOTTOM_START,
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface BadgeGravity {}

  
  public static final int TOP_END = Gravity.TOP | Gravity.END;

  
  public static final int TOP_START = Gravity.TOP | Gravity.START;

  
  public static final int BOTTOM_END = Gravity.BOTTOM | Gravity.END;

  
  public static final int BOTTOM_START = Gravity.BOTTOM | Gravity.START;

  
  private static final int DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4;

  
  private static final int BADGE_NUMBER_NONE = -1;

  
  private static final int MAX_CIRCULAR_BADGE_NUMBER_COUNT = 9;

  @StyleRes private static final int DEFAULT_STYLE = R.style.Widget_MaterialComponents_Badge;
  @AttrRes private static final int DEFAULT_THEME_ATTR = R.attr.badgeStyle;

  
  static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";

  @NonNull private final WeakReference<Context> contextRef;
  @NonNull private final MaterialShapeDrawable shapeDrawable;
  @NonNull private final TextDrawableHelper textDrawableHelper;
  @NonNull private final Rect badgeBounds;
  private final float badgeRadius;
  private final float badgeWithTextRadius;
  private final float badgeWidePadding;
  @NonNull private final SavedState savedState;

  private float badgeCenterX;
  private float badgeCenterY;
  private int maxBadgeNumber;
  private float cornerRadius;
  private float halfBadgeWidth;
  private float halfBadgeHeight;

  
  @Nullable private WeakReference<View> anchorViewRef;
  @Nullable private WeakReference<ViewGroup> customBadgeParentRef;

  
  @RestrictTo(LIBRARY_GROUP)
  public static final class SavedState implements Parcelable {

    @ColorInt private int backgroundColor;
    @ColorInt private int badgeTextColor;
    private int alpha = 255;
    private int number = BADGE_NUMBER_NONE;
    private int maxCharacterCount;
    @Nullable private CharSequence contentDescriptionNumberless;
    @PluralsRes private int contentDescriptionQuantityStrings;
    @StringRes private int contentDescriptionExceedsMaxBadgeNumberRes;
    @BadgeGravity private int badgeGravity;

    @Dimension(unit = Dimension.PX)
    private int horizontalOffset;

    @Dimension(unit = Dimension.PX)
    private int verticalOffset;

    public SavedState(@NonNull Context context) {
      
      
      TextAppearance textAppearance =
          new TextAppearance(context, R.style.TextAppearance_MaterialComponents_Badge);
      badgeTextColor = textAppearance.textColor.getDefaultColor();
      contentDescriptionNumberless =
          context.getString(R.string.mtrl_badge_numberless_content_description);
      contentDescriptionQuantityStrings = R.plurals.mtrl_badge_content_description;
      contentDescriptionExceedsMaxBadgeNumberRes =
          R.string.mtrl_exceed_max_badge_number_content_description;
    }

    protected SavedState(@NonNull Parcel in) {
      backgroundColor = in.readInt();
      badgeTextColor = in.readInt();
      alpha = in.readInt();
      number = in.readInt();
      maxCharacterCount = in.readInt();
      contentDescriptionNumberless = in.readString();
      contentDescriptionQuantityStrings = in.readInt();
      badgeGravity = in.readInt();
      horizontalOffset = in.readInt();
      verticalOffset = in.readInt();
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      dest.writeInt(backgroundColor);
      dest.writeInt(badgeTextColor);
      dest.writeInt(alpha);
      dest.writeInt(number);
      dest.writeInt(maxCharacterCount);
      dest.writeString(contentDescriptionNumberless.toString());
      dest.writeInt(contentDescriptionQuantityStrings);
      dest.writeInt(badgeGravity);
      dest.writeInt(horizontalOffset);
      dest.writeInt(verticalOffset);
    }
  }

  @NonNull
  public SavedState getSavedState() {
    return savedState;
  }

  
  @NonNull
  static BadgeDrawable createFromSavedState(
      @NonNull Context context, @NonNull SavedState savedState) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.restoreFromSavedState(savedState);
    return badge;
  }

  
  @NonNull
  public static BadgeDrawable create(@NonNull Context context) {
    return createFromAttributes(context,  null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  
  @NonNull
  public static BadgeDrawable createFromResource(@NonNull Context context, @XmlRes int id) {
    AttributeSet attrs = DrawableUtils.parseDrawableXml(context, id, "badge");
    @StyleRes int style = attrs.getStyleAttribute();
    if (style == 0) {
      style = DEFAULT_STYLE;
    }
    return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, style);
  }

  
  @NonNull
  private static BadgeDrawable createFromAttributes(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.loadDefaultStateFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    return badge;
  }

  
  public void setVisible(boolean visible) {
    setVisible(visible,  false);
  }

  private void restoreFromSavedState(@NonNull SavedState savedState) {
    setMaxCharacterCount(savedState.maxCharacterCount);

    
    
    
    if (savedState.number != BADGE_NUMBER_NONE) {
      setNumber(savedState.number);
    }

    setBackgroundColor(savedState.backgroundColor);

    
    
    setBadgeTextColor(savedState.badgeTextColor);

    setBadgeGravity(savedState.badgeGravity);

    setHorizontalOffset(savedState.horizontalOffset);
    setVerticalOffset(savedState.verticalOffset);
  }

  private void loadDefaultStateFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Badge, defStyleAttr, defStyleRes);

    setMaxCharacterCount(
        a.getInt(R.styleable.Badge_maxCharacterCount, DEFAULT_MAX_BADGE_CHARACTER_COUNT));

    
    
    
    if (a.hasValue(R.styleable.Badge_number)) {
      setNumber(a.getInt(R.styleable.Badge_number, 0));
    }

    setBackgroundColor(readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor));

    
    
    if (a.hasValue(R.styleable.Badge_badgeTextColor)) {
      setBadgeTextColor(readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor));
    }

    setBadgeGravity(a.getInt(R.styleable.Badge_badgeGravity, TOP_END));

    setHorizontalOffset(a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0));
    setVerticalOffset(a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0));

    a.recycle();
  }

  private static int readColorFromAttributes(
      Context context, @NonNull TypedArray a, @StyleableRes int index) {
    return MaterialResources.getColorStateList(context, a, index).getDefaultColor();
  }

  private BadgeDrawable(@NonNull Context context) {
    this.contextRef = new WeakReference<>(context);
    ThemeEnforcement.checkMaterialTheme(context);
    Resources res = context.getResources();
    badgeBounds = new Rect();
    shapeDrawable = new MaterialShapeDrawable();

    badgeRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_radius);
    badgeWidePadding = res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding);
    badgeWithTextRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius);

    textDrawableHelper = new TextDrawableHelper( this);
    textDrawableHelper.getTextPaint().setTextAlign(Paint.Align.CENTER);
    this.savedState = new SavedState(context);
    setTextAppearanceResource(R.style.TextAppearance_MaterialComponents_Badge);
  }

  
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable ViewGroup customBadgeParent) {
    this.anchorViewRef = new WeakReference<>(anchorView);
    this.customBadgeParentRef = new WeakReference<>(customBadgeParent);
    updateCenterAndBounds();
    invalidateSelf();
  }

  
  @ColorInt
  public int getBackgroundColor() {
    return shapeDrawable.getFillColor().getDefaultColor();
  }

  
  public void setBackgroundColor(@ColorInt int backgroundColor) {
    savedState.backgroundColor = backgroundColor;
    ColorStateList backgroundColorStateList = ColorStateList.valueOf(backgroundColor);
    if (shapeDrawable.getFillColor() != backgroundColorStateList) {
      shapeDrawable.setFillColor(backgroundColorStateList);
      invalidateSelf();
    }
  }

  
  @ColorInt
  public int getBadgeTextColor() {
    return textDrawableHelper.getTextPaint().getColor();
  }

  
  public void setBadgeTextColor(@ColorInt int badgeTextColor) {
    savedState.badgeTextColor = badgeTextColor;
    if (textDrawableHelper.getTextPaint().getColor() != badgeTextColor) {
      textDrawableHelper.getTextPaint().setColor(badgeTextColor);
      invalidateSelf();
    }
  }

  
  public boolean hasNumber() {
    return savedState.number != BADGE_NUMBER_NONE;
  }

  
  public int getNumber() {
    if (!hasNumber()) {
      return 0;
    }
    return savedState.number;
  }

  
  public void setNumber(int number) {
    number = Math.max(0, number);
    if (this.savedState.number != number) {
      this.savedState.number = number;
      textDrawableHelper.setTextWidthDirty(true);
      updateCenterAndBounds();
      invalidateSelf();
    }
  }

  
  public void clearNumber() {
    savedState.number = BADGE_NUMBER_NONE;
    invalidateSelf();
  }

  
  public int getMaxCharacterCount() {
    return savedState.maxCharacterCount;
  }

  
  public void setMaxCharacterCount(int maxCharacterCount) {
    if (this.savedState.maxCharacterCount != maxCharacterCount) {
      this.savedState.maxCharacterCount = maxCharacterCount;
      updateMaxBadgeNumber();
      textDrawableHelper.setTextWidthDirty(true);
      updateCenterAndBounds();
      invalidateSelf();
    }
  }

  @BadgeGravity
  public int getBadgeGravity() {
    return savedState.badgeGravity;
  }

  
  public void setBadgeGravity(@BadgeGravity int gravity) {
    if (savedState.badgeGravity != gravity) {
      savedState.badgeGravity = gravity;
      if (anchorViewRef != null && anchorViewRef.get() != null) {
        updateBadgeCoordinates(
            anchorViewRef.get(), customBadgeParentRef != null ? customBadgeParentRef.get() : null);
      }
    }
  }

  @Override
  public boolean isStateful() {
    return false;
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    
  }

  @Override
  public int getAlpha() {
    return savedState.alpha;
  }

  @Override
  public void setAlpha(int alpha) {
    this.savedState.alpha = alpha;
    textDrawableHelper.getTextPaint().setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  
  @Override
  public int getIntrinsicHeight() {
    return badgeBounds.height();
  }

  
  @Override
  public int getIntrinsicWidth() {
    return badgeBounds.width();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0 || !isVisible()) {
      return;
    }
    shapeDrawable.draw(canvas);
    if (hasNumber()) {
      drawText(canvas);
    }
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void onTextSizeChange() {
    invalidateSelf();
  }

  @Override
  public boolean onStateChange(int[] state) {
    return super.onStateChange(state);
  }

  public void setContentDescriptionNumberless(CharSequence charSequence) {
    savedState.contentDescriptionNumberless = charSequence;
  }

  public void setContentDescriptionQuantityStringsResource(@StringRes int stringsResource) {
    savedState.contentDescriptionQuantityStrings = stringsResource;
  }

  public void setContentDescriptionExceedsMaxBadgeNumberStringResource(
      @StringRes int stringsResource) {
    savedState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
  }

  @Nullable
  public CharSequence getContentDescription() {
    if (!isVisible()) {
      return null;
    }
    if (hasNumber()) {
      if (savedState.contentDescriptionQuantityStrings > 0) {
        Context context = contextRef.get();
        if (context == null) {
          return null;
        }
        if (getNumber() <= maxBadgeNumber) {
          return context
              .getResources()
              .getQuantityString(
                  savedState.contentDescriptionQuantityStrings, getNumber(), getNumber());
        } else {
          return context.getString(
              savedState.contentDescriptionExceedsMaxBadgeNumberRes, maxBadgeNumber);
        }
      } else {
        return null;
      }
    } else {
      return savedState.contentDescriptionNumberless;
    }
  }

  
  public void setHorizontalOffset(int px) {
    savedState.horizontalOffset = px;
    updateCenterAndBounds();
  }

  
  public int getHorizontalOffset() {
    return savedState.horizontalOffset;
  }

  
  public void setVerticalOffset(int px) {
    savedState.verticalOffset = px;
    updateCenterAndBounds();
  }

  
  public int getVerticalOffset() {
    return savedState.verticalOffset;
  }

  private void setTextAppearanceResource(@StyleRes int id) {
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    setTextAppearance(new TextAppearance(context, id));
  }

  private void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (textDrawableHelper.getTextAppearance() == textAppearance) {
      return;
    }
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    textDrawableHelper.setTextAppearance(textAppearance, context);
    updateCenterAndBounds();
  }

  private void updateCenterAndBounds() {
    Context context = contextRef.get();
    View anchorView = anchorViewRef != null ? anchorViewRef.get() : null;
    if (context == null || anchorView == null) {
      return;
    }
    Rect tmpRect = new Rect();
    tmpRect.set(badgeBounds);

    Rect anchorRect = new Rect();
    
    anchorView.getDrawingRect(anchorRect);

    ViewGroup customBadgeParent = customBadgeParentRef != null ? customBadgeParentRef.get() : null;
    if (customBadgeParent != null || BadgeUtils.USE_COMPAT_PARENT) {
      
      ViewGroup viewGroup =
          customBadgeParent == null ? (ViewGroup) anchorView.getParent() : customBadgeParent;
      viewGroup.offsetDescendantRectToMyCoords(anchorView, anchorRect);
    }

    calculateCenterAndBounds(context, anchorRect, anchorView);

    BadgeUtils.updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, halfBadgeHeight);

    shapeDrawable.setCornerSize(cornerRadius);
    if (!tmpRect.equals(badgeBounds)) {
      shapeDrawable.setBounds(badgeBounds);
    }
  }

  private void calculateCenterAndBounds(
      @NonNull Context context, @NonNull Rect anchorRect, @NonNull View anchorView) {
    switch (savedState.badgeGravity) {
      case BOTTOM_END:
      case BOTTOM_START:
        badgeCenterY = anchorRect.bottom - savedState.verticalOffset;
        break;
      case TOP_END:
      case TOP_START:
      default:
        badgeCenterY = anchorRect.top + savedState.verticalOffset;
        break;
    }

    if (getNumber() <= MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
      cornerRadius = !hasNumber() ? badgeRadius : badgeWithTextRadius;
      halfBadgeHeight = cornerRadius;
      halfBadgeWidth = cornerRadius;
    } else {
      cornerRadius = badgeWithTextRadius;
      halfBadgeHeight = cornerRadius;
      String badgeText = getBadgeText();
      halfBadgeWidth = textDrawableHelper.getTextWidth(badgeText) / 2f + badgeWidePadding;
    }

    int inset =
        context
            .getResources()
            .getDimensionPixelSize(
                hasNumber()
                    ? R.dimen.mtrl_badge_text_horizontal_edge_offset
                    : R.dimen.mtrl_badge_horizontal_edge_offset);
    
    switch (savedState.badgeGravity) {
      case BOTTOM_START:
      case TOP_START:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset
                : anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset;
        break;
      case BOTTOM_END:
      case TOP_END:
      default:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset
                : anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset;
        break;
    }
  }

  private void drawText(Canvas canvas) {
    Rect textBounds = new Rect();
    String badgeText = getBadgeText();
    textDrawableHelper.getTextPaint().getTextBounds(badgeText, 0, badgeText.length(), textBounds);
    canvas.drawText(
        badgeText,
        badgeCenterX,
        badgeCenterY + textBounds.height() / 2,
        textDrawableHelper.getTextPaint());
  }

  @NonNull
  private String getBadgeText() {
    
    if (getNumber() <= maxBadgeNumber) {
      return Integer.toString(getNumber());
    } else {
      Context context = contextRef.get();
      if (context == null) {
        return "";
      }

      return context.getString(
          R.string.mtrl_exceed_max_badge_number_suffix,
          maxBadgeNumber,
          DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX);
    }
  }

  private void updateMaxBadgeNumber() {
    maxBadgeNumber = (int) Math.pow(10.0d, (double) getMaxCharacterCount() - 1) - 1;
  }
}
