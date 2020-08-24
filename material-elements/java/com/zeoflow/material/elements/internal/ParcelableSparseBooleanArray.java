

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import android.util.SparseBooleanArray;


@RestrictTo(LIBRARY_GROUP)
public class ParcelableSparseBooleanArray extends SparseBooleanArray implements Parcelable {

  public ParcelableSparseBooleanArray() {
    super();
  }

  public ParcelableSparseBooleanArray(int initialCapacity) {
    super(initialCapacity);
  }

  public ParcelableSparseBooleanArray(@NonNull SparseBooleanArray sparseBooleanArray) {
    super(sparseBooleanArray.size());
    for (int i = 0; i < sparseBooleanArray.size(); i++) {
      put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(i));
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    int[] keys = new int[size()];
    boolean[] values = new boolean[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeBooleanArray(values);
  }

  public static final Parcelable.Creator<ParcelableSparseBooleanArray> CREATOR =
      new Parcelable.Creator<ParcelableSparseBooleanArray>() {
        @NonNull
        @Override
        public ParcelableSparseBooleanArray createFromParcel(@NonNull Parcel source) {
          int size = source.readInt();
          ParcelableSparseBooleanArray read = new ParcelableSparseBooleanArray(size);

          int[] keys = new int[size];
          boolean[] values = new boolean[size];

          source.readIntArray(keys);
          source.readBooleanArray(values);

          for (int i = 0; i < size; i++) {
            read.put(keys[i], values[i]);
          }

          return read;
        }

        @NonNull
        @Override
        public ParcelableSparseBooleanArray[] newArray(int size) {
          return new ParcelableSparseBooleanArray[size];
        }
      };
}
