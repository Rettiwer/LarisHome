package com.rettiwer.pl.laris.ui.authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import io.reactivex.disposables.CompositeDisposable;

import android.os.Bundle;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.user.AccountConfiguration;

public class AuthenticationActivity extends AppCompatActivity {
    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        String refreshToken = new AccountConfiguration(this).getRefreshToken();
        if (refreshToken != null) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.authentication_graph_host);
            navHostFragment.getNavController().navigate(R.id.home_activity);
            finish();
        }
    }

    public CompositeDisposable getCompositeDisposable() {
        return mDisposable;
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}
