package com.geocrat.instagramclone.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {
    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imageUrl){
        File imageFile = new File(imageUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try{
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }catch (FileNotFoundException e){
            Log.d(TAG, "getBitmap: FileNotFoundException " + e.getMessage());
        }finally {
            try{
                fis.close();
            }catch (IOException e){
                Log.d(TAG, "getBitmap: IOException " + e.getMessage());
            }
        }
        return bitmap;
    }

    //when we have a byte array, we have a choice to decide its quality indicated by its %
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}

















