package com.geocrat.instagramclone.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.geocrat.instagramclone.R;

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";

    public interface OnConfirmPasswordListner{
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListner mOnConfirmPasswordListner;

    //vars
    TextView mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        Log.d(TAG, "onCreateView: started");

        mPassword = view.findViewById(R.id.confirmPassword);
        TextView cancelDialog = (TextView) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing a dialog");
                getDialog().dismiss();
            }
        });
        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: captured password and confirming");
                String password = mPassword.getText().toString();
                if (!password.equals("")){
                    mOnConfirmPasswordListner.onConfirmPassword(password);
                }else{
                    Toast.makeText(getActivity(), "you must enter a password", Toast.LENGTH_SHORT).show();
                }
                getDialog().cancel();
            }
        });
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnConfirmPasswordListner = (OnConfirmPasswordListner) getTargetFragment();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException " + e.getMessage());
        }
    }
}
