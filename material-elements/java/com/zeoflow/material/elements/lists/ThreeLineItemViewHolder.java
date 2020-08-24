

package com.zeoflow.material.elements.lists;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ThreeLineItemViewHolder extends TwoLineItemViewHolder {

  public final TextView tertiary;

  public ThreeLineItemViewHolder(@NonNull View view) {
    super(view);
    this.tertiary = itemView.findViewById(R.id.mtrl_list_item_tertiary_text);
  }

  @NonNull
  public static ThreeLineItemViewHolder create(@NonNull ViewGroup parent) {
    return new ThreeLineItemViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.material_list_item_three_line, parent, false));
  }
}
