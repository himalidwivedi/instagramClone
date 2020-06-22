package com.geocrat.instagramclone.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.dialog.ConfirmPasswordDialog;
import com.geocrat.instagramclone.models.User;
import com.geocrat.instagramclone.models.UserAccountSettings;
import com.geocrat.instagramclone.models.UserSettings;
import com.geocrat.instagramclone.share.ShareActivity;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.geocrat.instagramclone.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListner {

    private static final String TAG = "EditProfileFragment";
//    private  ImageView mProfilePhoto;
    ImageView cross;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    FirebaseMethods mFirebaseMethods;

    //vars
    UserSettings mUserSettings;

    //Edit Profile Fragment widgets
    private EditText mDisplayName, mUserName, mWebsite, mDescription, mEmail, mPhoneNumber, mGender;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.displayName);
        mUserName = (EditText) view.findViewById(R.id.userName);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phone);
        mGender = (EditText) view.findViewById(R.id.gender);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);                     //it will differentiate between the navigation from bottom navbar share button and editProfileFragment's change profile photo TextView to the ShareActivity
                getActivity().startActivity(intent);
            }
        });

        mFirebaseMethods = new FirebaseMethods(getActivity());

        cross = view.findViewById(R.id.cross);

        setUpBackButton();
//        setProfileImage();
        setUpFirebaseAuth();

        ImageView checkMark = (ImageView) view.findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                saveProfileSettings();
                Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void setUpBackButton(){
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
//        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from the firebase " + userSettings.toString());
        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        mUserSettings = userSettings;
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUserName.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));
        mGender.setText(userSettings.getUser().getGender());
    }

    /*
    Retrieving the data contained in the widgets and submits it to the database
    Before doing so it checks to make sure the username choosen is unique
     */
    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String userName = mUserName.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());
        final String gender = mGender.getText().toString();

        //case1: if the user made the change to its username
        if (!mUserSettings.getUser().getUsername().equals(userName)){
            checkIfUserNameExists(userName);
        }
        //case1: if the user made the change to its username
        if (!mUserSettings.getUser().getEmail().equals(email)){
            //step1: Reauthenticate
                //-confirm the email and password
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
            //step2: check if the email is already registered
                //fetchingProvidersForEmail(String Email)
            //step3: change the email
                //submit the new email to the database and authentication
        }

        if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0, null);
        }
        if (!mUserSettings.getSettings().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0, null);
        }
        if (!mUserSettings.getSettings().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0, null);
        }
        if (mUserSettings.getUser().getPhone_number() != phoneNumber){
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber, null);
        }
        if (!mUserSettings.getUser().getGender().equals(gender)){
            mFirebaseMethods.updateUserAccountSettings(null, null, null, 0, gender);
        }
    }

    private void checkIfUserNameExists(final String username){
        Log.d(TAG, "checkIfUserNameExists: Checking if username already exists");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_users)).orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    //adding username
                    mFirebaseMethods.updateUsername(username);
                }
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUserNameExists: Found a match " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "that username already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //This method is used ro retrieve data from firebase
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //retrieving user's data from the firebase

                //retrieving user's images from firebase
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

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password " + password);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            //to check if the email is not already present in the database
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                                if (task.isSuccessful()) {
                                                    try{

//                                                ***********************email is already in use**********************************************
                                                        if (task.getResult().getSignInMethods().size() == 1) {
                                                            Log.d(TAG, "onComplete: it means email is already in use");
                                                            Toast.makeText(getActivity(), "That email already exists", Toast.LENGTH_SHORT).show();
                                                        }
    //
    //                                                ***********************email is available, so update it*************************************
                                                        else {
                                                            Log.d(TAG, "onComplete: that email is available");
//                                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                            mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Log.d(TAG, "User email address updated.");
                                                                                mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                                Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }catch (NullPointerException e){
                                                        Log.d(TAG, "onComplete: " + e.getMessage());
                                                    }
                                                }
                                        }
                                    });

                        }else{
                            Log.d(TAG, "User re-authentication failed.");
                        }
                    }
                });
    }
}
