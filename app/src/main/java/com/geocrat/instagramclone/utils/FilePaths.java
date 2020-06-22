package com.geocrat.instagramclone.utils;

import android.os.Environment;

public class FilePaths {
    //storage/emulated/0
    public String root_directory = Environment.getExternalStorageDirectory().getPath();
    public String CAMERA = root_directory + "/DCIM/Camera";
    public String PICTURES = root_directory + "/Pictures";
    public String WHATSAPP_WALLPAPER = root_directory + "/WhatsApp/Media/WallPaper";
    public String WHATSAPP_IMAGES = root_directory + "/WhatsApp/Media/WhatsApp Images";
    public String WHATSAPP_PROFILE_PHOTOS = root_directory + "/WhatsApp/Media/WhatsApp Profile Photos";
    public String WHATSAPP_STICKERS = root_directory + "/WhatsApp/Media/WhatsApp Stickers";
    public String WHATSAPP_ANIMATED_GIF = root_directory + "/WhatsApp/Media/WhatsApp Animated Gifs";


    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
