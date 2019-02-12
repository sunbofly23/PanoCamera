package com.fx.android.watercamerademo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;


import com.fx.android.watercamerademo.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujg on 2018/7/20.
 */

public class WaterMaskVO implements Serializable {
    /**
     * 水印的图标
     */
    public int icon;
    /**
     * 文本
     */
    public String text;

    public WaterMaskVO(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public static List<WaterMaskVO> getOutdoorWaterMask() {
        ArrayList<WaterMaskVO> list = new ArrayList<>();
        list.add(new WaterMaskVO(R.drawable.ow_icn_camera_owner, "姓名:XXX"/* ignore i18n */));
        list.add(new WaterMaskVO(R.drawable.ow_icn_camera_time, "时间:2018-07-20 10:52"/* ignore i18n */));
        list.add(new WaterMaskVO(R.drawable.ow_icn_camera_location, "地址:北京市海淀区某个大厦嘿嘿嘿"/* ignore i18n */));
        return list;
    }


    public static Bitmap drawWaterToBitMap1(Context context, List<WaterMaskVO> list, Bitmap bitmap,
                                            int size, int color, int paddingLeft, int degrees) {

        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }

        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        Log.i("Canvas", canvas.getWidth() + "," + canvas.getHeight());

        paddingLeft = dp2px(context, paddingLeft);//50

        int paddingTop = 0;

        //设置误差值10，防止太靠下。
        int error = 10;

        if (degrees == 0 || degrees == 180)
            paddingTop = bitmap.getHeight() - FSScreen.getStatusBarHeight(context) - error;
        else if (degrees == 90 || degrees == 270)
            paddingTop = bitmap.getHeight() - error;

        int textSize = dp2px(context, size);

        Log.i("Canvas_textSize", textSize + "");

        GradientDrawable gd = (GradientDrawable) context.getResources().getDrawable(R.drawable.watermark_bg_shape);
        gd.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        gd.draw(canvas);

        //绘制的时候是从下往上，所以list倒叙读取
        for (int i = list.size() - 1; i >= 0; i--) {
            WaterMaskVO wo = list.get(i);
            TextPaint paint = new TextPaint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(textSize);
            Rect bounds = new Rect();
            //绘制文本
            paint.getTextBounds(wo.text, 0, wo.text.length(), bounds);

            paint.setDither(true); // 防抖动
            paint.setFilterBitmap(true);// 抗锯齿

            //换行
            StaticLayout layout = new StaticLayout(wo.text, paint, bitmap.getWidth()-100, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);

            int lineCount = layout.getLineCount();
            paddingTop = paddingTop - ((lineCount) * 50);
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), wo.icon);
            bmp = getbitmap(bmp, textSize, textSize);
            canvas.drawBitmap(bmp, 10, paddingTop + 5, null);
            canvas.save();

            canvas.translate(paddingLeft, paddingTop);
            layout.draw(canvas);
            // 存储
            canvas.restore();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float density = dm.density;
            //水印行间距
            paddingTop = paddingTop - 10 * (int) density;
        }

        return bitmap;
    }

    private static Bitmap getbitmap(Bitmap bitmap, int nheight, int nwidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = (float) nwidth / width;
        float scaleHeight = (float) nheight / width;

        // 取得想要缩放的matrix參數
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的圖片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}