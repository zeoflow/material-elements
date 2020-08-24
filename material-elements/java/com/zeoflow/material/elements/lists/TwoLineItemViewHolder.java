

package com.zeoflow.material.elements.lists;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TwoLineItemViewHolder extends SingleLineItemViewHolder {

  public final TextView secondary;

  public TwoLineItemViewHolder(@NonNull View view) {
    super(view);
    this.secondary = itemView.findViewById(R.id.mtrl_list_item_secondary_text);
  }

  @NonNull
  public static TwoLineItemViewHolder create(@NonNull ViewGroup parent) {
    return new TwoLineItemViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.material_list_item_two_line, parent, false));
  }
}
