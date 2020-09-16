package com.rettiwer.pl.laris.ui.room.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.api.sensors.MotionSensor;
import com.rettiwer.pl.laris.data.remote.api.sensors.Sensor;
import com.rettiwer.pl.laris.data.remote.api.sensors.SwitchSensor;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SensorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Sensor> mSensors;

    public SensorListAdapter(List<Sensor> sensors) {
        this.mSensors = sensors;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.switch_sensor_item, parent, false);
                return new SwitchSensorViewHolder(view);
            case 2:
                return new MotionSensorViewHolder(parent);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Sensor sensor = mSensors.get(position);
        if (sensor instanceof SwitchSensor)
            return 1;
        else if (sensor instanceof MotionSensor)
            return 2;
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 1:
                SwitchSensorViewHolder switchSensorViewHolder = (SwitchSensorViewHolder) holder;
                switchSensorViewHolder.setSwitchSensor(((SwitchSensor) mSensors.get(position)));
                break;
            case 2:
                MotionSensorViewHolder motionSensorViewHolder = (MotionSensorViewHolder) holder;
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSensors.size();
    }
}
