package com.cat.cc.taglibrary.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cat.cc.taglibrary.R;
import com.cat.cc.taglibrary.util.DensityUtil;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cc on 2017/10/25.
 */

public class ImageDotLayout extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = ImageDotLayout.class.getSimpleName();
    private List<ImageView> iconList;
    private PhotoView photoView;//背景图
    private RectF tempRectF;
    private OnIconClickListener onIconClickListener;
    private OnIconLongClickListener onIconLongClickListener;
    private OnLayoutReadyListener onLayoutReadyListener;
    private OnImageClickListener onImageClickListener;
    private Matrix photoViewMatrix;
    boolean firstLoadPhotoView = true;

    public ImageDotLayout(@NonNull Context context) {
        this(context, null);
    }

    public ImageDotLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageDotLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private Drawable mIconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_location);

    void initView(final Context context) {
        photoView = new PhotoView(context);
        LayoutParams layoutParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(photoView, layoutParams);
        photoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                if (iconList != null && iconList.size() > 0) {
                    for (ImageView icon : iconList) {
                        IconBean bean = (IconBean) icon.getTag();
                        float newX = bean.sx * (rectF.right - rectF.left);
                        float newY = bean.sy * (rectF.bottom - rectF.top);
                        //保持底部中心位置不变
                        icon.setX(rectF.left + newX - DensityUtil.dp2px(getContext(), 45) / 2);
                        icon.setY(rectF.top + newY - DensityUtil.dp2px(getContext(), 48));
                    }
                }
                tempRectF = rectF;
                //图片加载完成后才可以添加图标
                if (onLayoutReadyListener != null) {
                    onLayoutReadyListener.onLayoutReady();
                    //保证只执行一次
                    onLayoutReadyListener = null;
                }

            }
        });
        //实现OnPhotoTapListener接口，监听图片被点击的位置
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float v, float v1) {
                Log.i(TAG, "onPhotoTap");
                int id = 0;
                if (iconList != null && iconList.size() > 0) {
                    id = iconList.size();
                }
                IconBean bean = new IconBean(id, v, v1, mIconDrawable);
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(bean);
                }
            }
        });
    }

    public void setIconDrawable(Drawable drawable) {
        mIconDrawable = drawable;
    }

    public void addIcon(IconBean bean) {
        //记住此时photoView的Matrix
        if (photoViewMatrix == null) {
            photoViewMatrix = new Matrix();
        }
        photoView.getSuppMatrix(photoViewMatrix);
        if (iconList == null) {
            iconList = new ArrayList<>();
        }
        final ImageView icon = new ImageView(getContext());
        LayoutParams layoutParams = new LayoutParams(DensityUtil.dp2px(getContext(), 45), DensityUtil.dp2px(getContext(), 48));
        icon.setImageDrawable(bean.drawable == null ? mIconDrawable : bean.drawable);
        icon.setTag(bean);
        float newX = bean.sx * (tempRectF.left - tempRectF.right);
        float newY = bean.sy * (tempRectF.bottom - tempRectF.top);
        icon.setX(tempRectF.left + newX);
        icon.setY(tempRectF.top + newY);
        icon.setOnClickListener(this);
        icon.setOnLongClickListener(this);
        addView(icon, layoutParams);
        iconList.add(icon);
    }

    public void updateIconResource(ImageView icon, Drawable drawable) {
        icon.setImageDrawable(drawable);
    }

    public void addIcon(int id, float sx, float sy, Drawable drawable) {
        IconBean iconBean = new IconBean(id, sx, sy, drawable);
        addIcon(iconBean);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {
        if (onIconClickListener != null) {
            onIconClickListener.onIconClick(v);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (photoViewMatrix != null) {
            photoView.setDisplayMatrix(photoViewMatrix);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onIconLongClickListener != null) {
            onIconLongClickListener.onIconLongClick(v);
        }
        return true;
    }


    public static class IconBean {
        public int id;//标签的顺序，从0开始
        public float sx;//左边距比例
        public float sy;//上边距比例
        public Drawable drawable;//图标

        public IconBean(int id, float sx, float sy, Drawable drawable) {
            this.id = id;
            this.sx = sx;
            this.sy = sy;
            this.drawable = drawable;
        }
    }

    public interface OnIconClickListener {
        void onIconClick(View v);
    }

    public interface OnIconLongClickListener {
        void onIconLongClick(View v);
    }

    public interface OnImageClickListener {
        void onImageClick(IconBean bean);
    }

    public interface OnLayoutReadyListener {
        void onLayoutReady();
    }

    public void setOnIconClickListener(OnIconClickListener onIconClickListener) {
        this.onIconClickListener = onIconClickListener;
    }

    public void setOnIconLongClickListener(OnIconLongClickListener onIconLongClickListener) {
        this.onIconLongClickListener = onIconLongClickListener;
    }

    public void setOnLayoutReadyListener(OnLayoutReadyListener onLayoutReadyListener) {
        this.onLayoutReadyListener = onLayoutReadyListener;
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    /**
     * 设置图片
     *
     * @param url 网址或本地路径
     */
    public void setImage(String url) {
        firstLoadPhotoView = true;
        Glide.with(getContext()).load(url).into(photoView);
    }

    /**
     * 移除icon
     */
    public void removeIcon(ImageView icon) {
        removeView(icon);
    }

    /**
     * 移除所有icon
     */
    public void removeAllIcon() {
        if (iconList != null && iconList.size() > 0) {
            for (ImageView icon : iconList) {
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
    public List<IconBean> getAllIconInfos() {
        List<IconBean> rectBeans = new ArrayList<>();
        if (iconList != null && iconList.size() > 0) {
            for (ImageView icon : iconList) {
                IconBean rectBean = (IconBean) icon.getTag();
                rectBeans.add(rectBean);
            }
        }
        return rectBeans;
    }

    public void addIcons(List<IconBean> iconBeanList) {
        if (iconBeanList != null && iconBeanList.size() > 0) {
            for (IconBean bean : iconBeanList) {
                addIcon(bean);
            }
        }
    }
}
