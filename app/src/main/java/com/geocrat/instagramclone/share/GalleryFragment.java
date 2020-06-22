package com.geocrat.instagramclone.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geocrat.instagramclone.AccountSettingActivity;
import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.profile.EditProfileFragment;
import com.geocrat.instagramclone.utils.FilePaths;
import com.geocrat.instagramclone.utils.FileSearch;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.geocrat.instagramclone.utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment";
    //constants
    private static final int NUM_GRID_COLUMNS = 3;

    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    //vars
    private String mAppend="file:/";
    private ArrayList<String> directories;
    private String mSelectedImage;
    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        
        gridView = view.findViewById(R.id.gridView);
        galleryImage = view.findViewById(R.id.galleryImageView);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        
        ImageView shareClose = view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing gallery fragment");
                getActivity().finish();
            }
        });
        
        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                if (isRootTask()){
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(intent);
                }else{
                    FirebaseMethods firebaseMethods = new FirebaseMethods(getActivity());
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0, mSelectedImage, null);
                    Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                }
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private boolean isRootTask(){
        if (((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }else{
            return false;
        }
    }

    private  void  init(){
        FilePaths filePaths = new FilePaths();
//        checking for other folders inside "/storage/emulator/0/pictures"
        if (FileSearch.getDirectoryPath(filePaths.PICTURES)!= null) {
            directories = FileSearch.getDirectoryPath(filePaths.PICTURES);
        }
        directories.add(filePaths.WHATSAPP_STICKERS);
        directories.add(filePaths.WHATSAPP_IMAGES);
        directories.add(filePaths.CAMERA);
        directories.add(filePaths.WHATSAPP_PROFILE_PHOTOS);
        directories.add(filePaths.WHATSAPP_WALLPAPER);

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i =0; i < directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index).replace("/","");
            directoryNames.add(string);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected : " + directories.get(position));
                //setUp our image grid for the directory classes
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupGridView(String selectedDirectory){
        Log.d(TAG, "setupGridView: directory choosen : " + selectedDirectory);
        final ArrayList<String> imgUrls = FileSearch.getFilePath(selectedDirectory);

        //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the GridImageAdapter to populate images in the grid view
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgUrls);
        gridView.setAdapter(adapter);

        //setting first image to be displayed when the activity fragment view is inflated
        if (imgUrls.size() != 0) {
            setImage(imgUrls.get(0), galleryImage, mAppend);
            mSelectedImage = imgUrls.get(0);
        }else{
            Toast.makeText(getActivity(), "No Image found", Toast.LENGTH_SHORT).show();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an image " + imgUrls.get(position));
                setImage(imgUrls.get(position), galleryImage, mAppend);
                mSelectedImage = imgUrls.get(position);
            }
        });
    }

    private void setImage(String imgUrl, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
