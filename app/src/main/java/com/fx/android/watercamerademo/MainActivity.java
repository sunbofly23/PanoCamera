package com.fx.android.watercamerademo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fx.android.watercamerademo.Camera.OrdinaryCameraPreviewActivity;
import com.fx.android.watercamerademo.utils.DialogUtil;
import com.fx.android.watercamerademo.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Button mBtnCamera;
    private ArrayList<String> pathList = new ArrayList<>();
    private String [] strs= new String[15];
    private Dialog dialog;
    private ImageView mIvPano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//使用相机拍照
        mBtnCamera=(Button)findViewById(R.id.btn_camera);
        mIvPano=(ImageView)findViewById(R.id.iv_pano);
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>23){
                    if(checkPermission(MainActivity.this)) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 1);
                    }
                    else{
                        startActivityForResult(
                                new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class)
                                ,301);
                    }
                }else{
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class)
                            ,301);
                }
            }
        });

    }

    private boolean checkPermission(Activity activity) {
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class).
                                    putExtra("CameraModel",0),301);
                }
                else
                    Toast.makeText(MainActivity.this,"用户否定了这个权限",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private ImagesStitchUtil.onStitchResultListener listener =new ImagesStitchUtil.onStitchResultListener(){
        @Override
        public void onSuccess(final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/TempCamera/";
                    FileUtils.deleteDirectory(path);
                    savePanoBitmap(bitmap);
                }
            });
        }

        @Override
        public void onError(final String errorMsg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
                }
            });
            if(dialog.isShowing()){
                DialogUtil.closeDialog(dialog);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 301:
                    dialog = DialogUtil.showDialog(MainActivity.this,"正在合成中……",false,true);
                    pathList = data.getStringArrayListExtra("IMAGEKEY");
                    strs = (String[]) pathList.toArray(new String[0]);
                    //需要在子线程中处理的逻辑
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ImagesStitchUtil.StitchImages(strs, listener);
                        }
                    }).start();
                    break;
            }
        }
    }


//保存处理后的图并且回传
    public void savePanoBitmap(Bitmap bitmap) {
// 首先保存图片
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/PanoCamera/";
        File appDir = new File(path);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = getPhotoFilename();
        File file = new File(appDir, fileName);
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
                Log.i("PanoCamera1","success");
                if(dialog!=null&&dialog.isShowing()){
                    DialogUtil.closeDialog(dialog);
                }
                mIvPano.setImageBitmap(bitmap);
            } else
                Log.i("PanoCamera1","error");

        } catch (Exception e) {
            e.printStackTrace();
        }
// 通知图库更新
        MediaScannerConnection.scanFile(this,
                new String[]{file.getPath()},
                new String[]{"image/jpeg"}, null);
    }
    //文件名字
    protected static String getPhotoFilename() {
        String ts=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return("Pamo_" + ts + ".jpg");
    }

}
