package com.cat.cc.taglibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cat.cc.taglibrary.util.BitmapHelper;
import com.cat.cc.taglibrary.util.FileUtil;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by cc on 2017/10/25.
 */

public class ImageDragRectLayout extends FrameLayout implements View.OnClickListener {
    private static final String TAG = ImageDragRectLayout.class.getSimpleName();

    public ImageDragRectLayout(@NonNull Context context) {
        this(context, null);
    }

    public ImageDragRectLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageDragRectLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    int imageViewWidth = -1;
    int imageViewHeight = -1;

    List<DragScaleView> iconList;
    //    float scale;
    float tempScale;

    PhotoView photoView;
    //    RectF rectF;
    RectF tempRectF;
    private boolean isCanAdd = true;
    private int iconColor = Color.RED;
    private Matrix photoViewMatrix;

    void initView(final Context context) {
        photoView = new PhotoView(context);
        LayoutParams layoutParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(photoView, layoutParams);

        photoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rf) {
                if (imageViewWidth == -1) {
                    imageViewWidth = photoView.getMeasuredWidth();
                    imageViewHeight = photoView.getMeasuredHeight();
                    Log.i(TAG, "imageViewWidth=" + imageViewWidth + ",rf.right - rf.left=" + (rf.right - rf.left));
                }
                tempRectF = rf;

                tempScale = (rf.right - rf.left) / imageViewWidth;

                if (iconList != null && iconList.size() > 0) {
                    for (DragScaleView icon : iconList) {
                        RectBean bean = (RectBean) icon.getTag();
                        float newX = bean.sx * (rf.right - rf.left);
                        float newY = bean.sy * (rf.bottom - rf.top);

                        //图标跟随图片放大
                        int iLeft = (int) (tempRectF.left + newX);
                        int iTop = (int) (tempRectF.top + newY);
                        int iRight = (int) (iLeft + bean.width * tempScale);
                        int iBottom = (int) (iTop + bean.height * tempScale);
                        icon.layout(iLeft, iTop, iRight, iBottom);
                        icon.invalidate();
                    }
                }

                Log.i(TAG, "tempRectF.left=" + rf.left + "tempRectF.top=" + rf.top);
            }
        });
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float v, float v1) {
                if (!isCanAdd) {
                    return;
                }
//                Toast.makeText(context, "v=" + v + "v1=" + v1, Toast.LENGTH_SHORT).show();
//                int id = 0;
//                if (iconList != null && iconList.size() > 0) {
//                    id = iconList.size();
//                }
//                scale = tempScale;
//                rectF = new RectF(tempRectF.left, tempRectF.top, tempRectF.right, tempRectF.bottom);
//                int width = (int) (200 / scale);
//                int height = (int) (200 / scale);
//                //使在中心点生成矩形
//                v = v - width / 2 / (rectF.right - rectF.left);
//                v1 = v1 - height / 2 / (rectF.bottom - rectF.top);
//                RectBean bean = new RectBean(id, v, v1, width, height);
//                addIcon(bean);
            }

        });
    }

    public void addMiddleIcon() {
        int id = 0;
        if (iconList != null && iconList.size() > 0) {
            id = iconList.size();
        }
        int width = (int) (200 / tempScale);
        int height = (int) (200 / tempScale);
        float v = 0.5f;
        float v1 = 0.5f;
        //使在中心点生成矩形
        v = v - width / 2 / (tempRectF.right - tempRectF.left);
        v1 = v1 - height / 2 / (tempRectF.bottom - tempRectF.top);
        RectBean bean = new RectBean(id, v, v1, width, height);
        addIcon(bean);
    }

    private void addIcon(final RectBean bean) {
        //记住此时photoView的Matrix
        if (photoViewMatrix == null) {
            photoViewMatrix = new Matrix();
        }
        photoView.getSuppMatrix(photoViewMatrix);

        if (iconList == null) {
            iconList = new ArrayList<>();
        }
        final DragScaleView icon = new DragScaleView(getContext());
        icon.setTag(bean);

        icon.setOnClickListener(this);
        icon.setOnMoveListener(new DragScaleView.OnMoveListener() {
            @Override
            public void onMoved(float l, float t) {
                bean.sy = (t - tempRectF.top) / (tempRectF.bottom - tempRectF.top);
                bean.sx = (l - tempRectF.left) / (tempRectF.right - tempRectF.left);
            }

            @Override
            public void onScale(int l, int t, int r, int b) {
                bean.sy = (t - tempRectF.top) / (tempRectF.bottom - tempRectF.top);
                bean.sx = (l - tempRectF.left) / (tempRectF.right - tempRectF.left);
                bean.width = r - l;
                bean.height = b - t;
            }

            @Override
            public void onActionUp() {
                //记住此时图片的大小
//                rectF = new RectF(tempRectF.left, tempRectF.top, tempRectF.right, tempRectF.bottom);
            }
        });
        icon.setColor(iconColor);
        addView(icon);
        iconList.add(icon);
    }


    @Override
    public void onClick(View v) {
        RectBean bean = (RectBean) v.getTag();
        Toast.makeText(getContext(), "pos : " + bean.id, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (photoViewMatrix != null) {
            photoView.setDisplayMatrix(photoViewMatrix);
        }
    }

    private class RectBean {
        int id;
        float sx;
        float sy;
        int width;
        int height;

        private RectBean(int id, float sx, float sy, int width, int height) {
            this.id = id;
            this.sx = sx;
            this.sy = sy;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * 设置图片
     *
     * @param url 网址或本地路径
     */
    public void setImage(String url) {
        Glide.with(getContext()).load(url).into(photoView);
    }

    public void setIconColor(@ColorInt int iconColor) {
        this.iconColor = iconColor;
    }

    /**
     * 设置是否可添加图标
     *
     * @param isCanAdd true 可以添加
     */
    public void setIsCanAdd(boolean isCanAdd) {
        this.isCanAdd = isCanAdd;
    }

    /**
     * 移除所有icon
     */
    public void removeAllIcon() {
        if (iconList != null && iconList.size() > 0) {
            for (DragScaleView icon : iconList) {
                removeView(icon);
            }
            iconList.clear();
        }
    }

    /**
     * 获取所有icon信息
     *
     * @return
     */
    public List<RectBean> getAllIconInfos() {
        List<RectBean> rectBeans = new ArrayList<>();
        if (iconList != null && iconList.size() > 0) {
            for (DragScaleView icon : iconList) {
                RectBean rectBean = (RectBean) icon.getTag();
                rectBeans.add(rectBean);
            }
        }
        return rectBeans;
    }

    /**
     * 在图片上绘制标记并保存
     *
     * @param imagePath 图片的地址
     * @return 新图片地址
     */
    public String savePhotoView(String imagePath) {
//        Bitmap bitmap = photoView.getDrawingCache();
        Bitmap bitmap = BitmapHelper.getDiskBitmap(imagePath);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        Log.i(TAG, "bw=" + bw + ",imageViewWidth=" + imageViewWidth);
        float scale = bw / (float) imageViewWidth;
        if (iconList != null && iconList.size() > 0) {
            for (DragScaleView icon : iconList) {
                RectBean rectBean = (RectBean) icon.getTag();
                RectF rectF = new RectF(bw * rectBean.sx, bh * rectBean.sy,
                        bw * rectBean.sx + rectBean.width * scale,
                        bh * rectBean.sy + rectBean.height * scale);
                canvas.drawRoundRect(rectF, 5f, 5f, paint);
            }
        }
        String fileName = UUID.randomUUID().toString() + ".png";
        File file = FileUtil.getDiskCacheDir(getContext(), fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "找不到文件");
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, fOut);
        Log.i(TAG, "保存成功");
        try {
            if (fOut != null) {
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeAllIcon();
        Glide.with(getContext()).load(file.getAbsolutePath()).into(photoView);
        return file.getAbsolutePath();
    }
}
