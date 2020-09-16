package com.rettiwer.pl.laris.ui.room.adapters;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.api.sensors.SwitchSensor;
import com.rettiwer.pl.laris.ui.widget.SwitchButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SwitchSensorViewHolder extends RecyclerView.ViewHolder {
    private SwitchSensor mSwitchSensor;

    @BindView(R.id.power_switch)
    Switch mSwitchButton;

    @BindView(R.id.sensor_name)
    TextView mSensorName;

    public SwitchSensorViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setSwitchSensor(SwitchSensor switchSensor) {
        this.mSwitchSensor = switchSensor;
        mSensorName.setText(switchSensor.getName());
    }
}
