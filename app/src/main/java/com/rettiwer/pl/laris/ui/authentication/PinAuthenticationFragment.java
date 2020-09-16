package com.rettiwer.pl.laris.ui.authentication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserAuthenticationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserAuthenticationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinAuthenticationFragment extends Fragment {


    public PinAuthenticationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pin_authentication, container, false);
    }

}
