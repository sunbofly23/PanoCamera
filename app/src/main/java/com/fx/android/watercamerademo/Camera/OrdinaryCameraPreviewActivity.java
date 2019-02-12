package com.fx.android.watercamerademo.Camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.fx.android.watercamerademo.R;
import com.fx.android.watercamerademo.utils.BitmapUtil;
import com.fx.android.watercamerademo.utils.CameraUtils;
import com.fx.android.watercamerademo.utils.ViewUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import static org.opencv.core.CvType.CV_32FC1;


public class OrdinaryCameraPreviewActivity extends AppCompatActivity
        implements View.OnClickListener {

    public static final String TAG = OrdinaryCameraPreviewActivity.class.getSimpleName();
    //相机Id -1不可用，0后置相机，1前置相机
    private int cameraId = -1;
    private int displayOrientation = -1;
    //相机是否预览
    private boolean isPreview = false;
    //闪光灯是否打开，默认关闭
    private Boolean isOpenFlash=false;
    //是否转换前后相机
    private boolean useFrontFacingCamera = false;
    //拍照咔嚓声
    int soundID = 0;
    //相机方向控制
    private int orientationTag;
    final int ORIGIN = 0;
    final int LEFT = 1;
    final int RIGHT = 2;
    final int TOP = 3;
    private int _rotation = 90;
    private int _rotationfront = -90;

    private OrdinaryCameraPreviewActivity.CaptureOrientationEventListener mCaptureOrientationEventListener;
    //相机框的监听
    private OrdinaryCameraPreviewActivity.MySurfaceTextureListener listener;

    // 屏幕的宽
    public static int ScreenW;
    // 屏幕的高
    public static int ScreenH;
    //屏幕实际高
    private static  int RealScreenH;
    //屏幕方向观察者
    CaptureSensorsObserver mCaptureSensorsObserver;

    public static final String kPhotoPath = "path";

    //Activity 给fragment传值
    private  int CameraModel=1;
    //相机
    private Camera mCamera;
    //大相机框
    private TextureView textureView;
    //预览框
    private SurfaceTexture mSurfaceTexture;

    //闪光灯
    private ImageView btn_openFlash;

    //取消按钮
    private TextView btn_cancle;
    //拍照按钮
    private ImageView btn_handle;
    //预览尺寸
    private Camera.Size mSize;
    //重拍返回得不getActivity解决方案
    private  float CameraRatio;
    private  float cameraH;  //相机高
    private  float cameraW;  //相机宽
    private ImageView iv;
    private RelativeLayout rv;
    private View vv_top, vv_bottom;
    private int  top_y,bottom_y;
    
    private TextView tv_X,tv_Y,tv_Count;
    //图片路径和水印


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //注意requestWindowFeature方法必须要放在setContentView方法的前面
        setContentView(R.layout.activity_ordinary_camera_preview);
        //保存相机状态，方便用户下次使用
        initScreenSize();
        initView();
        initEvent();
    }


    //获取屏幕的宽，高
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ScreenW = metrics.widthPixels;
        ScreenH = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        RealScreenH=metrics.heightPixels;
    }

    private void initView() {
        //大相机框
        textureView = (TextureView)findViewById(R.id.textureView);
        //闪光灯
        btn_openFlash = (ImageView)findViewById(R.id.openFlash);
        //取消按钮
        btn_cancle=(TextView)findViewById(R.id.btnCancel);
        //拍照按钮
        btn_handle=(ImageView)findViewById(R.id.handle);
        tv_X = (TextView) findViewById(R.id.tv1);
        tv_Y = (TextView) findViewById(R.id.tv3);
        tv_Count = (TextView)findViewById(R.id.tv4);
        iv = (ImageView)findViewById(R.id.iv);
        rv= (RelativeLayout)findViewById(R.id.rv);
        vv_top=(View)findViewById(R.id.vv_top);
        vv_bottom=(View)findViewById(R.id.vv_bottom);
        vv_top.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int []location=new int[2];
                vv_top.getLocationInWindow(location);
                top_y=location[1];//获取当前位置的纵坐标
                Log.i("sunbobob",top_y+"");
                vv_top.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        vv_bottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int []location=new int[2];
                vv_bottom.getLocationInWindow(location);
                bottom_y=location[1];//获取当前位置的纵坐标
                Log.i("sunbobob",bottom_y+"");
                vv_bottom.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }


    private  void initEvent(){
        //相机框的监听
        listener=new OrdinaryCameraPreviewActivity.MySurfaceTextureListener();
        textureView.setSurfaceTextureListener(listener);
        //屏幕旋转的监听
        mCaptureOrientationEventListener = new OrdinaryCameraPreviewActivity.CaptureOrientationEventListener(this);
        //使用观察者模式观察屏幕方向的改变
        mCaptureSensorsObserver = new CaptureSensorsObserver(this);
        //变焦监听
        btn_openFlash.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        btn_handle.setOnClickListener(this);
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                needFocuse();
                return false;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        if(mCaptureOrientationEventListener!=null){
            mCaptureOrientationEventListener.disable();
        }
        if(mCaptureSensorsObserver!=null){
            mCaptureSensorsObserver.stop();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        iniLoadOpenCV();
        if(mCaptureOrientationEventListener!=null){
            mCaptureOrientationEventListener.enable();
        }
        if(mCaptureSensorsObserver!=null){
            mCaptureSensorsObserver.start();
        }
    }


    private  void iniLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();
        if(success)
            Log.i("opencv","OpenCV Libs loaded success...");
        else
            Log.i("opencv","OpenCV Libs loaded error...");
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        initCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        stopCamera();
        if(mHanderThread!=null)
        mHanderThread.quit() ;
        if (mCaptureOrientationEventListener != null) {
            mCaptureOrientationEventListener.disable();
            mCaptureOrientationEventListener = null;
        }

        if (null != mCaptureSensorsObserver) {
            mCaptureSensorsObserver.setRefocuseListener(null);
            mCaptureSensorsObserver = null;
        }
    }

    //聚焦
    public void needFocuse() {
        try {
            if(mCamera!=null){
                mCamera.cancelAutoFocus();
                mCamera.autoFocus(focusCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    private boolean firstClick = false;

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.openFlash) {

                if (!isOpenFlash) {
                    isOpenFlash = true;
                    openFlash();
                } else {
                    isOpenFlash = false;
                    closeFlash();
                }
                btn_openFlash.setImageResource(isOpenFlash ? R.drawable.flashlight_on_icn
                        : R.drawable.flashlight_off_icn);

        } else if (i == R.id.btnCancel) {
            finish();
        } else if (i == R.id.handle) {
            if (!firstClick) {
                //testPhaseCorrelate();
                btn_handle.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ow_photodown));
                getPicFrame();
            } else {
                if (paths.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("IMAGEKEY",paths);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            firstClick = !firstClick;
        }
    }


    private ArrayList<String>paths = new ArrayList<>();
    private  Bitmap baseBitmap = null;
    private Bitmap currentBitmap = null;
    private Handler mHandler;
    private HandlerThread mHanderThread;
    private int xtran = 0;
    private int ytran = 0;
    private int frame = 0;
    private int count = 0;

    //从预览池里取帧
    private void getPicFrame() {
        if (isPreview && mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    count++;
                    if(count%15==0)
                    openThread(data,count);
                }
            });
        }
    }


    private  void openThread(byte[] data, final int count){
              //创建一个名字是calculate_offset的子线程
              mHanderThread = new HandlerThread("calculate_offset");
              //开启这个子线程
              mHanderThread.start();
              //在这个子线程里创建一个handle对象
               mHandler = new Handler(mHanderThread.getLooper()){
                   @Override//处理由主线程或者子线程传递来的消息
                   public void handleMessage(Message msg) {
                       super.handleMessage(msg);
                       if(msg.what==1){
                               byte[] data = msg.getData().getByteArray("data");
                               //byte[]转bitmap且缩放
                               currentBitmap = bytetoBitmap(data);
                               currentBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
                               //相机默认图像是横向的，需要旋转90°
                               currentBitmap = BitmapUtil.rotate(currentBitmap, 90);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       iv.setImageBitmap(currentBitmap);
                                   }
                               });
                               Log.i("getPicFrame", "宽:" + currentBitmap.getWidth() + ",高:" + currentBitmap.getHeight());
                               //第一次进来
                               if (baseBitmap == null) {
                                   Log.i("getPicFrame", "Count值:" + count);
                                   baseBitmap = currentBitmap;
                                   //保存第一帧
                                   frame++;
                                   saveBitmap(baseBitmap, count);
                               }
                               //每次进来把偏移量置为0
                               xtran = 0;
                               ytran = 0;

                               if (currentBitmap != baseBitmap) {
                                   org.opencv.core.Point point = calOffset(baseBitmap, currentBitmap);
                                   xtran = (int) point.x;
                                   ytran = (int) point.y;
                                   Log.i("getPicFrame", "X轴的偏移量:" + xtran + ",Y轴的偏移量:" + ytran);
                                   if (xtran > 40 && Math.abs(ytran) < 20) {
                                       //认为向右移动了，保存当前帧，基准线换为当前帧，
                                       // Y的绝对值<20认为上下无抖动
                                       saveBitmap(currentBitmap, count);
                                       frame++;
                                       runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               rv.scrollBy(-ScreenW / 16  , 0);
                                           }
                                       });
                                       //如果取得帧大于16张，为了拼接效率就不取帧了。
                                       if (frame == 16 && paths.size() > 0) {
                                           Intent intent = new Intent();
                                           intent.putExtra("IMAGEKEY", paths);
                                           setResult(RESULT_OK, intent);
                                           finish();
                                       }
                                       baseBitmap = currentBitmap;
                                   }
                                   else  if(Math.abs(ytran) >= 20){
                                       runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               Toast.makeText(OrdinaryCameraPreviewActivity.this,
                                                       "请保持手机水平缓慢移动",Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                       restartActivity();
                                   }
                                   else if(xtran<0){
                                       runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               Toast.makeText(OrdinaryCameraPreviewActivity.this,
                                                       "请保持向右一个方向移动",Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                       restartActivity();
                                   }
                               }
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       tv_X.setText(xtran + "");
                                       tv_Y.setText(ytran + "");
                                       tv_Count.setText(frame + "");
                                   }
                               });
                       }
                   }
               };

        Message msg = new Message();
        Bundle bundle = new Bundle();
        msg.what=1;
        bundle.putByteArray("data",data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //根据Bitmap计算相位偏移
    private org.opencv.core.Point calOffset(Bitmap front, Bitmap back) {

        Mat last = new Mat();
        Mat next = new Mat();

        Utils.bitmapToMat(front,last);
        Utils.bitmapToMat(back,next);

        Imgproc.cvtColor(last,last,Imgproc.COLOR_BGR2GRAY);
        last.convertTo(last,CV_32FC1);

        Imgproc.cvtColor(next,next,Imgproc.COLOR_BGR2GRAY);
        next.convertTo(next,CV_32FC1);

        //应该是后一张和前一张去比较，假设向右移动，那么前一张相对于后一张是向左移动了。
        org.opencv.core.Point point = Imgproc.phaseCorrelate(next,last);

        last.release();
        next.release();

        return point;
    }

    //重启Activity
    private  void restartActivity(){
        Intent intent = getIntent();
        //Activity的切换动画，这里为0表示无
        //overridePendingTransition(0, 0);
        //没有动画
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        //overridePendingTransition(0, 0);
        startActivity(intent);
    }

    //根据素材图测试相位偏移
    private void testPhaseCorrelate(){

        Bitmap front = BitmapFactory.decodeResource(getResources(),R.drawable.ss);

        Bitmap back = BitmapFactory.decodeResource(getResources(),R.drawable.qq);

        Mat last = new Mat();
        Mat next = new Mat();

        Utils.bitmapToMat(front,last);
        Utils.bitmapToMat(back,next);

        Imgproc.cvtColor(last,last,Imgproc.COLOR_BGR2GRAY);
        last.convertTo(last,CV_32FC1);

        Imgproc.cvtColor(next,next,Imgproc.COLOR_BGR2GRAY);
        next.convertTo(next,CV_32FC1);

        org.opencv.core.Point point = Imgproc.phaseCorrelate(last,next);

        //向右为正，向下为正

        Log.i("testPhaseCorrelate","X轴的偏移量:"+(int)point.x+",Y轴的偏移量:"+(int)point.y);

        last.release();

        next.release();

    }

    //根据帧测试相位偏移
    private void testPhaseCorrelate1() {
        Bitmap front = BitmapFactory.decodeFile(paths.get(0));

        Bitmap back = BitmapFactory.decodeFile(paths.get(1));

        Mat last = new Mat();
        Mat next = new Mat();

        Utils.bitmapToMat(front,last);
        Utils.bitmapToMat(back,next);

        Imgproc.cvtColor(last,last,Imgproc.COLOR_BGR2GRAY);
        last.convertTo(last,CV_32FC1);

        Imgproc.cvtColor(next,next,Imgproc.COLOR_BGR2GRAY);
        next.convertTo(next,CV_32FC1);

        org.opencv.core.Point point = Imgproc.phaseCorrelate(last,next);

        //向右为正，向下为正

        Log.i("testPhaseCorrelate1","X轴的偏移量:"+(int)point.x+",Y轴的偏移量:"+(int)point.y);

        last.release();

        next.release();
    }


    //相机预览池里NV21格式的byte[]转bitmap
    private Bitmap bytetoBitmap(byte[] data) {
        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), mSize.width, mSize.height,null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, mSize.width,  mSize.height), 100, out);
         //缩放
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;

        Bitmap bmp = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size(),options);

        return bmp;
    }


    private  void  saveBitmap(Bitmap bitmap,int count){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/TempCamera/";
        File appDir = new File(path);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] bytes = stream.toByteArray();
        File pictureFile = new File(appDir,ViewUtils.getPhotoFilename(count));
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bytes);
            fos.close();
            paths.add(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //截图手电筒开
    public void openFlash() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        }
    }
    //截图手电筒关
    public void closeFlash() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }
    }

    private class CaptureOrientationEventListener extends OrientationEventListener {
        public CaptureOrientationEventListener(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            if (null == mCamera)
                return;
            if (orientation == ORIENTATION_UNKNOWN)
                return;
            if (isPreview) {
//                FCLog.i(CustomCameraEventLog.CAMERA_EVENT, "是否开启旋转:"+flag+",手机屏幕角度为" + orientation);
//                //只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    orientationTag = 0;
                } else if (orientation > 80 && orientation < 100) { //90度
                    orientationTag = 90;
                } else if (orientation > 170 && orientation < 190) { //180度
                    orientationTag = 180;
                } else if (orientation > 260 && orientation < 280) { //270度
                    orientationTag = 270;
                } else {
                    return;
                }
            }
            orientation = (orientation + 45) / 90 * 90;
            if (android.os.Build.VERSION.SDK_INT <= 8) {
                _rotation = (90 + orientation) % 360;
                return;
            }
            try {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                Log.d(TAG, "CameraInfo角度为" + info.orientation);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    _rotationfront = (info.orientation - orientation + 360) % 360;
                    Log.d(TAG, "前置照片角度为" + _rotationfront + ";"
                            + "CameraInfo角度为" + info.orientation);
                } else { // back-facing camera
                    _rotation = (info.orientation + orientation) % 360;
                    Log.d(TAG, "后置照片角度为" + _rotation + ";"
                            + "CameraInfo角度为" + info.orientation);
                }
            }catch (Exception e){
            }

        }
    }


    private final class MySurfaceTextureListener implements
            TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            initCamera();

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            stopCamera();
            return true;
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    private Camera.Parameters parameters;
    private Camera.AutoFocusCallback focusCallback;

    public void initCamera() {
        if (!isPreview && null != mSurfaceTexture) {
            cameraId = getCameraId();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
            }
        }
         parameters = mCamera.getParameters();
            btn_openFlash.setVisibility(View.GONE);
            if (cameraId == 0)
                btn_openFlash.setVisibility(View.VISIBLE);
            mSize = CameraUtils.getPreviewSize(parameters,RealScreenH,ScreenW);
            parameters.setPreviewSize(mSize.width,mSize.height);
            Log.i("preview_camera",mSize.width+","+mSize.height);
            //根据分辨率适配initTextureViewSize
            initTextureViewSize();

        try {
            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        isPreview = true;
        focusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success,  Camera ca) {
                            if(mCamera!=null){
                                mCamera.cancelAutoFocus();
                            }
                            if(mCaptureSensorsObserver!=null){
                                mCaptureSensorsObserver.stop();
                            }
                        }
        };
        }

    private void initTextureViewSize() {
        cameraH =mSize.height;
        cameraW=mSize.width;
        CameraRatio=cameraW/cameraH;
        if((int)(CameraRatio*ScreenW)>=ScreenH)
            textureView.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW), Gravity.CENTER));
        else
            textureView.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW), Gravity.TOP));
    }

    public int getCameraId() {
        if (cameraId == -1) {
            initCameraId();
        }
        return (cameraId);
    }

    private void initCameraId() {
        int count = Camera.getNumberOfCameras();
        int result = -1;
        if (count > 0) {
            result = 0; // if we have a camera, default to this one
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                        && !useFrontFacingCamera()) {
                    result = i;
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        && useFrontFacingCamera()) {
                    result = i;
                    break;
                }
            }
        }
        cameraId = result;
    }

    protected boolean useFrontFacingCamera() {
        return (useFrontFacingCamera);
    }

    // 停止相机
    private void stopCamera(){
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
                isPreview = false;
            }
        }
    }

}
