

package com.zeoflow.material.elements.stateful;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.customview.view.AbsSavedState;


public class ExtendableSavedState extends AbsSavedState {

  @NonNull public final SimpleArrayMap<String, Bundle> extendableStates;

  public ExtendableSavedState(Parcelable superState) {
    super(superState);
    extendableStates = new SimpleArrayMap<>();
  }

  private ExtendableSavedState(@NonNull Parcel in, ClassLoader loader) {
    super(in, loader);

    int size = in.readInt();

    String[] keys = new String[size];
    in.readStringArray(keys);

    Bundle[] states = new Bundle[size];
    in.readTypedArray(states, Bundle.CREATOR);

    extendableStates = new SimpleArrayMap<>(size);
    for (int i = 0; i < size; i++) {
      extendableStates.put(keys[i], states[i]);
    }
  }

  @Override
  public void writeToParcel(@NonNull Parcel out, int flags) {
    super.writeToParcel(out, flags);

    int size = extendableStates.size();
    out.writeInt(size);

    String[] keys = new String[size];
    Bundle[] states = new Bundle[size];

    for (int i = 0; i < size; i++) {
      keys[i] = extendableStates.keyAt(i);
      states[i] = extendableStates.valueAt(i);
    }

    out.writeStringArray(keys);
    out.writeTypedArray(states, 0);
  }

  @NonNull
  @Override
  public String toString() {
    return "ExtendableSavedState{"
        + Integer.toHexString(System.identityHashCode(this))
        + " states="
        + extendableStates
        + "}";
  }

  public static final Parcelable.Creator<ExtendableSavedState> CREATOR =
      new Parcelable.ClassLoaderCreator<ExtendableSavedState>() {

        @NonNull
        @Override
        public ExtendableSavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
          return new ExtendableSavedState(in, loader);
        }

        @Nullable
        @Override
        public ExtendableSavedState createFromParcel(@NonNull Parcel in) {
          return new ExtendableSavedState(in, null);
        }

        @NonNull
        @Override
        public ExtendableSavedState[] newArray(int size) {
          return new ExtendableSavedState[size];
        }
      };
}
