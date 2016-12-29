package com.maleant.pickerlib.libcore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.maleant.pickerlib.libcore.base.DefalutImageLoader;
import com.maleant.pickerlib.libcore.base.HImageLoader;
import com.yalantis.ucrop.UCrop;

/**
 * weibo gallery
 * Created by maleant on 16/9/2.
 */
public class HGallery {

    private static HImageLoader sImageLoader;

    public static HImageLoader getImageLoader(Context context) {
        if (sImageLoader == null) {
            synchronized (HGallery.class) {
                if (sImageLoader == null) {
                    sImageLoader = new DefalutImageLoader();
                }
            }
        }
        return sImageLoader;
    }

    public static void init(Context context, HImageLoader imageLoader) {
        sImageLoader = imageLoader;
        init(context);
    }

    public static void init(Context context) {
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GridImageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startForResult(Activity context, int flag) {
        Intent intent = new Intent(context, GridImageActivity.class);
        context.startActivityForResult(intent, flag);
    }
}
