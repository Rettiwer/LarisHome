package com.rettiwer.pl.laris.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.api.room.Room;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomItemViewHolder> {
    private List<Room> rooms;
    private Context mContext;

    public RoomListAdapter(Context context, ArrayList<Room> rooms) {
        this.rooms = rooms;
        this.mContext = context;
    }

    @Override
    public RoomItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_list_item, parent, false);
        RoomItemViewHolder viewHolder = new RoomItemViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomItemViewHolder holder, int position) {
        TypedArray icons = mContext.getResources().obtainTypedArray(R.array.room_icons);
        String[] roomIconNames = mContext.getResources().getStringArray(R.array.room_icon_names);

        for (int i = 0; i < roomIconNames.length; i++) {
            if(roomIconNames[i].equals(rooms.get(position).getIcon())) {
                holder.logo.setImageDrawable(icons.getDrawable(i));
                break;
            }
        }

        holder.logo.setColorFilter(Color.parseColor(rooms.get(position).getIconColor()));
        holder.name.setText(rooms.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public class RoomItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.room_icon)
        public ImageView logo;

        @BindView(R.id.room_name)
        public TextView name;

        public RoomItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick
        void openRoom(View view) {
            Navigation.findNavController(view).navigate(RoomListFragmentDirections.
                    actionRoomListFragmentToRoomDevicesFragment(rooms.get(getAdapterPosition()).getId()));
        }

        @OnLongClick
        void manageRoom(View view) {
            //TODO: Build room configuration menu
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String[] options = {"Usuń", "Zmień nazwę"};
            builder.setTitle(name.getText())
                    .setItems(options, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.create().show();
        }
    }
}
