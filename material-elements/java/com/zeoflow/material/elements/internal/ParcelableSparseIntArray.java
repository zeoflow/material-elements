

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import android.util.SparseIntArray;


@RestrictTo(LIBRARY_GROUP)
public class ParcelableSparseIntArray extends SparseIntArray implements Parcelable {

  public ParcelableSparseIntArray() {
    super();
  }

  public ParcelableSparseIntArray(int initialCapacity) {
    super(initialCapacity);
  }

  public ParcelableSparseIntArray(@NonNull SparseIntArray sparseIntArray) {
    super();
    for (int i = 0; i < sparseIntArray.size(); i++) {
      put(sparseIntArray.keyAt(i), sparseIntArray.valueAt(i));
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    int[] keys = new int[size()];
    int[] values = new int[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeIntArray(values);
  }

  public static final Creator<ParcelableSparseIntArray> CREATOR =
      new Creator<ParcelableSparseIntArray>() {
        @NonNull
        @Override
        public ParcelableSparseIntArray createFromParcel(@NonNull Parcel source) {
          int size = source.readInt();
          ParcelableSparseIntArray read = new ParcelableSparseIntArray(size);

          int[] keys = new int[size];
          int[] values = new int[size];

          source.readIntArray(keys);
          source.readIntArray(values);

          for (int i = 0; i < size; i++) {
            read.put(keys[i], values[i]);
          }

          return read;
        }

        @NonNull
        @Override
        public ParcelableSparseIntArray[] newArray(int size) {
          return new ParcelableSparseIntArray[size];
        }
      };
}
