package com.rettiwer.pl.laris.ui.authentication;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mattprecious.swirl.SwirlView;
import com.rettiwer.pl.laris.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FingerprintAuthenticationFragment extends Fragment {
    SwirlView swirlView;

    public FingerprintAuthenticationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fingerprint_authentication, container, false);
        swirlView = v.findViewById(R.id.fingerprint_button);

        return v;
    }

}
