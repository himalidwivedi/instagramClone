package com.geocrat.instagramclone.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.home.HomeActivity;
import com.geocrat.instagramclone.models.Comment;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.models.User;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {


    public interface OnLoadMoreItemsListner{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListner mOnLoadMoreItemsListner;

    private static final String TAG = "MainfeedListAdapter";
    private Context mContext;
    private int mLayoutResource;
    private LayoutInflater mLayoutInflater;
    private DatabaseReference reference;
    private String currentUsername="";

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        this.mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{
        CircleImageView profile_photo;
        TextView username, text_likes, text_caption, text_comments_link, text_time_posted, text_user, liked_by;
        ImageView more_menu, image_heart, image_heart_red, image_comment, image_message, image_save, image_save_black;
        SquareImageView post_image;
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            convertView = mLayoutInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.liked_by = (TextView)convertView.findViewById(R.id.liked_by);
            holder.text_likes = (TextView) convertView.findViewById(R.id.text_likes);
            holder.text_caption = (TextView) convertView.findViewById(R.id.text_caption);
            holder.text_user = (TextView) convertView.findViewById(R.id.text_user);
            holder.text_comments_link = (TextView) convertView.findViewById(R.id.text_comments_link);
            holder.text_time_posted = (TextView) convertView.findViewById(R.id.text_time_posted);
            holder.more_menu = (ImageView) convertView.findViewById(R.id.more_menu);
            holder.image_heart = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.image_heart_red = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.image_comment = (ImageView) convertView.findViewById(R.id.image_comment);
            holder.image_message = (ImageView) convertView.findViewById(R.id.image_message);
            holder.image_save_black = (ImageView) convertView.findViewById(R.id.image_save_black);
            holder.image_save = (ImageView) convertView.findViewById(R.id.image_save);
            holder.profile_photo = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.post_image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heart = new Heart(holder.image_heart, holder.image_heart_red);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListner(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        //get the current user's username (needed for checking like string)
        getCurrentUsername();

        //get Likes string
        getLikesString(holder);

        //set the comment
        holder.text_comments_link.setText("View all comments");
        holder.text_comments_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());
                ((HomeActivity)mContext).onCommentThreadSelectedListner(getItem(position), mContext.getString(R.string.home_activity));

                //going to write a code to do something else
                ((HomeActivity)mContext).hideLayout();
            }
        });

        //set the time it was posted
        String timeStampDifference = getTimeStampDifference(getItem(position));
        if (!timeStampDifference.equals("0")){
            holder.text_time_posted.setText(timeStampDifference + " days ago");
        }else{
            holder.text_time_posted.setText("today");
        }

        //set the post image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.post_image);

        //set profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    currentUsername = ds.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user : " + ds.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(ds.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of " + holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(ds.getValue(UserAccountSettings.class).getProfile_photo(), holder.profile_photo);
                    holder.profile_photo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of " + holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    holder.settings = ds.getValue(UserAccountSettings.class);
                    holder.image_comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).onCommentThreadSelectedListner(getItem(position), mContext.getString(R.string.home_activity));
                            //another thing to do
                            ((HomeActivity)mContext).hideLayout();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //setting caption
        holder.text_caption.setText(getItem(position).getCaption());

        //get the user object
        Query userQuery = reference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user " + ds.getValue(User.class).getUsername());
                    holder.user = ds.getValue(User.class);
                }
                if (holder.text_caption.getText().toString().equals("")){
                    holder.text_user.setText("");
                    holder.text_caption.setText("(No caption)");
                    holder.text_caption.setTextColor(Color.parseColor("#bfbfbf"));
                }else{
                    holder.text_user.setText(holder.user.getUsername() + " ");
                    holder.text_user.setTextColor(Color.parseColor("#000000"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }

    private void loadMoreData(){
        try{
            mOnLoadMoreItemsListner = (OnLoadMoreItemsListner) getContext();
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException" + e.getMessage());
        }

        try{
            mOnLoadMoreItemsListner.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: NullPointerException" + e.getMessage());
        }
    }


    public class GestureListner extends GestureDetector.SimpleOnGestureListener{
        ViewHolder mHolder;
        public GestureListner(ViewHolder holder){
            mHolder = holder;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos)).child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String keyId = ds.getKey();
                        //Case1) user already liked the photo
                        if (mHolder.likedByCurrentUser && ds.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            reference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes)).child(keyId).removeValue();

                            reference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes)).child(keyId).removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }

                        //case2) the user hasn't liked the photo
                        else if (!mHolder.likedByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: Retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    currentUsername = ds.getValue(UserAccountSettings.class).getUsername();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewLike(ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");
        String newLikeId = reference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes)).child(newLikeId).setValue(like);

        reference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId).setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos)).child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference.child(mContext.getString(R.string.dbname_users)).orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(ds.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like : " + ds.getValue(User.class).getUsername());
                                    holder.users.append(ds.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }
                                String[] splitUsers = holder.users.toString().split(",");
                                if (holder.users.toString().contains(currentUsername + ",")) {
                                    holder.likedByCurrentUser = true;
                                } else {
                                    holder.likedByCurrentUser = false;
                                }
                                int length = splitUsers.length;
                                if(length == 0){
                                    holder.mLikesString = "Be the first to like this post";
                                }
                                else if (length == 1) {
                                    holder.mLikesString = splitUsers[0];
                                } else if (length == 2) {
                                    holder.mLikesString = splitUsers[0] + " and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.mLikesString = splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.mLikesString = splitUsers[0] + " ," + splitUsers[1] + " ," + splitUsers[2] + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.mLikesString = splitUsers[0] + ", " +
                                            splitUsers[1] + ", " +
                                            splitUsers[2] + ", " +
                                            splitUsers[3] +
                                            " and " + (splitUsers.length - 3) +
                                            " others";
                                }
                                setUpLikeString(holder, holder.mLikesString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    if (!dataSnapshot.exists()) {
                        holder.mLikesString = "Be the first to like this post";

                        holder.likedByCurrentUser = false;
                        setUpLikeString(holder, holder.mLikesString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getLikesString: " + e.getMessage());
            holder.likedByCurrentUser = false;
            holder.mLikesString = "";
            //set up like string
            setUpLikeString(holder, holder.mLikesString);
        }
    }

    private  void setUpLikeString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setUpLikeString: likes string " + holder.mLikesString);

        if (holder.mLikesString.equals("Be the first to like this post")) {
            holder.liked_by.setText("");
            holder.text_likes.setTextColor(Color.parseColor("#bfbfbf"));
        }else{
            holder.liked_by.setText("Liked by: ");
            holder.text_likes.setTextColor(Color.parseColor("#000000"));
        }

        if (holder.likedByCurrentUser) {
            Log.d(TAG, "setUpLikeString: photo is liked by current user");
            holder.image_heart.setVisibility(View.GONE);
            holder.image_heart_red.setVisibility(View.VISIBLE);
            holder.image_heart_red.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }else {
            Log.d(TAG, "setUpLikeString: photo is not liked by the current user");
            holder.image_heart.setVisibility(View.VISIBLE);
            holder.image_heart_red.setVisibility(View.GONE);
            holder.image_heart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.text_likes.setText(likesString);
    }

    private String getTimeStampDifference(Photo photo){
        Log.d(TAG, "getTimeStampDifference: getting timeStamp difference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }catch(ParseException e){
            Log.e(TAG, "getTimeStampDifference: ParseException "+ e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
