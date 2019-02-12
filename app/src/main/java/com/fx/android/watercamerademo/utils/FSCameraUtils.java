package com.fx.android.watercamerademo.utils;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by sunbo on 2018/11/29.
 */

public class FSCameraUtils {

    /**
     * 相机预览尺寸，避免变形
     *
     * @param sizes
     * @param w
     * @param h
     * @param aspectTolerance
     *
     * @return
     */
    public static final Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h,
                                                          double aspectTolerance) {
        double targetRatio = (double) w / h;//预览标准比值
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetWidth = w;
        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            int diff = Math.abs(size.height - targetHeight) + Math.abs(size.width - targetWidth);
            if (diff < minDiff) {
                optimalSize = size;
                minDiff = diff;
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                int diff = Math.abs(size.height - targetHeight) + Math.abs(size.width - targetWidth);
                if (diff < minDiff) {
                    optimalSize = size;
                    minDiff = diff;
                }
            }
        }
        // ToastUtils.show(optimalSize.width+","+optimalSize.height);
        return optimalSize;
    }
}
