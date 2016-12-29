package com.maleant.pickerlib.libcore.util;

import android.app.Activity;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;

/**
 * Created by maleant on 2016/12/27.
 */

public class FileUtils {
    /**
     * 判断SDCard是否可用
     */
    public static boolean existSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回拍照后照片存储目录
     * @return
     */
    public static File getTakePhotoDir() {
        File takePhotoFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/");
        if (! takePhotoFile.exists()) {
            takePhotoFile.mkdirs();
        }
        return takePhotoFile;
    }



    /**
     * 获取手机大小（分辨率）
     *
     * @param activity
     * @return
     */
    public static DisplayMetrics getScreenPix(Activity activity) {
        // DisplayMetrics 一个描述普通显示信息的结构，例如显示大小、密度、字体尺寸
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        // 获取手机窗口的Display 来初始化DisplayMetrics 对象
        // getManager()获取显示定制窗口的管理器。
        // 获取默认显示Display对象
        // 通过Display 对象的数据来初始化一个DisplayMetrics 对象
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displaysMetrics);
        return displaysMetrics;
    }
}
