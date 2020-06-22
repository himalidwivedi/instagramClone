package com.geocrat.instagramclone.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.models.Comment;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.utils.Like;
import com.geocrat.instagramclone.utils.MainfeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //vars
    private static final String TAG = "HomeFragment";
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private MainfeedListAdapter adapter;
    private ArrayList<String> following;
    private int mResults;

    //widgets
    ListView mListView;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = view.findViewById(R.id.listView);
        mPhotos = new ArrayList<>();
        following = new ArrayList<>();

        getFollowing();

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
                    //get photos
                    getPhotos();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getFollowing: NullPointerException" + e.getMessage());
        }
    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < following.size(); i++) {
            final int count = i;
            Query query = reference.child(getString(R.string.dbname_user_photos))
                    .child(following.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(following.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found current user" + ds.child(getString(R.string.field_user_id)).getValue());

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.image_path)).toString());

                        ArrayList<Comment> comments = new ArrayList<>();
                        for (DataSnapshot dSnapshot : ds.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }
                        photo.setComments(comments);

                        mPhotos.add(photo);

                        if (count >= following.size()-1){
                            //display our photos
                            displayPhotos();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if (mPhotos != null){
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();
                if (iterations > 10){
                    iterations = 10;
                }
                mResults = 10;

                for (int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                adapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(adapter);

            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage());
            }catch (ArrayIndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: ArrayIndexOutOfBoundsException" + e.getMessage());
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try {
            if (mPhotos.size() > mResults && mPhotos.size() > 0){
                int iterations;
                if (mPhotos.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are more than 10 photos");
                    iterations  = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there are less than 10 photos");
                    iterations = mPhotos.size() - mResults;
                }

                //adding new photos to the paginated results
                for (int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                adapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayMorePhotos: NullPointerException" + e.getMessage());
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "displayMorePhotos: ArrayIndexOutOfBoundsException" + e.getMessage());
        }
    }
}



















