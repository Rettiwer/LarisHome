package com.rettiwer.pl.laris.ui.home;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rettiwer.pl.laris.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorItemViewHolder> {
    private List<String> colors;
    private String currentColor;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    @NonNull
    private OnItemClickListener onItemClickListener;

    public ColorPickerAdapter(List<String> rooms, @NonNull OnItemClickListener onItemClickListener) {
        this.colors = rooms;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ColorItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_picker_item, parent, false);
        ColorPickerAdapter.ColorItemViewHolder viewHolder = new ColorPickerAdapter.ColorItemViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorItemViewHolder holder, final int position) {
        Drawable wrapDrawable = DrawableCompat.wrap(holder.color.getBackground());
        DrawableCompat.setTint(wrapDrawable, Color.parseColor(colors.get(position)));
        holder.color.setBackground(wrapDrawable);


        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(
                        colors.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public class ColorItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView color;

        public ColorItemViewHolder(View itemView) {
            super(itemView);
            color = itemView.findViewById(R.id.color_item);
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }
}
