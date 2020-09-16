package com.rettiwer.pl.laris.ui.room.adapters;

import android.view.View;

import com.rettiwer.pl.laris.data.remote.api.sensors.MotionSensor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MotionSensorViewHolder extends RecyclerView.ViewHolder {
    private MotionSensor mMotionSensor;

    public MotionSensorViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
