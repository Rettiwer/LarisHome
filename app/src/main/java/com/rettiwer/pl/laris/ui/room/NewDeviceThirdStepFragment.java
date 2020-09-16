package com.rettiwer.pl.laris.ui.room;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewDeviceThirdStepFragment extends Fragment {


    public NewDeviceThirdStepFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_device_third_step, container, false);
    }

}
