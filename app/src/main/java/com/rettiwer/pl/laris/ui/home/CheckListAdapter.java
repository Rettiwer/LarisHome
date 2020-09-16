package com.rettiwer.pl.laris.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.remote.api.user.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ItemViewHolder> {
    private List<User> mUsers = new ArrayList<>();
    private List<String> mCheckedUsers = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_list_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        holder.mUserName.setText(mUsers.get(position).getFirstName());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void addUsers(List<User> users) {
        this.mUsers.addAll(users);
    }

    public List<String> getCheckedUsers() {
        return this.mCheckedUsers;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_selected_checkbox)
        CheckBox mCheckBox;

        @BindView(R.id.user_name_text)
        TextView mUserName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mCheckBox.setClickable(false);
        }

        @OnClick
        void onCheckItem() {
            mCheckBox.setChecked(!mCheckBox.isChecked());
            if (mCheckBox.isChecked()) {
                mCheckedUsers.add(mUsers.get(getAdapterPosition()).getId());
            }
            else {
                mCheckedUsers.remove(mUsers.get(getAdapterPosition()));
            }
        }
    }
}