package com.aravamsinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by sivaprakash on 12/21/2017.
 */

@SuppressLint("AppCompatCustomView")
public class FolderImageView extends ImageView {
    public FolderImageView(Context context) {
        super(context);
    }

    public FolderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        System.out.println(widthMeasureSpec+"ddddddddddddddddddd: "+getMeasuredWidth());
        setMeasuredDimension(getMeasuredWidth(), (getMeasuredWidth()/2)+100); //Snap to width

    }
}
