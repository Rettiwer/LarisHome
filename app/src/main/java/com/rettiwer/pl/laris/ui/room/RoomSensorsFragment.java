package com.rettiwer.pl.laris.ui.room;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.api.sensors.Sensor;
import com.rettiwer.pl.laris.data.remote.api.sensors.SwitchSensor;
import com.rettiwer.pl.laris.ui.room.adapters.SensorListAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RoomSensorsFragment extends Fragment {
    @BindView(R.id.room_sensors_list)
    RecyclerView mSensorsRecyclerView;

    public RoomSensorsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Log.d("Room", RoomSensorsFragmentArgs.fromBundle(getArguments()).getRoomUuid());
        }

        List<Sensor> sensors = new ArrayList<>();
        Sensor sensor = new SwitchSensor("test", "test", "switch", "1");
        sensors.add(sensor);

        mSensorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSensorsRecyclerView.setAdapter(new SensorListAdapter(sensors));


    }
}
