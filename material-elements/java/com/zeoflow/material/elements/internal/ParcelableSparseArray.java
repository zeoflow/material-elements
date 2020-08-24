

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.SparseArray;


@RestrictTo(LIBRARY_GROUP)
public class ParcelableSparseArray extends SparseArray<Parcelable> implements Parcelable {

  public ParcelableSparseArray() {
    super();
  }

  public ParcelableSparseArray(@NonNull Parcel source, @Nullable ClassLoader loader) {
    super();
    int size = source.readInt();
    int[] keys = new int[size];
    source.readIntArray(keys);
    Parcelable[] values = source.readParcelableArray(loader);
    for (int i = 0; i < size; ++i) {
      put(keys[i], values[i]);
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    int size = size();
    int[] keys = new int[size];
    Parcelable[] values = new Parcelable[size];
    for (int i = 0; i < size; ++i) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }
    parcel.writeInt(size);
    parcel.writeIntArray(keys);
    parcel.writeParcelableArray(values, flags);
  }

  public static final Creator<ParcelableSparseArray> CREATOR =
      new ClassLoaderCreator<ParcelableSparseArray>() {
        @NonNull
        @Override
        public ParcelableSparseArray createFromParcel(@NonNull Parcel source, ClassLoader loader) {
          return new ParcelableSparseArray(source, loader);
        }

        @Nullable
        @Override
        public ParcelableSparseArray createFromParcel(@NonNull Parcel source) {
          return new ParcelableSparseArray(source, null);
        }

        @NonNull
        @Override
        public ParcelableSparseArray[] newArray(int size) {
          return new ParcelableSparseArray[size];
        }
      };
}
