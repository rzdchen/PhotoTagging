package com.cat.cc.tag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toImageDot(View v) {
        startActivity(new Intent(this, ImageDotActivity.class));
    }
    public void toImageEdit(View v) {
        startActivity(new Intent(this, ImageEditActivity.class));
    }

}
