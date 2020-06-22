package com.geocrat.instagramclone.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class LogOut extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;

    private ProgressBar mProgressBar;
    private TextView tvSignOut;
    Button btnConfirmSignOut;

    public LogOut() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_out, container, false);
        mProgressBar = view.findViewById(R.id.progressBar);
        tvSignOut = view.findViewById(R.id.tvSigningOut);
        btnConfirmSignOut = view.findViewById(R.id.btnConfirmSignOut);
        mProgressBar.setVisibility(View.GONE);
        tvSignOut.setVisibility(View.GONE);

        setUpFirebaseAuth();
        Log.d(TAG, "onCreateView: navigating back to login screen");

        btnConfirmSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to sign out");
                mAuth.signOut();
                mProgressBar.setVisibility(View.VISIBLE);
                tvSignOut.setVisibility(View.VISIBLE);

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                //clearing the backstack of the activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return view;
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
