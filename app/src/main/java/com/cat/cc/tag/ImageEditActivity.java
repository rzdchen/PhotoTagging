package com.cat.cc.tag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cat.cc.taglibrary.view.ImageDragRectLayout;

/**
 * Created by KF on 2017/10/27.
 */

public class ImageEditActivity extends AppCompatActivity {
    private String mImgPath = "http://c.hiphotos.baidu.com/image/pic/item/fcfaaf51f3deb48faed9f20cfa1f3a292cf578ab.jpg";
    private ImageDragRectLayout mImageIdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_drag_rect);
        getSupportActionBar().hide();
        mImageIdr = (ImageDragRectLayout) findViewById(R.id.idr_image);
        initData();
    }

    public void addIcon(View v) {
        mImageIdr.addMiddleIcon();
    }

    public void saveImage(View v) {
//        mImageIdr.savePhotoView(mImgPath);
        //如果是网络图片需要先把图片下载到本地
    }

    private void initData() {
        mImageIdr.setImage(mImgPath);
    }

}
