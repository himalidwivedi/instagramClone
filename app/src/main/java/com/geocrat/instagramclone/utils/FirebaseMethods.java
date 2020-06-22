package com.geocrat.instagramclone.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.home.HomeActivity;
import com.geocrat.instagramclone.models.Photo;
import com.geocrat.instagramclone.models.User;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private Context mContext;
    String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    //vars
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context mContext) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        this.mContext = mContext;

        if (mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to " + username);
        Toast.makeText(mContext, "username updated", Toast.LENGTH_SHORT).show();
        myRef.child(mContext.getString(R.string.dbname_users)).child(userID).child(mContext.getString(R.string.field_username)).setValue(username);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID).child(mContext.getString(R.string.field_username)).setValue(username);
    }

    public void updateEmail(String email){
        Log.d(TAG, "updateUsername: updating username to " + email);
        Toast.makeText(mContext, "username updated", Toast.LENGTH_SHORT).show();
        myRef.child(mContext.getString(R.string.dbname_users)).child(userID).child(mContext.getString(R.string.field_email)).setValue(email);
    }

    public void uploadNewPhoto(String photo_type, final String caption, int count, String imageUrls, Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: upload a photo");
        FilePaths filePaths = new FilePaths();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //case1: new photo
        if (photo_type.equals(mContext.getString(R.string.new_photo))){
            Toast.makeText(mContext, "uploading a new photo", Toast.LENGTH_SHORT).show();
            final StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count+1));

            //convert image uri to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imageUrls);
            }
            //now need to convert bitmps into bytes
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //uri is a download url for ur images that you store in the firebase storage
                    //everytime u will get a new uri, whenever you add a photo to the storage
                    Task<Uri> firebaseUrl = storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //add the new photo to the photos node and user_photos node
                            addPhotoToDatabase(caption, uri.toString());
                        }
                    });
                    Toast.makeText(mContext, "Photo upload successful", Toast.LENGTH_SHORT).show();

                    //navigate to the main feed so the user can see their photos
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress% from the amount of bytes transferred
                    double progress = (100 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    if ((progress - 15) > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload proceed to: " + progress + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress : " + progress + "% done");
                }
            });
        }

        //case2: new profile photo
        else if (photo_type.equals(mContext.getString(R.string.profile_photo))){
            Toast.makeText(mContext, "uploading a new profile photo", Toast.LENGTH_SHORT).show();
            final StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image uri to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imageUrls);
            }
            //now need to convert bitmps into bytes
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //uri is a download url for ur images that you store in the firebase storage
                    //everytime u will get a new uri, whenever you add a photo to the storage
                    Task<Uri> firebaseUrl = storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //add the new photo to the photos node and user_account_settings
                            setProfilePhoto(uri.toString());
                        }
                    });
                    Toast.makeText(mContext, "Photo upload successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress% from the amount of bytes transferred
                    double progress = (100 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    if ((progress - 15) > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload proceed to: " + progress + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress : " + progress + "% done");
                }
            });
        }
    }

    private void setProfilePhoto(String firebaseUrl){
        Log.d(TAG, "setProfilePhoto: setting profile photo " + firebaseUrl);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mAuth.getCurrentUser().getUid()).child(mContext.getString(R.string.profile_photo)).setValue(firebaseUrl);
    }

    private void addPhotoToDatabase(String caption, String firebaseUrl){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");
        //push() generates a unique key
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        String tags = StringManipulation.getTags(caption);
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(firebaseUrl);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds : dataSnapshot.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){
            count++;
        }
        return count;
    }

    public void updateUserAccountSettings(String displayname, String website, String description, long phoneNumber,String gender){
        if (displayname != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID).child(mContext.getString(R.string.field_display_name)).setValue(displayname);
        }
        if (website != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID).child(mContext.getString(R.string.field_website)).setValue(website);
        }
        if (description != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID).child(mContext.getString(R.string.field_description)).setValue(description);
        }
        if (phoneNumber != 0){
            myRef.child(mContext.getString(R.string.dbname_users)).child(userID).child(mContext.getString(R.string.field_phone_number)).setValue(phoneNumber);
        }
        if (gender != null){
            myRef.child(mContext.getString(R.string.dbname_users)).child(userID).child(mContext.getString(R.string.field_gender)).setValue(gender);
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");

        User user = new User();
        for (DataSnapshot ds : dataSnapshot.child(userID).getChildren()){
            Log.d(TAG, "checkIfUsernameExists: datasnapshot " + ds);
            user.setUsername(ds.getValue(User.class).getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH " + user.getUsername());
                return true;
            }
        }
        return false;
    }

    /*
        Register a new email and password to the firebase authentication
     */
    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(mContext, "Authentication succeed.",
                                    Toast.LENGTH_SHORT).show();

                            //send verification email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                    }else{
                        Toast.makeText(mContext, "couldn't send verification email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /*
        Adding info to the users node
        Adding info to the user_account_settings node
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo, String gender){
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username), gender);
        myRef.child(mContext.getString(R.string.dbname_users)).child(userID).setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );
        myRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID).setValue(settings);
    }

    /*
    retrieves the account settings for the user currently logged in

     Database: user_account_settings node
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving users account settings from the firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        for(DataSnapshot ds: dataSnapshot.getChildren()){

            //user_asccount_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))){
                Log.d(TAG, "getUserAccountSettings: dataSnapshot " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );

                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );

                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );

                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );

                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );

                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );

                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );

                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings " + settings.toString());
                }catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException : " + e.getLocalizedMessage() );
                }
            }

            //users node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: dataSnapshot " + ds);
                user.setUsername(ds.child(userID).getValue(User.class).getUsername());

                user.setEmail(ds.child(userID).getValue(User.class).getEmail());

                user.setPhone_number(ds.child(userID).getValue(User.class).getPhone_number());

                user.setUser_id(ds.child(userID).getValue(User.class).getUser_id());

                user.setGender(ds.child(userID).getValue(User.class).getGender());
                Log.d(TAG, "getUser: retrieved user " + user.toString());
            }
        }
        return new UserSettings(user, settings);
    }
}




















