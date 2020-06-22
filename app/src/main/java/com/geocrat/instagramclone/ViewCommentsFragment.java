package com.geocrat.instagramclone;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geocrat.instagramclone.home.HomeActivity;
import com.geocrat.instagramclone.models.Comment;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.utils.CommentListAdapter;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.geocrat.instagramclone.utils.GridImageAdapter;
import com.geocrat.instagramclone.utils.Like;
import com.geocrat.instagramclone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "ViewCommentsFragment";

    //widgets
    private TextView beTheFirst;
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    ListView mListView;

    //vars
    private Photo mPhoto;
    ArrayList<Comment> mComments;
    Context mContext;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    FirebaseMethods mFirebaseMethods;


    public ViewCommentsFragment() {
        // Required empty public constructor
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        beTheFirst = view.findViewById(R.id.be_the_first);
        mContext = getActivity();

        mComments = new ArrayList<>();

        try {
            mPhoto = getPhotoFromBundle();
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: NullPointerException: photo was null" + e.getMessage());
        }

        setUpFirebaseAuth();
        return view;
    }

    private void setUpWidgets(){
        CommentListAdapter adapter = new CommentListAdapter(mContext, R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyboard();

                }else{
                    Toast.makeText(getActivity(), "You cant post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                try {
                    if (getCallingActivityFromBundle().equals(getString(R.string.home_activity))) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        ((HomeActivity) mContext).showLayout();
                    } else {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException" + e.getMessage());
                }
            }
        });
    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null){
            InputMethodManager inm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment){
        String commentID = myRef.push().getKey();
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into photo node
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //insert into user_photo node
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }


    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null){
            return  bundle.getParcelable(getString(R.string.photo));
        }else{
            return null;
        }
    }

    private String getCallingActivityFromBundle(){
        Log.d(TAG, "getCallingActivityFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null){
            return  bundle.getString(getString(R.string.home_activity));
        }else{
            return null;
        }
    }

    /*
     *********************************** firebase stuff**********************************
     */

    /*
        setup firebase auth
     */
    private void setUpFirebaseAuth(){
        Log.d(TAG, "setUpFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                if (user != null){
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: " + user.getUid());
//                    Toast.makeText(HomeActivity.this, user.getUid(), Toast.LENGTH_SHORT).show();
                }else{
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
//                    Toast.makeText(HomeActivity.this, "signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        myRef.child((mContext.getString(R.string.dbname_photos))).child(mPhoto.getPhoto_id()).child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Query query = myRef.child(mContext.getString(R.string.dbname_photos)).orderByChild(mContext.getString(R.string.field_photo_id)).equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String , Object>)ds.getValue();
                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.image_path)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());
                                    mComments.add(firstComment);

                                    for (DataSnapshot dSnapshot : ds.child(mContext.getString(R.string.field_comments)).getChildren()){
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                        beTheFirst.setVisibility(View.GONE);
                                    }
                                    photo.setComments(mComments);
                                    mPhoto = photo;
                                    setUpWidgets();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        Query query = myRef.child(mContext.getString(R.string.dbname_photos)).orderByChild(mContext.getString(R.string.field_photo_id)).equalTo(mPhoto.getPhoto_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String , Object>)ds.getValue();
                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(mContext.getString(R.string.image_path)).toString());

                    mComments.clear();
                    Comment firstComment = new Comment();
                    firstComment.setComment(mPhoto.getCaption());
                    firstComment.setUser_id(mPhoto.getUser_id());
                    firstComment.setDate_created(mPhoto.getDate_created());
                    mComments.add(firstComment);

                    for (DataSnapshot dSnapshot : ds.child(mContext.getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        mComments.add(comment);
                    }
                    photo.setComments(mComments);
                    mPhoto = photo;
                    setUpWidgets();

//                    List<Like> likesList = new ArrayList<>();
//                    for (DataSnapshot dSnapshot : ds.child(getString(R.string.field_likes)).getChildren()){
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);

        FirebaseUser user = mAuth.getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null){
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }
}
