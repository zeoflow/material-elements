

package com.zeoflow.material.elements.shape;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;


public class ShapeAppearanceModel {

  
  public static final class Builder {

    @NonNull
    private CornerTreatment topLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment topRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment bottomRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment bottomLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull private CornerSize topLeftCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize topRightCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize bottomRightCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize bottomLeftCornerSize = new AbsoluteCornerSize(0);

    @NonNull private EdgeTreatment topEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment rightEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment bottomEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment leftEdge = MaterialShapeUtils.createDefaultEdgeTreatment();

    public Builder() {}

    public Builder(@NonNull ShapeAppearanceModel other) {
      topLeftCorner = other.topLeftCorner;
      topRightCorner = other.topRightCorner;
      bottomRightCorner = other.bottomRightCorner;
      bottomLeftCorner = other.bottomLeftCorner;

      topLeftCornerSize = other.topLeftCornerSize;
      topRightCornerSize = other.topRightCornerSize;
      bottomRightCornerSize = other.bottomRightCornerSize;
      bottomLeftCornerSize = other.bottomLeftCornerSize;

      topEdge = other.topEdge;
      rightEdge = other.rightEdge;
      bottomEdge = other.bottomEdge;
      leftEdge = other.leftEdge;
    }

    
    @NonNull
    public Builder setAllCorners(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setAllCorners(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setAllCornerSizes(cornerSize);
    }

    
    @NonNull
    public Builder setAllCorners(@NonNull CornerTreatment cornerTreatment) {
      return setTopLeftCorner(cornerTreatment)
          .setTopRightCorner(cornerTreatment)
          .setBottomRightCorner(cornerTreatment)
          .setBottomLeftCorner(cornerTreatment);
    }

    
    @NonNull
    public Builder setAllCornerSizes(@NonNull CornerSize cornerSize) {
      return setTopLeftCornerSize(cornerSize)
          .setTopRightCornerSize(cornerSize)
          .setBottomRightCornerSize(cornerSize)
          .setBottomLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setAllCornerSizes(@Dimension float cornerSize) {
      return setTopLeftCornerSize(cornerSize)
          .setTopRightCornerSize(cornerSize)
          .setBottomRightCornerSize(cornerSize)
          .setBottomLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setTopLeftCornerSize(@Dimension float cornerSize) {
      topLeftCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    
    @NonNull
    public Builder setTopLeftCornerSize(@NonNull CornerSize cornerSize) {
      topLeftCornerSize = cornerSize;
      return this;
    }

    
    @NonNull
    public Builder setTopRightCornerSize(@Dimension float cornerSize) {
      topRightCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    
    @NonNull
    public Builder setTopRightCornerSize(@NonNull CornerSize cornerSize) {
      topRightCornerSize = cornerSize;
      return this;
    }

    
    @NonNull
    public Builder setBottomRightCornerSize(@Dimension float cornerSize) {
      bottomRightCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    
    @NonNull
    public Builder setBottomRightCornerSize(@NonNull CornerSize cornerSize) {
      bottomRightCornerSize = cornerSize;
      return this;
    }

    
    @NonNull
    public Builder setBottomLeftCornerSize(@Dimension float cornerSize) {
      bottomLeftCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    
    @NonNull
    public Builder setBottomLeftCornerSize(@NonNull CornerSize cornerSize) {
      bottomLeftCornerSize = cornerSize;
      return this;
    }

    
    @NonNull
    public Builder setTopLeftCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setTopLeftCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setTopLeftCorner(@NonNull CornerTreatment topLeftCorner) {
      this.topLeftCorner = topLeftCorner;
      
      float size = compatCornerTreatmentSize(topLeftCorner);
      if (size != -1) {
        setTopLeftCornerSize(size);
      }
      return this;
    }

    
    @NonNull
    public Builder setTopRightCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopRightCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setTopRightCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopRightCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setTopRightCorner(@NonNull CornerTreatment topRightCorner) {
      this.topRightCorner = topRightCorner;
      
      float size = compatCornerTreatmentSize(topRightCorner);
      if (size != -1) {
        setTopRightCornerSize(size);
      }
      return this;
    }

    
    @NonNull
    public Builder setBottomRightCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomRightCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setBottomRightCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomRightCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setBottomRightCorner(@NonNull CornerTreatment bottomRightCorner) {
      this.bottomRightCorner = bottomRightCorner;
      
      float size = compatCornerTreatmentSize(bottomRightCorner);
      if (size != -1) {
        setBottomRightCornerSize(size);
      }
      return this;
    }

    
    @NonNull
    public Builder setBottomLeftCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setBottomLeftCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomLeftCornerSize(cornerSize);
    }

    
    @NonNull
    public Builder setBottomLeftCorner(@NonNull CornerTreatment bottomLeftCorner) {
      this.bottomLeftCorner = bottomLeftCorner;
      
      float size = compatCornerTreatmentSize(bottomLeftCorner);
      if (size != -1) {
        setBottomLeftCornerSize(size);
      }
      return this;
    }

    
    @NonNull
    public Builder setAllEdges(@NonNull EdgeTreatment edgeTreatment) {
      return setLeftEdge(edgeTreatment)
          .setTopEdge(edgeTreatment)
          .setRightEdge(edgeTreatment)
          .setBottomEdge(edgeTreatment);
    }

    
    @NonNull
    public Builder setLeftEdge(@NonNull EdgeTreatment leftEdge) {
      this.leftEdge = leftEdge;
      return this;
    }

    
    @NonNull
    public Builder setTopEdge(@NonNull EdgeTreatment topEdge) {
      this.topEdge = topEdge;
      return this;
    }

    
    @NonNull
    public Builder setRightEdge(@NonNull EdgeTreatment rightEdge) {
      this.rightEdge = rightEdge;
      return this;
    }

    
    @NonNull
    public Builder setBottomEdge(@NonNull EdgeTreatment bottomEdge) {
      this.bottomEdge = bottomEdge;
      return this;
    }

    
    private static float compatCornerTreatmentSize(CornerTreatment treatment) {
      if (treatment instanceof RoundedCornerTreatment) {
        return ((RoundedCornerTreatment) treatment).radius;
      } else if (treatment instanceof CutCornerTreatment) {
        return ((CutCornerTreatment) treatment).size;
      }
      return -1;
    }

    
    @NonNull
    public ShapeAppearanceModel build() {
      return new ShapeAppearanceModel(this);
    }
  }

  @NonNull
  public static Builder builder() {
    return new ShapeAppearanceModel.Builder();
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    return builder(context, attrs, defStyleAttr, defStyleRes, 0);
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      int defaultCornerSize) {
    return builder(
        context, attrs, defStyleAttr, defStyleRes, new AbsoluteCornerSize(defaultCornerSize));
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @NonNull CornerSize defaultCornerSize) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialShape, defStyleAttr, defStyleRes);

    int shapeAppearanceResId = a.getResourceId(R.styleable.MaterialShape_shapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.MaterialShape_shapeAppearanceOverlay, 0);
    a.recycle();
    return builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId, defaultCornerSize);
  }

  @NonNull
  public static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId) {
    return builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId, 0);
  }

