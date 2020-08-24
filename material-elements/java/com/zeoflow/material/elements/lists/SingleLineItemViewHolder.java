

package com.zeoflow.material.elements.lists;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class SingleLineItemViewHolder extends ViewHolder {

  public final ImageView icon;
  public final TextView text;

  public SingleLineItemViewHolder(@NonNull View view) {
    super(view);
    this.icon = itemView.findViewById(R.id.mtrl_list_item_icon);
    this.text = itemView.findViewById(R.id.mtrl_list_item_text);
  }

  @NonNull
  public static SingleLineItemViewHolder create(@NonNull ViewGroup parent) {
    return new SingleLineItemViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.material_list_item_single_line, parent, false));
  }
}
