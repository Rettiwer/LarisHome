package com.rettiwer.pl.laris.ui.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.RetryWhenNoConnection;
import com.rettiwer.pl.laris.data.remote.ConnectionEvent;
import com.rettiwer.pl.laris.data.remote.api.ApiResponse;
import com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent;
import com.rettiwer.pl.laris.data.remote.api.SingleCallbackWrapper;

import com.rettiwer.pl.laris.data.remote.api.room.Room;
import com.rettiwer.pl.laris.data.remote.api.room.RoomService;
import com.rettiwer.pl.laris.data.remote.api.user.User;
import com.rettiwer.pl.laris.data.remote.api.user.UserService;
import com.rettiwer.pl.laris.ui.widget.CarouselPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.NETWORK_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.TIMEOUT;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_API_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_ERROR;

public class NewRoomFragment extends Fragment {
    @BindView(R.id.room_icon_picker)
    CarouselPicker mRoomIconCarouselPicker;

    @BindView(R.id.color_picker)
    RecyclerView mColorPickerRecyclerView;

    @BindView(R.id.room_members_checkbox)
    RecyclerView mRoomMembersRecyclerView;

    @BindView(R.id.room_name_input_layout)
    TextInputLayout mRoomName;

    private Context mContext;

    private View mView;

    private ConnectionManager mConnectionManager;

    private Snackbar mErrorSnackbar;

    private CheckListAdapter mCheckListAdapter;

    private String currentColor;

    public NewRoomFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_new_room, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mConnectionManager = App.getInstance(mContext).getConnectionManager();

        ButterKnife.bind(this, view);

        String colors[] = mContext.getResources().getStringArray(R.array.room_icon_colors);
        List<String> roomIconColors = Arrays.asList(colors);


        List<CarouselPicker.DrawableItem> roomIcons = new ArrayList<>();
        TypedArray icons = mContext.getResources().obtainTypedArray(R.array.room_icons);
        String[] roomIconNames = mContext.getResources().getStringArray(R.array.room_icon_names);

        for (int i = 0; i < icons.length(); i++) {
            roomIcons.add(new CarouselPicker.DrawableItem(icons.getDrawable(i), roomIconNames[i]));
        }
        icons.recycle();

        CarouselPicker.CarouselViewAdapter iconAdapter =
                new CarouselPicker.CarouselViewAdapter(mContext, roomIcons, 0, roomIconColors.get(0));
        mRoomIconCarouselPicker.setAdapter(iconAdapter);
        currentColor = roomIconColors.get(0);

        mColorPickerRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 5));
        ColorPickerAdapter colorPickerAdapter =
                new ColorPickerAdapter(roomIconColors, item -> {
                    mRoomIconCarouselPicker.changeItemsColor(item);
                    currentColor = item;
                });
        mColorPickerRecyclerView.setAdapter(colorPickerAdapter);


        mRoomMembersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mCheckListAdapter = new CheckListAdapter();
        mRoomMembersRecyclerView.setAdapter(mCheckListAdapter);

        loadUsers();
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.add_new_room_button)
    protected void addRoom() {
        if (TextUtils.isEmpty(mRoomName.getEditText().getText())) {
            mRoomName.setError(getText(R.string.room_name_cannot_be_empty));
            return;
        }

        if (mRoomName.getEditText().getText().length() > 32) {
            mRoomName.setError(getText(R.string.room_name_cannot_be_empty));
            return;
        }

        mConnectionManager.getRetrofit()
                .retryWhen(new RetryWhenNoConnection())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        retrofit -> {
                            if (mErrorSnackbar != null) {
                                mErrorSnackbar.dismiss();
                            }

                            CarouselPicker.DrawableItem icon = ((CarouselPicker.CarouselViewAdapter)mRoomIconCarouselPicker.getAdapter()).getItems()
                                    .get(mRoomIconCarouselPicker.getCurrentItem());

                            Room room = new Room();
                            room.setName(mRoomName.getEditText().getText().toString());
                            room.setIcon(icon.getIconName());
                            room.setIconColor(currentColor);
                            room.setMembers(mCheckListAdapter.getCheckedUsers());

                            RoomService roomService = retrofit.create(RoomService.class);
                            roomService.addRoom(room).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                                    .mainThread()).subscribeWith(new SingleCallbackWrapper<ApiResponse<Room>>() {
                                @Override
                                protected void onReceive(ApiResponse<Room> roomApiResponse) {
                                    Navigation.findNavController(mView)
                                            .navigate(NewRoomFragmentDirections.actionRoomCreatorFragmentToRoomListFragment());
                                }
                            });
                        });
    }

    @SuppressLint("CheckResult")
    private void loadUsers() {
        mConnectionManager.getRetrofit()
                .retryWhen(new RetryWhenNoConnection())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        retrofit -> {
                            if (mErrorSnackbar != null) {
                                mErrorSnackbar.dismiss();
                            }
                            UserService userService = retrofit.create(UserService.class);
                            userService.getUsers().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                                    .mainThread()).subscribeWith(new SingleCallbackWrapper<ApiResponse<User>>() {
                                @Override
                                protected void onReceive(ApiResponse<User> userApiResponse) {
                                    mCheckListAdapter.addUsers(userApiResponse.getData());
                                    mCheckListAdapter.notifyDataSetChanged();
                                }
                            });
                        });
    }

    @Subscribe
    public void torConnectionEventReceived(ConnectionEvent event) {
        if (mErrorSnackbar == null) {
            mErrorSnackbar = Snackbar.make(mView, getText(event.getData()), Snackbar.LENGTH_INDEFINITE);
            mErrorSnackbar.show();
        }
    }

    @Subscribe
    public void requestExceptionEventHandler(RequestExceptionEvent event) {
        @RequestExceptionEvent.RequestExceptionType String exception = event.getRequestExceptionType();

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
