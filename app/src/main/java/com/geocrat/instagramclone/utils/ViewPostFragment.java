package com.geocrat.instagramclone.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.models.User;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.utils.BottomNavigationViewHelper;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.geocrat.instagramclone.utils.GridImageAdapter;
import com.geocrat.instagramclone.utils.SquareImageView;
import com.geocrat.instagramclone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPostFragment extends Fragment {

    public interface OnCommentThreadSelectedListner{
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListner mOnCommentThreadSelectedListner;

    //vars
    Photo mPhoto;
    int mActivityNumber = 0;
    private String photoUsername = "";
    private String photoUrl = "";
    private UserAccountSettings userAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikeString = "";
    private User mCurrentUser;

    //widgets
    SquareImageView postImage;
    TextView mBackLabel, mCaption, mUserName, mTimeStamp, mLikes, mComments, text_user, likedBy;
    ImageView mBackArrow, mProfilePhoto, mElipse, mHeartRed, mHeartWhite, mComment, mMessage, mSaveWhite, mSaveBlack;
    BottomNavigationViewEx bottomNavigationViewEx;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    FirebaseMethods mFirebaseMethods;

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mUserName = view.findViewById(R.id.username);
        text_user = view.findViewById(R.id.text_user);
        mCaption = view.findViewById(R.id.text_caption);
        mTimeStamp = view.findViewById(R.id.text_time_posted);
        mBackArrow = view.findViewById(R.id.backArrow);
        mElipse = view.findViewById(R.id.more_menu);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mHeartWhite = view.findViewById(R.id.image_heart);
        mSaveBlack = view.findViewById(R.id.image_save_black);
        mSaveWhite = view.findViewById(R.id.image_save);
        mComment = view.findViewById(R.id.image_comment);
        mMessage = view.findViewById(R.id.image_message);
        mComments = view.findViewById(R.id.text_comments_link);
        likedBy = view.findViewById(R.id.liked_by);
        mLikes = view.findViewById(R.id.text_likes);

        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);

        mGestureDetector = new GestureDetector(getActivity(), new GestureListner());
        mHeart = new Heart(mHeartWhite, mHeartRed);

        postImage = view.findViewById(R.id.post_image);
        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), postImage, null, "");
            getCurrentUser();
            getPhotoDetails();
            getLikesString();
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: NullPointerException: photo was null" + e.getMessage());
        }

        setUpFirebaseAuth();
        setUpBottomNavigationView();

        getPhotoDetails();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListner = (OnCommentThreadSelectedListner) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage() );
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_photos)).child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference.child(getString(R.string.dbname_users)).orderByChild(getString(R.string.field_user_id))
                            .equalTo(ds.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like : " + ds.getValue(User.class).getUsername());
                                mUsers.append(ds.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }
                            String []splitUsers = mUsers.toString().split(",");
                            if (mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }
                            int length = splitUsers.length;
                            if (length == 1){
                                mLikeString = splitUsers[0];
                            }
                            else if (length == 2){
                                mLikeString = splitUsers[0] + " and " + splitUsers[1];
                            }
                            else if (length == 3){
                                mLikeString = splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2];
                            }
                            else if (length == 4){
                                mLikeString = splitUsers[0] + " ," + splitUsers[1] + " ," + splitUsers[2] + " and " + splitUsers[3];
                            }
                            else if (length > 4){
                                mLikeString = splitUsers[0] + ", " +
                                        splitUsers[1] + ", " +
                                        splitUsers[2] + ", " +
                                        splitUsers[3] +
                                        " and " + (splitUsers.length-3) +
                                        " others";
                            }
                            setUpWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if (!dataSnapshot.exists()){
                    mLikeString = "Be the first to like this post";
                    mLikedByCurrentUser = false;
                    setUpWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_users)).orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    mCurrentUser = ds.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void getPhotoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_account_settings)).orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    userAccountSettings = ds.getValue(UserAccountSettings.class);
                }
//                setUpWidgets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }


    public class GestureListner extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_photos)).child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String keyId = ds.getKey();
                        //Case1) user already liked the photo
                        if (mLikedByCurrentUser && ds.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes)).child(keyId).removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes)).child(keyId).removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }

                        //case2) the user hasn't liked the photo
                        else if (!mLikedByCurrentUser){
                            //add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()){
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");
        String newLikeId = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes)).child(newLikeId).setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes)).child(newLikeId).setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void setUpWidgets(){
        String timeStampDiff = getTimeStampDifference();
        if (!timeStampDiff.equals("0")){
            mTimeStamp.setText(timeStampDiff + " days ago");
        }else{
            mTimeStamp.setText("today");
        }
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfilePhoto,null, "");

        mCaption.setText(mPhoto.getCaption());
        if (mPhoto.getCaption() == null){
            mCaption.setText("(No caption)");
            mCaption.setTextColor(Color.parseColor("#bfbfbf"));
            text_user.setText("");
        }else{
            text_user.setText(userAccountSettings.getUsername() + " ");
        }

        mUserName.setText(userAccountSettings.getUsername());
        mLikes.setText(mLikeString);
        if (mLikeString.equals("Be the first to like this post")) {
            mLikes.setTextColor(Color.parseColor("#bfbfbf"));
            likedBy.setText("");
        }else{
            mLikes.setTextColor(Color.parseColor("#000000"));
            likedBy.setText("Liked by");
        }

        //need to be fixed
        if (mPhoto.getComments() != null){
            mComments.setText("View all comments");
        }else{
            mComments.setText("No comments yet");
        }

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");
                mOnCommentThreadSelectedListner.onCommentThreadSelectedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCommentThreadSelectedListner.onCommentThreadSelectedListener(mPhoto);
            }
        });

        if (mLikedByCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }else{
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    private String getTimeStampDifference(){
        Log.d(TAG, "getTimeStampDifference: getting timeStamp difference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = mPhoto.getDate_created();
        try{
            timestamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }catch(ParseException e){
            Log.e(TAG, "getTimeStampDifference: ParseException "+ e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up bottom navigation view");
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationViewEx);
        mActivityNumber = getActivityNumberFromBundle();
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);        //this will highlight the item at index 4 of the bot nav bar when this activity is clicked
        menuItem.setChecked(true);
    }

    //retrieving bundle from ProfileFragment
    private int getActivityNumberFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null){
            return  bundle.getInt(getString(R.string.activity_number));
        }else{
            return 0;
        }
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
