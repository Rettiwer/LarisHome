package com.rettiwer.pl.laris.ui.authentication;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;

public class NewFingerprintCreatorFragment extends Fragment {


    public NewFingerprintCreatorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_fingerprint_creator, container, false);
    }

}
