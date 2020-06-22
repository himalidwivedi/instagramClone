package com.geocrat.instagramclone.share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.geocrat.instagramclone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";


    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    FirebaseMethods mFirebaseMethods;

    //widgets
    EditText mCaption;

    //vars
    private String mAppend="file:/";
    private int imageCounts = 0;
    private String imageUrls;
    Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mFirebaseMethods = new FirebaseMethods(NextActivity.this);

        setUpFirebaseAuth();

        mCaption = findViewById(R.id.description);

        ImageView back = findViewById(R.id.ivBackArrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing activity");
                finish();
            }
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                //uploading the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload a new image", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_image))){
                    imageUrls = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCounts, imageUrls, null);
                }else{
                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCounts, null, bitmap);
                }
            }
        });

        setImage();
    }


    /*
        gets the image url from the incomming intent and displays the choosen image
     */
    private void setImage(){
        intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))){
            Log.d(TAG, "setImage: got new image url");
            imageUrls = intent.getStringExtra(getString(R.string.selected_image));
            UniversalImageLoader.setImage(imageUrls, image, null, mAppend);
        }else{
            Log.d(TAG, "setImage: got new bitmap");
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            image.setImageBitmap(bitmap);
        }
    }

    private void someMethod(){
        /*
            Step1)
                Create a data model for photos

            Step2)
                Add properties to the photo objects (caption, date, imageurl, photo_id, tags, user_id

            Step3)
                Count the number of photos that the user already has

            Step4)
                a) Upload the photo to the firebase storage
                b) insert into "photos" node
                c) insert into "users_photos" node
         */
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
        Toast.makeText(this, "image Count = " + imageCounts, Toast.LENGTH_SHORT).show();
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageCounts = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: imageCount" + imageCounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
