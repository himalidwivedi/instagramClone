package com.geocrat.instagramclone.utils;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {

    /*
    Search a directory and return a list of all directories contained inside
     */
    public static ArrayList<String> getDirectoryPath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file  = new File(directory);
        File []listFiles = file.listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isDirectory()) {
                    pathArray.add(listFiles[i].getAbsolutePath());
                }
            }
        }
        return pathArray;
    }

    /*
   Search a directory and return a list of all files contained inside
    */
    public static ArrayList<String> getFilePath(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file  = new File(directory);
        File []listFiles = file.listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile()) {
                    pathArray.add(listFiles[i].getAbsolutePath());
                }
            }
        }
        return pathArray;
    }
}
