package com.geocrat.instagramclone.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.models.Comment;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.utils.CameraAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {
    private ListView listView;
    CameraAdapter adapter;
    private static final String TAG = "CameraFragment";
    private ArrayList<UserAccountSettings> settings;
    private ArrayList<String> following;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        settings = new ArrayList<>();
        following = new ArrayList<>();
        getFollowing();
        getUser();
        listView = view.findViewById(R.id.listview);
        adapter = new CameraAdapter(getActivity(), R.layout.layout_friends_listitem, settings);
    return view;
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        try {
            Query query = reference.child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found current user" + ds.child(getString(R.string.field_user_id)).getValue());

                        following.add(ds.child(getString(R.string.field_user_id)).getValue().toString());
                    }
                    following.add(FirebaseAuth.getInstance().getUid());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getFollowing: NullPointerException" + e.getMessage());
        }
    }

    private void getUser(){
        Log.d(TAG, "getUser: getting user");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < following.size(); i++) {
            final int count = i;
            Query query = reference.child(getString(R.string.dbname_user_account_settings))
                    .child(following.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(following.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found user account" + ds.child(getString(R.string.field_user_id)).getValue());
                        settings.add((UserAccountSettings) ds.getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
