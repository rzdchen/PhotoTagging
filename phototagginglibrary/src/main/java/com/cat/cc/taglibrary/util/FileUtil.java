package com.cat.cc.taglibrary.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    public static final String TAG = FileUtil.class.getSimpleName();

    public static void saveBitmap(String fullName, Bitmap bitmap) {
        File f = new File(fullName);
        try {
            if (!f.exists()) {
                boolean success = f.createNewFile();
                if (!success) {
                    Log.e(TAG, "saveBitmap create file failed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, fOut);
        try {
            if (fOut != null) {
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapNoCompress(String fullName, Bitmap bitmap) {
        File f = new File(fullName);
        try {
            if (!f.exists()) {
                boolean success = f.createNewFile();
                if (!success) {
                    Log.e(TAG, "saveBitmap create file failed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            if (fOut != null) {
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     */
    public static void copyFile(String oldPath, String newPath) throws IOException {
        int byteRead;
        File oldFile = new File(oldPath);
        if (oldFile.exists()) { //文件存在时
            InputStream inStream = new FileInputStream(oldPath); //读入原文件
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            while ((byteRead = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
                fs.flush();
            }
            inStream.close();
            fs.close();
        }
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File cacheDir = context.getExternalCacheDir();

            if(cacheDir == null) {
                cacheDir = new File("/Android/data/" + context.getPackageName() + "/cache/");
            }
            if(!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cachePath = cacheDir.getPath();
        } else {
            File cacheDir = context.getCacheDir();
            if(!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cachePath = cacheDir.getPath();
        }
        File file = new File(cachePath + File.separator + uniqueName);
        File dir = file.getParentFile();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return file;
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 检验SDcard状态
     *
     * @return boolean
     */
    public static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void clearDir(String dirName) {
        File dir = new File(dirName);
        if(dir == null || !dir.exists()) {
            return;
        }
        for(File file : dir.listFiles()) {
            if(file.exists()) {
                file.delete();
            }
        }
    }

    //往SD卡写入文件的方法
    public static void saveFile(String fullName, byte[] bytes) {
        //这里就不要用openFileOutput了,那个是往手机内存中写数据的
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(fullName);
            //将bytes写入到输出流中
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭输出流
                if(output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存文件文件到目录
     * @param context
     * @return  文件保存的目录
     */
    public static String setMkdir(Context context)
    {
        String filePath;
        if(checkSDCard())
        {
            filePath = Environment.getExternalStorageDirectory()+File.separator+"myfile";
        }else{
            filePath = context.getCacheDir().getAbsolutePath()+File.separator+"myfile";
        }
        File file = new File(filePath);
        if(!file.exists())
        {
            boolean b = file.mkdirs();
            Log.e("file", "文件不存在  创建文件    "+b);
        }else{
            Log.e("file", "文件存在");
        }
        return filePath;
    }

    public static String saveBitmapToTemp(String filePath, Bitmap mBitmap) {
        File f = new File(filePath);
        File dir = f.getParentFile();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        if(f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            Log.d("saveBitmapToTemp", "在保存图片时出错：" + e.toString());
            return null;
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 60, fOut);
        try {
            fOut.flush();
            fOut.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 本地图片地址
     * @param path 本地图片地址
     */
    public static String sdCard2Uri(String path) {
        // String imageUri = "file:///mnt/sdcard/image.png"; // from SD card
        return "file://" + path;
    }

    /**
     * assets文件夹图片地址
     *
     * @param path assets文件夹图片地址
     */
    public static String assets2Uri(String path) {
        // String imageUri = "assets://image.png"; // from assets
        return "assets://" + path;
    }

    /**
     * drawable文件夹下的图片
     *
     * @param drawableId drawable文件夹下的图片id
     */
    public static String drawable2Uri(int drawableId) {
        // String imageUri = "drawable://" + R.drawable.image; // from drawables
        // (only images, non-9patch)
//        ImageLoader.getInstance().displayImage("drawable://" + imageId,
//                imageView);
        return "drawable://" + drawableId;
    }

    /**
     * 从内容提提供者中抓取图片
     * @param uri 内容提提供者中图片地址
     */
    public static String content2Uri(String uri) {
        // String imageUri = "content://media/external/audio/albumart/13"; //
        // from content provider
        return "content://" + uri;
    }
}
