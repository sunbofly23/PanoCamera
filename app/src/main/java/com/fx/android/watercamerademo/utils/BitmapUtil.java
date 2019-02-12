package com.fx.android.watercamerademo.utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sreay on 14-10-24.
 */
public class BitmapUtil {
    /**
     * 从exif信息获取图片旋转角度
     *
     * @param path
     * @return
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
     * 对图片进行压缩选择处理
     *
     * @param picPath
     * @return
     */
    public static Bitmap compressRotateBitmap(String picPath) {
        Bitmap bitmap = null;
        int degree = readPictureDegree(picPath);
        if (degree == 90) {
            bitmap = featBitmapToSuitable(picPath, 500, 1.8f);
            bitmap = rotate(bitmap, 90);
        } else {
            bitmap = featBitmapToSuitable(picPath, 500, 1.8f);
        }
        return bitmap;
    }

    /**
     * 转换bitmap为字节数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        byte[] image = out.toByteArray();
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;

    }

    /**
     * 获取合适尺寸的图片 图片的长或高中较大的值要 < suitableSize*factor
     *
     * @param path
     * @param suitableSize
     * @return
     */
    public static Bitmap featBitmapToSuitable(String path, int suitableSize,
                                              float factor) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            int bitmap_w = options.outWidth;
            int bitmap_h = options.outHeight;
            int max_edge = bitmap_w > bitmap_h ? bitmap_w : bitmap_h;
            while (max_edge / (float) suitableSize > factor) {
                options.inSampleSize <<= 1;
                max_edge >>= 1;
            }
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
        }
        return bitmap;
    }

    public static Bitmap featBitmap(String path, int width) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            int bitmap_w = options.outWidth;
            while (bitmap_w / (float) width > 2) {
                options.inSampleSize <<= 1;
                bitmap_w >>= 1;
            }
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(String path, int maxSideLen) {
        if (null == path) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(options.outWidth / maxSideLen, options.outHeight / maxSideLen);
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap != bitmap) {
                bitmap.recycle();
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
        }
        return null;
    }

    /**
     * 不压缩的加载图片
     *
     * @param path
     * @return
     */
    public static Bitmap loadBitmap(String path) {
        if (null == path) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //不对图进行压缩
        options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            return bitmap;
        } catch (OutOfMemoryError e) {
        }
        return null;
    }

    //资源文件中加载
    public static Bitmap loadFromAssets(Activity activity, String name, int sampleSize, Bitmap.Config config) {
        AssetManager asm = activity.getAssets();
        try {
            InputStream is = asm.open(name);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = config;
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            } catch (OutOfMemoryError e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //bet数组到bitmap图片
    public static Bitmap decodeByteArrayUnthrow(byte[] data, BitmapFactory.Options opts) {
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (Throwable e) {
        }

        return null;
    }

    public static Bitmap rotateAndScale(Bitmap b, int degrees, float maxSideLen) {

        return rotateAndScale(b, degrees, maxSideLen, true);
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    private static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }


    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (null != b2 && b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static Bitmap rotateNotRecycleOriginal(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees);
            try {
                return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static Bitmap rotateAndScale(Bitmap b, int degrees, float maxSideLen, boolean recycle) {
        if (null == b || degrees == 0 && b.getWidth() <= maxSideLen + 10 && b.getHeight() <= maxSideLen + 10) {
            return b;
        }

        Matrix m = new Matrix();
        if (degrees != 0) {
            m.setRotate(degrees);
        }

        // float scale = Math.min(maxSideLen / b.getWidth(), maxSideLen / b.getHeight());
//        if (scale < 1) {
//            m.postScale(scale, scale);
//        }

        try {
            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
            if (null != b2 && b != b2) {
                if (recycle) {
                    b.recycle();
                }
                b = b2;
            }
        } catch (OutOfMemoryError e) {
        }

        return b;
    }

    public static boolean saveBitmap2file(Bitmap bmp, File file, Bitmap.CompressFormat format, int quality) {
        if (file.isFile())
            file.delete();
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (Exception e) {
            return false;
        }

        return bmp.compress(format, quality, stream);
    }

    public static boolean saveNewBitmap2file(Bitmap bmp, String path, Bitmap.CompressFormat format, int quality) {
        File file = new File(path);
//        if (file.isFile())
//            file.delete();
//        if (!file.exists()) {
//            file.mkdirs();
//        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (Exception e) {
            return false;
        }

        return bmp.compress(format, quality, stream);
    }

    /**
     * 水印
     *
     * @param
     * @return
     */
    private float RATIO = 0;
    private float OFFSET_LEFT = 0;
    private float OFFSET_TOP = 0;
    public int TEXT_SIZE = 0;

    public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark, String title) {
        if (src == null) {
            return null;
        }

        int screenWidth = FSScreen.getScreenWidth();
        int screenHeight = FSScreen.getScreenHeight() - FSScreen.getStatusBarHeight();
        float ratioWidth = (float) screenWidth / 480;
        float ratioHeight = (float) screenHeight / 800;
        float RATIO = Math.min(ratioWidth, ratioHeight);
        if (ratioWidth != ratioHeight) {
            if (RATIO == ratioWidth) {
                float OFFSET_LEFT = 0;
                float OFFSET_TOP = Math.round((screenHeight - 800 * RATIO) / 2);
            } else {
                float OFFSET_LEFT = Math.round((screenWidth - 480 * RATIO) / 2);
                float OFFSET_TOP = 0;
            }
        }
        int TEXT_SIZE = Math.round(25 * RATIO);

        int w = src.getWidth();
        int h = src.getHeight();

        //        // create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图,Bitmap.Config.ARGB_8888
        Canvas cv = new Canvas(newb);
        //        // draw src into
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        //        // draw watermark into
        if (null != watermark) {
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
            cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
        }
        // 开始加入文字
        if (null != title) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(TEXT_SIZE);
            String familyName ="宋体";
            Typeface typeface = Typeface.create(familyName, Typeface.BOLD_ITALIC);
            textPaint.setTypeface(typeface);
            cv.drawText(title, FSScreen.getScreenWidth() - w, TEXT_SIZE, textPaint);
        }
        // save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        // store
        cv.restore();// 存储
        return newb;
    }

    public static Bitmap convertViewToBitmap(View view) {
//        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 水印，
     *
     * @param
     * @return
     */

    public static Bitmap newBitmapToWatermark(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int screenWidth = FSScreen.getScreenWidth();
        int screenHeight = FSScreen.getScreenHeight();
        int w = src.getWidth();
        int h = src.getHeight();
        Log.i("图片尺寸，宽", w + "--高" + h);
        //横着 宽: 1920--高1080
        //竖着 宽: 1080--高1920
//      create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图,Bitmap.Config.ARGB_8888
        Canvas cv = new Canvas(newb);
//      draw src into
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        if (null != watermark) {
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
//            watermark = watermark.copy(Bitmap.Config.ARGB_8888, true);
//            Canvas c = new Canvas(watermark);
//            Paint p1 = new Paint();
//            int removeColor = 0;//要去除的背景色; // store this color's int for later use
//            p1.setAlpha(0);
//            p1.setXfermode(new AvoidXfermode(removeColor, 0, AvoidXfermode.Mode.TARGET));
//            c.drawPaint(p1);
            /**下面是绘制渐变色背景*/
//            Paint p = new Paint();
            float i = w / (float) ww;
            int result = (int) (wh * i);
            watermark = cropPhotoImage(src, watermark, result);
            cv.drawBitmap(watermark, 0, h - watermark.getHeight(), null);// 在src的左下角画入水印
// 不用再画背景色了，由于View本身就带有渐变背景色
//            //CLAMP夹紧   REPEAT重复  MIRROR镜像
//            LinearGradient lg = new LinearGradient(0, 0, 0, watermark.getHeight(), Color.parseColor("#00000000"), Color.parseColor("#99000000"), Shader.TileMode.CLAMP);//00000000    99000000     00ff0000   990000ff
//            p.setShader(lg);
//            cv.drawRect(0, h - watermark.getHeight(), watermark.getWidth(), h, p);

        }
        return newb;
    }

    /**
     * 根据拍照的图片来剪裁
     */
    public static Bitmap cropPhotoImage(Bitmap src, Bitmap bmp, int i) {
        //将图片压缩至指定尺寸,可以在外面传进来
        //将传进来的参数进行切割

        float scale = (float) bmp.getHeight() / bmp.getWidth();

        try {
            Bitmap b4 = Bitmap.createScaledBitmap(bmp, src.getWidth(), i, false);
            if (null != b4 && bmp != b4) {
                bmp.recycle();
                bmp = b4;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bmp;
    }

    // 缩放图片
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public static Bitmap drawableToBitmapByBD(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }

    /**
     * 翻转相机
     *
     * @param bmp
     * @return
     */
    public static Bitmap convertBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1); // 镜像水平翻转
        Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        return convertBmp;
    }

    /**
     * Bitmap 转 Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


}
