package com.rettiwer.pl.laris.ui.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dgreenhalgh.android.simpleitemdecoration.grid.GridDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.RoomDatabaseManager;
import com.rettiwer.pl.laris.data.remote.ConnectionEvent;
import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.RetryWhenNoConnection;
import com.rettiwer.pl.laris.data.remote.api.ApiResponse;
import com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent;
import com.rettiwer.pl.laris.data.remote.api.SingleCallbackWrapper;
import com.rettiwer.pl.laris.data.remote.api.room.Room;
import com.rettiwer.pl.laris.data.remote.api.room.RoomService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.NETWORK_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.RequestExceptionType;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.TIMEOUT;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_API_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_ERROR;

public class RoomListFragment extends Fragment {

    private static final String TAG = RoomListFragment.class.getName();

    @BindView(R.id.swipeToRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.no_rooms_text)
    TextView mNoRoomsTextView;

    @BindView(R.id.room_list)
    RecyclerView mRoomRecyclerView;

    @BindView(R.id.building_spinner)
    Spinner mBuildingSpinner;

    private GridDividerItemDecoration mGridDividerItemDecoration;

    private Context mContext;

    private View mView;

    private ConnectionManager mConnectionManager;

    private RoomListAdapter mRoomListAdapter;

    private Snackbar mErrorSnackbar;

    private RoomDatabaseManager mRoomDatabaseManager;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_room_list, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRoomDatabaseManager = RoomDatabaseManager.getInstance(mContext);
        mConnectionManager = App.getInstance(mContext).getConnectionManager();

        ButterKnife.bind(this, view);

        mRoomListAdapter = new RoomListAdapter(mContext, new ArrayList<>());

        mRoomRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        mRoomRecyclerView.setAdapter(mRoomListAdapter);

        getRemoteRooms();

        List<String> buildings = new ArrayList<>();
        buildings.add("Dom");

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, buildings);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mBuildingSpinner.setAdapter(dataAdapter);

        if (buildings.size() == 1) {
            mBuildingSpinner.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mBuildingSpinner.setEnabled(false);
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            getRemoteRooms();
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    @OnClick(R.id.add_room)
    public void addNewRoom() {
        Navigation.findNavController(mView).navigate(R.id.newRoomFragment);
    }

    private void showRoomsList(List<Room> rooms) {
        if (!rooms.isEmpty()) {
            if (mNoRoomsTextView.getVisibility() != View.GONE) {
                mHandler.post(() -> {
                    mNoRoomsTextView.setVisibility(View.GONE);
                    mRoomRecyclerView.setVisibility(View.VISIBLE);
                });
            }

            mRoomListAdapter.getRooms().clear();
            mRoomListAdapter.getRooms().addAll(rooms);

            if (mRoomListAdapter.getItemCount() >= 2) {
                Drawable horizontalDivider = ContextCompat.getDrawable(mContext, R.drawable.room_list_divider);
                Drawable verticalDivider = ContextCompat.getDrawable(mContext, R.drawable.room_list_divider);
                mGridDividerItemDecoration = new GridDividerItemDecoration(horizontalDivider, verticalDivider, 2);
                mRoomRecyclerView.addItemDecoration(mGridDividerItemDecoration);
            } else {
                mRoomRecyclerView.removeItemDecoration(mGridDividerItemDecoration);
            }

            mRoomListAdapter.notifyDataSetChanged();
        }
        else {
            if (mNoRoomsTextView.getVisibility() == View.GONE) {
                mHandler.post(() -> {
                    mNoRoomsTextView.setVisibility(View.VISIBLE);
                    mRoomRecyclerView.setVisibility(View.GONE);
                });
            }
        }
    }

    @SuppressLint("CheckResult")
    private void getRemoteRooms() {
        mConnectionManager.getRetrofit()
                .retryWhen(new RetryWhenNoConnection())
                .subscribe(
                        retrofit -> {
                            if (mErrorSnackbar != null) {
                                mErrorSnackbar.dismiss();
                            }
                            RoomService roomService = retrofit.create(RoomService.class);
                            roomService.getRooms().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                                    .mainThread()).subscribeWith(new SingleCallbackWrapper<ApiResponse<Room>>() {
                                @Override
                                protected void onReceive(ApiResponse<Room> roomApiResponse) {
                                    showRoomsList(roomApiResponse.getData());
                                }
                            });
                        });
    }

    @Subscribe
    public void connectionEventReceiver(ConnectionEvent event) {
        if (mErrorSnackbar == null) {
            mErrorSnackbar = Snackbar.make(mView, getText(event.getData()), Snackbar.LENGTH_INDEFINITE);
            mErrorSnackbar.show();
        }
    }

    @Subscribe
    public void requestExceptionEventHandler(RequestExceptionEvent event) {
        @RequestExceptionType String exception = event.getRequestExceptionType();

        switch (exception) {
            case TIMEOUT: {
                Snackbar.make(mView, R.string.request_timeout, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case NETWORK_ERROR: {
                Snackbar.make(mView, R.string.network_api_error, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case UNKNOWN_API_ERROR:
            case UNKNOWN_ERROR: {
                Snackbar.make(mView, event.getUnknownError(), Snackbar.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
