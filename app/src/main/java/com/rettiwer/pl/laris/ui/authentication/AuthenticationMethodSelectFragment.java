package com.rettiwer.pl.laris.ui.authentication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rettiwer.pl.laris.R;

public class AuthenticationMethodSelectFragment extends Fragment {

    public AuthenticationMethodSelectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authentication_method_select, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.fingerprint_method_button)
    public void fingerprintMethod() {
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.authentication_graph_host);
        navHostFragment.getNavController().navigate(R.id.newFingerprintCreatorFragment);
    }

    @OnClick(R.id.pin_method_button)
    public void pinMethod() {
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.authentication_graph_host);
        navHostFragment.getNavController().navigate(R.id.newPinCreatorFragment);
    }

    @OnClick(R.id.skip_button)
    public void skip() {
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.authentication_graph_host);
        navHostFragment.getNavController().navigate(R.id.newFingerprintCreatorFragment);
    }
}
