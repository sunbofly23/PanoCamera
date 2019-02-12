package com.fx.android.watercamerademo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhujg on 2018/3/14.
 */

public class ViewUtils {


    private static final String[] SCAN_TYPES= { "image/jpeg" };

    public interface OnTakePicture{
    	public void takePicture(Bitmap bitmap, String imagePath);
    }

    //普通外勤
    public static  void  drawToJieTu(Context context, TextureView tv, String path , int degrees, List<WaterMaskVO> list, final OnTakePicture mOnTakePicture){
        File photo=getPhotoPath(path);
        Bitmap texturebitmap = tv.getBitmap();   //拿到TextureView的宽高，是根据相机分辨率比率 得到的

        if (photo.exists()) {
            photo.delete();
        }

        texturebitmap=adjustPhotoRotation(texturebitmap,degrees);

        //截图----画水印
        texturebitmap = WaterMaskVO.drawWaterToBitMap1(context,list, texturebitmap, 15, Color.WHITE, 25,degrees);


        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(photo);

            texturebitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

            fos.flush();
            fos.close();

            Log.i("path","save success  ,file path :" + photo.getAbsolutePath());

            mOnTakePicture.takePicture(texturebitmap,photo.getAbsolutePath());

            if(!texturebitmap.isRecycled()){
                texturebitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static  File getPhotoPath(String path) {
        File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        dir.mkdirs();

        return(new File(dir, path));
    }

    public static String getPhotoFilename(int count) {
        return(new StringBuffer().append("Photo_").append(count).append(".jpg").toString());
    }



    public static Bitmap adjustPhotoRotation(Bitmap origin, final float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix,
                false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }
}

