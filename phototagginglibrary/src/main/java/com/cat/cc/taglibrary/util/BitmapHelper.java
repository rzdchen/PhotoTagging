package com.cat.cc.taglibrary.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wang on 2015/9/9.
 * 图片处理帮助类
 */
public class BitmapHelper {
    private static final String TAG = BitmapHelper.class.getSimpleName();


    public static Drawable genCircleDrawable(int fillColor, int radius) {
//        int fillColor = Color.parseColor("#DFDFE0");//内部填充颜色
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(fillColor);
        gd.setShape(GradientDrawable.OVAL);
        gd.setSize(radius, radius);

        return gd;
    }

    public static Drawable genCircleDrawable(String fillColor, int radius) {
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(readIntColor(fillColor));
        gd.setShape(GradientDrawable.OVAL);
        gd.setSize(radius, radius);
        return gd;
    }

    public static int readIntColor(String color) {
        if(TextUtils.isEmpty(color)) {
            return Color.BLUE;
        }
        if(!color.startsWith("#")) {
            return Color.parseColor("#" + color);
        }

        return Color.parseColor(color);
    }

    public static Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        Bitmap bMapRotate = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                opt.inTempStorage = new byte[1024 * 1024 * 10];
                long length = file.length();
                Log.d(TAG, "file.length() = " + length);
                if (length / (1024 * 1024) > 4) {
//                    opt.inSampleSize = 16;
                    opt.inSampleSize = 8;
                    Log.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
                } else if (length / (1024 * 1024) >= 1) {
//                    opt.inSampleSize = 8;
                    opt.inSampleSize = 4;
                    Log.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
                } else if (length / (1024 * 512) >= 1) {
//                    opt.inSampleSize = 4;
                    opt.inSampleSize = 2;
                    Log.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
                } else if (length / (1024 * 256) >= 1) {
//                    opt.inSampleSize = 2;
                    opt.inSampleSize = 1;
                    Log.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
                } else {
                    opt.inSampleSize = 1;
                    Log.d(TAG, "opt.inSampleSize = " + opt.inSampleSize);
                }
                bitmap = BitmapFactory.decodeFile(pathString, opt).copy(Bitmap.Config.ARGB_8888, true);
                Log.i(TAG, "图片旋转度数：" + readPictureDegree(pathString));
                // ///////////
                // if(pathString.contains("floor/imgs")){
                // YLog.i("fangdd", "处理旋转:"+isGetImage);
                int orientation = readPictureDegree(pathString);
                /*
                 * if(bitmap.getHeight() < bitmap.getWidth()){ orientation = 90;
				 * } else { orientation = 0; }
				 */
                if (orientation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    bMapRotate = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                    bitmap.getHeight(), matrix, true);
                } else {
                    bMapRotate = Bitmap.createScaledBitmap(bitmap,
                            bitmap.getWidth(), bitmap.getHeight(), true);
                }
                // }
                // //////////////
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bMapRotate != null) {
            return bMapRotate;
        }
        return bitmap;
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 选择图片
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    // 压缩图片大小
    public static Bitmap compressBitmapBySize(String srcPath, float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
//        int be = 1;
//        if (w > h && w > width) {
//            be = (int) (newOpts.outWidth / width);
//        } else if (w < h && h > height) {
//            be = (int) (newOpts.outHeight / height);
//        }
//        if (be <= 0)
//            be = 1;
//        newOpts.inSampleSize = be;//设置采样率
        newOpts.inSampleSize = (int) ((w / width + h/ height) / 2);//设置采样率
        Log.d(TAG, "compress sample size is " + newOpts.inSampleSize);
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//当系统内存不够时候图片自动被回收

//        return BitmapFactory.decodeFile(srcPath, newOpts);
        try {
            return BitmapFactory.decodeStream(new FileInputStream(srcPath), null, newOpts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 压缩图片大小
    public static Bitmap compressBitmapBySizeNew(String srcPath, float width, float height) {
        FileInputStream f = null;
        FileDescriptor fd = null;
        try {
            f = new FileInputStream(srcPath);
            fd = f.getFD();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fd == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = (int) ((options.outWidth / width + options.outHeight/ height) / 2);//设置采样率
        Log.d(TAG, "compress sample size is " + options.inSampleSize);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(f, null, options);
    }

    // 压缩图片质量并保持到指定文件 压缩到1M左右
    public static void compressBitmapToFile(Bitmap bmp, File file){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 50; // 从60开始,
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if(options > 10) {
                options -= 10;
            } else {
                options -= 2;
            }
            Log.d(TAG, "options="+options);
            if(options <= 2) {
                break;
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File uri2File(Context context, final Uri uri) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri,
                    new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data == null ? null : new File(data);
    }

    /**
     * 将base64转换成bitmap图片
     * 　　 @param string base64字符串
     * 　　 @return bitmap
     *
     */
    public static Bitmap stringtoBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /**
     * 将bitmap图片转换成base64
     * 　　 @param bit 图片
     * 　　 @return String  base64
     *
     */
    public static String bitmap2StrByBase64(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }




}
