package com.fx.android.watercamerademo.utils;

/***
 Copyright (c) 2013 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CameraUtils {
	// based on ApiDemos
	private static final double ASPECT_TOLERANCE = 0.1;

	public static Size getOptimalPreviewSize(int displayOrientation,
                                             int width, int height, Parameters parameters) {
		double targetRatio = (double) width / height;
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = height;

		if (displayOrientation == 90 || displayOrientation == 270) {
			targetRatio = (double) height / width;
		}

		// Try to find an size match aspect ratio and size

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;

			if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		// Cannot find the one match the aspect ratio, ignore
		// the requirement

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;

			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return (optimalSize);
	}

	public static Size getBestAspectPreviewSize(int displayOrientation,
                                                int width, int height, Parameters parameters) {
		return (getBestAspectPreviewSize(displayOrientation, width, height,
				parameters, 0.0d));
	}

	public static Size getBestAspectPreviewSize(int displayOrientation,
                                                int width, int height, Parameters parameters,
                                                double closeEnough) {
		double targetRatio = (double) width / height;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		if (displayOrientation == 90 || displayOrientation == 270) {
			targetRatio = (double) height / width;
		}

		List<Size> sizes = parameters.getSupportedPreviewSizes();

		Collections.sort(sizes, Collections.reverseOrder(new SizeComparator()));

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(ratio - targetRatio);
			}

			if (minDiff < closeEnough) {
				break;
			}
		}

		return (optimalSize);
	}

	public static Size getSmallestPictureSize(
			Parameters parameters) {
		Size result = null;

		for (Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea < resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	public static String findBestFlashModeMatch(Parameters params,
                                                String... modes) {
		String match = null;

		List<String> flashModes = params.getSupportedFlashModes();

		if (flashModes != null) {
			for (String mode : modes) {
				if (flashModes.contains(mode)) {
					match = mode;
					break;
				}
			}
		}

		return (match);
	}

	private static class SizeComparator implements Comparator<Size> {
		@Override
		public int compare(Size lhs, Size rhs) {
			int left = lhs.width * lhs.height;
			int right = rhs.width * rhs.height;

			if (left < right) {
				return (-1);
			} else if (left > right) {
				return (1);
			}

			return (0);
		}
	}
	
	public static final Size getOptimalPreviewSize(List<Size> sizes, int w, int h, double aspectTolerance) {
        double targetRatio = (double)w / (double)h;
        if(sizes == null) {
            return null;
        } else {
            Size optimalSize = null;
            double minDiff = 1.7976931348623157E308D;
            int targetWidth = w;
            int targetHeight = h;
            Iterator var12 = sizes.iterator();

            Size size;
            while(var12.hasNext()) {
                size = (Size)var12.next();
                double ratio = (double)size.width / (double)size.height;
                if(Math.abs(ratio - targetRatio) <= aspectTolerance) {
                    int diff = Math.abs(size.height - targetHeight) + Math.abs(size.width - targetWidth);
                    if((double)diff < minDiff) {
                        optimalSize = size;
                        minDiff = (double)diff;
                    }
                }
            }

            if(optimalSize == null) {
                minDiff = 1.7976931348623157E308D;
                var12 = sizes.iterator();

                while(var12.hasNext()) {
                    size = (Size)var12.next();
                    int diff = Math.abs(size.height - targetHeight) + Math.abs(size.width - targetWidth);
                    if((double)diff < minDiff) {
                        optimalSize = size;
                        minDiff = (double)diff;
                    }
                }
            }

            return optimalSize;
        }
    }
	
	
	public static final Size getPreviewSize(Parameters parameters, int w, int h){
		Size newSize = getMaxSize(parameters.getSupportedPreviewSizes(),w,h);
		if(newSize == null){
			newSize = parameters.getPreferredPreviewSizeForVideo();
		}
		return newSize;
	}
	
	
    public static void handleZoom(boolean isZoomIn, Camera camera) {
        if(camera==null)
            return;
        Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int step = maxZoom / 10;
            if (step == 0) {
                step = 1;
            }
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom += step;
                if (zoom > maxZoom - step / 2) {
                    zoom = maxZoom;
                }
            } else if (zoom > 0 && !isZoomIn) {
                zoom -= step;
                if (zoom < step / 2) {
                    zoom = 0;
                }
            } else {
                return;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {

        }
    }

	private static Camera.Size getMaxSize (List<Size> supportedSizes, int w, int h) {


		//获取预览的各种分辨率
		Camera.Size max = null;

		double maxSize=0,minSize= Integer.MAX_VALUE;
		double maxFit=0,minFix= Double.MAX_VALUE;


		for (Camera.Size size :supportedSizes) {
			double tagSize = size.height*1d*size.width;//越大越好
			if (tagSize>maxSize) {
				maxSize = tagSize;
			}
			if (tagSize<minSize) {
				minSize = tagSize;
			}
			double tagFit = Math.abs(size.height*1d/size.width-h*1d/w);//越小越好

			if (tagFit>maxFit) {
				maxFit=tagFit;
			}
			if (tagFit<minFix) {
				minFix = tagFit;
			}
		}

//        FCLog.i(CustomCameraEventLog.CAMERA_EVENT, "最大尺寸:" + max.width + "*" + max.height);
		Collections.reverse(supportedSizes);
		for (Camera.Size size :supportedSizes) {
			double tagSize = size.height*1d*size.width;
			double sizeScore = (tagSize-minSize)*1d/(maxSize-minSize);

//            double tagFix = size.height*1d/size.width;
			double tagFix = Math.abs(size.height*1d/size.width-h*1d/w);
			double fixScore = (tagFix-minFix)*1d/(maxFit-minFix);

			if (max==null) {
				max = size;
			} else {
				double bSize = max.height*1d*max.width;
				double bestSize = (bSize-minSize)*1d/(maxSize-minSize);
//                double bFix = max.height/max.width;
				double bFix = Math.abs(max.height*1d/max.width-h*1d/w);
				double bestFix = (bFix-minFix)*1d/(maxFit-minFix);



				if (sizeScore*0.1+(1-fixScore) > bestSize*0.1+(1-bestFix)) {
					if (max.height<h && max.width<w) {
						max = size;

					}
				}
			}
		}


		return max;
	}
	
	
}
