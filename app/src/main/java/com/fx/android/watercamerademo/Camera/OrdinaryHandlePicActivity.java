package com.fx.android.watercamerademo.Camera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.fx.android.watercamerademo.R;
import com.fx.android.watercamerademo.utils.BitmapUtil;
import com.fx.android.watercamerademo.utils.FSScreen;
import com.fx.android.watercamerademo.utils.WaterMaskVO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OrdinaryHandlePicActivity extends AppCompatActivity {

    private ImageView iv_cancle, iv_commit;
    private ImageView iv_pic;

    //截图拿值逻辑
    private final static String Screenshot_IMAGE_KEY = "screenshot_image_key";
    private final static String Screenshot_IMAGE_GRAVITY="screenshot_image_gravity";
    //拍照拿值逻辑
    private final static String CAMERA_IMAGE_KEY = "camera_image_key";
    private final static String CAMERA_IMAGE_WATERMASK ="camera_image_waterlist";
    private final static String PHONE_SCREEN_ORIENTATION ="phone_screen_orientation";
    private final static String CAMERA_IMAGE_FRONTANDBACK="camera_device_cameraid";

    private static final String kPhotoPath = "path";

    private int orientation, cameraid;
    //相机的
    private Bitmap mBitmap_Camera=null;
    public String mPath_Camera;
    //截图的
    public String mPath_Screenshot;
    private Bitmap mBitmap_Screenshot;

    private String mGravity_Screenshot;

    private List<WaterMaskVO> mWaterMaskVOS=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ordinary_handle_pic);
        initView();
        initEvent();
    }
    @SuppressLint("NewApi")
    private void initView() {
        iv_cancle = (ImageView) findViewById(R.id.iv_cancle);
        iv_pic  = (ImageView) findViewById(R.id.iv_pic);
        iv_commit = (ImageView) findViewById(R.id.iv_commit);

        //普通外勤-截图-传递的信息
        mPath_Screenshot = getIntent().getStringExtra(Screenshot_IMAGE_KEY);
        mGravity_Screenshot=getIntent().getStringExtra(Screenshot_IMAGE_GRAVITY);

        //普通外勤-拍照-传递的信息
        mWaterMaskVOS=(List<WaterMaskVO>) getIntent().getSerializableExtra(CAMERA_IMAGE_WATERMASK);
        orientation = getIntent().getIntExtra(PHONE_SCREEN_ORIENTATION, -1);
        mPath_Camera = getIntent().getStringExtra(CAMERA_IMAGE_KEY);
        cameraid = getIntent().getIntExtra(CAMERA_IMAGE_FRONTANDBACK, -1);

        //截图
        if (mPath_Screenshot != null) {
            //竖直
            if((orientation==0||orientation==180)&& !TextUtils.isEmpty(mGravity_Screenshot)){
                iv_pic.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP));
            }//水平不改变imageview的Gravity
            mBitmap_Screenshot = BitmapFactory.decodeFile(mPath_Screenshot);
            iv_pic.setImageBitmap(mBitmap_Screenshot);
        }
        //相机
        if (mPath_Camera != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(mPath_Camera);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //获取到原图
            mBitmap_Camera = BitmapFactory.decodeStream(fis);

            Log.i("mBitmap_Camera1",mBitmap_Camera.getWidth()+","+mBitmap_Camera.getHeight());
            Log.i("mBitmap_Camera0", FSScreen.getScreenWidth(this)+","+FSScreen.getScreenHeight(this));


            //根据手机屏幕方向，顺时针转90,调整位图方向
            mBitmap_Camera = BitmapUtil.rotateAndScale(mBitmap_Camera, orientation + 90, 0, true);

            if (cameraid == 1)//如果是前置，需要水平翻转一下，后置不需要
            {
                mBitmap_Camera = BitmapUtil.convertBmp(mBitmap_Camera);
            }

            Log.i("mBitmap_Camera2",mBitmap_Camera.getWidth()+","+mBitmap_Camera.getHeight());
            int toScreenWRatio = 0;//适配比率
            int AdapterW = 0;//适配之后的宽

            //根据屏幕宽去缩放bitmap,防止水印太大或者太小
            if(orientation==0||orientation==180)//竖直
            {
                 AdapterW = FSScreen.getScreenWidth(this)>mBitmap_Camera.getWidth()?FSScreen.getScreenWidth(this):mBitmap_Camera.getWidth();
                toScreenWRatio = Math.round(AdapterW / mBitmap_Camera.getWidth());
                Log.i("mBitmap_Camera3",toScreenWRatio+"");
                mBitmap_Camera = getZoomImage(mBitmap_Camera,FSScreen.getScreenWidth(this),toScreenWRatio*mBitmap_Camera.getHeight());
            }
            else if(orientation==90||orientation==270)//水平
            {
                 AdapterW = FSScreen.getScreenWidth(this)>mBitmap_Camera.getHeight()?FSScreen.getScreenWidth(this):mBitmap_Camera.getHeight();
                 toScreenWRatio = Math.round(AdapterW / mBitmap_Camera.getHeight());
                 mBitmap_Camera = getZoomImage(mBitmap_Camera,toScreenWRatio*mBitmap_Camera.getWidth(),FSScreen.getScreenWidth(this));
            }

            //画水印
            if(mBitmap_Camera!=null) {
                mBitmap_Camera = WaterMaskVO.drawWaterToBitMap1(this, mWaterMaskVOS, mBitmap_Camera, 15, Color.WHITE, 25, orientation);
                iv_pic.setImageBitmap(mBitmap_Camera);
            }

        }
    }

    //缩放bitmap的宽和高
    public static Bitmap getZoomImage(Bitmap orgBitmap, double newWidth, double newHeight) {
        if (null == orgBitmap) {
            return null;
        }
        if (orgBitmap.isRecycled()) {
            return null;
        }
        if (newWidth <= 0 || newHeight <= 0) {
            return null;
        }

        // 获取图片的宽和高
        float width = orgBitmap.getWidth();
        float height = orgBitmap.getHeight();
        // 创建操作图片的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(orgBitmap, 0, 0, (int) width, (int) height, matrix, true);
        return bitmap;
    }



    private void initEvent() {
        iv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPath_Screenshot != null)
                    deletefile(mPath_Screenshot);
                Intent it = new Intent();
                setResult(RESULT_CANCELED, it);
                finish();
            }
        });
        iv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPath_Screenshot != null) {
                    //因为截图方法里 已经保存了。 这里没必要保存，点击完成默认保存，点击返回删除路径
                    //跳转到下个页面
                    Intent it = new Intent();
                    it.putExtra(kPhotoPath, mPath_Screenshot);//图片的路径
                    setResult(RESULT_OK, it);
                    finish();
                } else   //拍照
                    saveBitmap(mBitmap_Camera);
            }
        });

    }

    //保存处理后的图并且回传
    public void saveBitmap(Bitmap bitmap) {
        File file = new File(mPath_Camera);
        Boolean is = false;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            is = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (is) {
                Intent it = new Intent();
                it.putExtra(kPhotoPath,file.getAbsolutePath());//图片的路径
                setResult(RESULT_OK, it);
                finish();
            } else
                Toast.makeText(OrdinaryHandlePicActivity.this, "失败", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除已存储的文件
     */
    public static void deletefile(String fileName) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File("", fileName);
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
