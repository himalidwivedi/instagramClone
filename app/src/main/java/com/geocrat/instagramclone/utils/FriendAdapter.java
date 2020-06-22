package com.geocrat.instagramclone.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.models.UserAccountSettings;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter <FriendAdapter.Viewholder>{

    ArrayList<UserAccountSettings> settings;
    Context context;

    public FriendAdapter(ArrayList<UserAccountSettings> settings, Context context) {
        this.settings = settings;
        this.context = context;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_friends_listitem, parent, false);
        Viewholder holder = new Viewholder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    class Viewholder extends RecyclerView.ViewHolder{
        TextView friend_username, friend_displayname;
        CircleImageView friend_photo;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            friend_displayname = itemView.findViewById(R.id.friend_displayname);
            friend_username = itemView.findViewById(R.id.friend_username);
            friend_photo = itemView.findViewById(R.id.friend_photo);
        }
    }
}