  @NonNull
  private static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId,
      int defaultCornerSize) {
    return builder(
        context,
        shapeAppearanceResId,
        shapeAppearanceOverlayResId,
        new AbsoluteCornerSize(defaultCornerSize));
  }

  @NonNull
  private static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId,
      @NonNull CornerSize defaultCornerSize) {
    
    if (shapeAppearanceOverlayResId != 0) {
      context = new ContextThemeWrapper(context, shapeAppearanceResId);
      shapeAppearanceResId = shapeAppearanceOverlayResId;
    }

    TypedArray a =
        context.obtainStyledAttributes(shapeAppearanceResId, R.styleable.ShapeAppearance);

    try {
      int cornerFamily = a.getInt(R.styleable.ShapeAppearance_cornerFamily, CornerFamily.ROUNDED);
      int cornerFamilyTopLeft =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopLeft, cornerFamily);
      int cornerFamilyTopRight =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopRight, cornerFamily);
      int cornerFamilyBottomRight =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomRight, cornerFamily);
      int cornerFamilyBottomLeft =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomLeft, cornerFamily);

      CornerSize cornerSize =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSize, defaultCornerSize);

      CornerSize cornerSizeTopLeft =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeTopLeft, cornerSize);
      CornerSize cornerSizeTopRight =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeTopRight, cornerSize);
      CornerSize cornerSizeBottomRight =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeBottomRight, cornerSize);
      CornerSize cornerSizeBottomLeft =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeBottomLeft, cornerSize);

      return new Builder()
          .setTopLeftCorner(cornerFamilyTopLeft, cornerSizeTopLeft)
          .setTopRightCorner(cornerFamilyTopRight, cornerSizeTopRight)
          .setBottomRightCorner(cornerFamilyBottomRight, cornerSizeBottomRight)
          .setBottomLeftCorner(cornerFamilyBottomLeft, cornerSizeBottomLeft);
    } finally {
      a.recycle();
    }
  }

  @NonNull
  private static CornerSize getCornerSize(
      TypedArray a, int index, @NonNull CornerSize defaultValue) {
    TypedValue value = a.peekValue(index);
    if (value == null) {
      return defaultValue;
    }

    if (value.type == TypedValue.TYPE_DIMENSION) {
      
      
      return new AbsoluteCornerSize(
          TypedValue.complexToDimensionPixelSize(value.data, a.getResources().getDisplayMetrics()));
    } else if (value.type == TypedValue.TYPE_FRACTION) {
      return new RelativeCornerSize(value.getFraction(1.0f, 1.0f));
    } else {
      return defaultValue;
    }
  }

  
  public static final CornerSize PILL = new RelativeCornerSize(0.5f);

  CornerTreatment topLeftCorner;
  CornerTreatment topRightCorner;
  CornerTreatment bottomRightCorner;
  CornerTreatment bottomLeftCorner;
  CornerSize topLeftCornerSize;
  CornerSize topRightCornerSize;
  CornerSize bottomRightCornerSize;
  CornerSize bottomLeftCornerSize;
  EdgeTreatment topEdge;
  EdgeTreatment rightEdge;
  EdgeTreatment bottomEdge;
  EdgeTreatment leftEdge;

  private ShapeAppearanceModel(@NonNull ShapeAppearanceModel.Builder builder) {
    topLeftCorner = builder.topLeftCorner;
    topRightCorner = builder.topRightCorner;
    bottomRightCorner = builder.bottomRightCorner;
    bottomLeftCorner = builder.bottomLeftCorner;

    topLeftCornerSize = builder.topLeftCornerSize;
    topRightCornerSize = builder.topRightCornerSize;
    bottomRightCornerSize = builder.bottomRightCornerSize;
    bottomLeftCornerSize = builder.bottomLeftCornerSize;

    topEdge = builder.topEdge;
    rightEdge = builder.rightEdge;
    bottomEdge = builder.bottomEdge;
    leftEdge = builder.leftEdge;
  }

  
  public ShapeAppearanceModel() {
    topLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    topRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    bottomRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    bottomLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    topLeftCornerSize = new AbsoluteCornerSize(0);
    topRightCornerSize = new AbsoluteCornerSize(0);
    bottomRightCornerSize = new AbsoluteCornerSize(0);
    bottomLeftCornerSize = new AbsoluteCornerSize(0);

    topEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    rightEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    bottomEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    leftEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
  }

  
  @NonNull
  public CornerTreatment getTopLeftCorner() {
    return topLeftCorner;
  }

  
  @NonNull
  public CornerTreatment getTopRightCorner() {
    return topRightCorner;
  }

  
  @NonNull
  public CornerTreatment getBottomRightCorner() {
    return bottomRightCorner;
  }

  
  @NonNull
  public CornerTreatment getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  
  @NonNull
  public CornerSize getTopLeftCornerSize() {
    return topLeftCornerSize;
  }

  
  @NonNull
  public CornerSize getTopRightCornerSize() {
    return topRightCornerSize;
  }

  
  @NonNull
  public CornerSize getBottomRightCornerSize() {
    return bottomRightCornerSize;
  }

  
  @NonNull
  public CornerSize getBottomLeftCornerSize() {
    return bottomLeftCornerSize;
  }

  
  @NonNull
  public EdgeTreatment getLeftEdge() {
    return leftEdge;
  }

  
  @NonNull
  public EdgeTreatment getTopEdge() {
    return topEdge;
  }

  
  @NonNull
  public EdgeTreatment getRightEdge() {
    return rightEdge;
  }

  
  @NonNull
  public EdgeTreatment getBottomEdge() {
    return bottomEdge;
  }

  
  @NonNull
  public Builder toBuilder() {
    return new Builder(this);
  }

  
  @NonNull
  public ShapeAppearanceModel withCornerSize(float cornerSize) {
    return toBuilder().setAllCornerSizes(cornerSize).build();
  }

  @NonNull
  public ShapeAppearanceModel withCornerSize(@NonNull CornerSize cornerSize) {
    return toBuilder().setAllCornerSizes(cornerSize).build();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public interface CornerSizeUnaryOperator {
    @NonNull
    CornerSize apply(@NonNull CornerSize cornerSize);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public ShapeAppearanceModel withTransformedCornerSizes(@NonNull CornerSizeUnaryOperator op) {
    return toBuilder()
        .setTopLeftCornerSize(op.apply(getTopLeftCornerSize()))
        .setTopRightCornerSize(op.apply(getTopRightCornerSize()))
        .setBottomLeftCornerSize(op.apply(getBottomLeftCornerSize()))
        .setBottomRightCornerSize(op.apply(getBottomRightCornerSize()))
        .build();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public boolean isRoundRect(@NonNull RectF bounds) {
    boolean hasDefaultEdges =
        leftEdge.getClass().equals(EdgeTreatment.class)
            && rightEdge.getClass().equals(EdgeTreatment.class)
            && topEdge.getClass().equals(EdgeTreatment.class)
            && bottomEdge.getClass().equals(EdgeTreatment.class);

    float cornerSize = topLeftCornerSize.getCornerSize(bounds);

    boolean cornersHaveSameSize =
        topRightCornerSize.getCornerSize(bounds) == cornerSize
            && bottomLeftCornerSize.getCornerSize(bounds) == cornerSize
            && bottomRightCornerSize.getCornerSize(bounds) == cornerSize;

    boolean hasRoundedCorners =
        topRightCorner instanceof RoundedCornerTreatment
            && topLeftCorner instanceof RoundedCornerTreatment
            && bottomRightCorner instanceof RoundedCornerTreatment
            && bottomLeftCorner instanceof RoundedCornerTreatment;

    return hasDefaultEdges && cornersHaveSameSize && hasRoundedCorners;
  }
}
