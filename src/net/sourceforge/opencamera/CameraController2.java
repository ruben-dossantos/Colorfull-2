package net.sourceforge.opencamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraController2 extends CameraController {
	private static final String TAG = "CameraController2";

	private Context context = null;
	private CameraDevice camera = null;
	private String cameraIdS = null;
	private boolean callback_done = false;
	private CameraCaptureSession captureSession = null;
	private CaptureRequest previewRequest = null;
	private SurfaceHolder holder = null;
	
	public CameraController2(Context context, int cameraId) {
		if( MyDebug.LOG )
			Log.d(TAG, "create new CameraController2: " + cameraId);

		this.context = context;
		final Object waitObject = new Object();
		callback_done = false;

		class MyStateCallback extends CameraDevice.StateCallback {
			@Override
			public void onOpened(CameraDevice camera) {
				if( MyDebug.LOG )
					Log.d(TAG, "camera opened");
				CameraController2.this.camera = camera;
				synchronized( waitObject ) {
					callback_done = true;
					waitObject.notify();
				}
			}

			@Override
			public void onDisconnected(CameraDevice camera) {
				if( MyDebug.LOG )
					Log.d(TAG, "camera disconnected");
				camera.close();
				CameraController2.this.camera = null;
				synchronized( waitObject ) {
					callback_done = true;
					waitObject.notify();
				}
			}

			@Override
			public void onError(CameraDevice camera, int error) {
				if( MyDebug.LOG )
					Log.d(TAG, "camera error: " + error);
				camera.close();
				CameraController2.this.camera = null;
				synchronized( waitObject ) {
					callback_done = true;
					waitObject.notify();
				}
			}
		};
		MyStateCallback myStateCallback = new MyStateCallback();
		HandlerThread thread = new HandlerThread("CameraBackground"); 
		thread.start(); 
		Handler handler = new Handler(thread.getLooper()); 

		CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
		try {
			this.cameraIdS = manager.getCameraIdList()[cameraId];
			manager.openCamera(cameraIdS, myStateCallback, handler);
		}
		catch(CameraAccessException e) {
			e.printStackTrace();
			// throw as a RuntimeException instead, as this is what callers will catch
			throw new RuntimeException();
		}

		if( MyDebug.LOG )
			Log.d(TAG, "wait until camera opened...");
		// need to wait until camera is opened
		while( !callback_done ) {
			synchronized( waitObject ) {
				try {
					waitObject.wait();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if( camera == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "camera failed to open");
			throw new RuntimeException();
		}
		if( MyDebug.LOG )
			Log.d(TAG, "camera now opened");
	}

	@Override
	void release() {
		camera.close();
		camera = null;
	}

	@Override
	CameraFeatures getCameraFeatures() {
		if( MyDebug.LOG )
			Log.d(TAG, "getCameraFeatures()");
	    CameraFeatures camera_features = new CameraFeatures();
		CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
		try {
		    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
		    StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

		    android.util.Size [] camera_picture_sizes = configs.getOutputSizes(ImageFormat.JPEG);
			camera_features.picture_sizes = new ArrayList<CameraController.Size>();
			for(android.util.Size camera_size : camera_picture_sizes) {
				camera_features.picture_sizes.add(new CameraController.Size(camera_size.getWidth(), camera_size.getHeight()));
			}

		    android.util.Size [] camera_video_sizes = configs.getOutputSizes(MediaRecorder.class);
			camera_features.video_sizes = new ArrayList<CameraController.Size>();
			for(android.util.Size camera_size : camera_video_sizes) {
				camera_features.video_sizes.add(new CameraController.Size(camera_size.getWidth(), camera_size.getHeight()));
			}

			android.util.Size [] camera_preview_sizes = configs.getOutputSizes(SurfaceHolder.class);
			camera_features.preview_sizes = new ArrayList<CameraController.Size>();
			for(android.util.Size camera_size : camera_preview_sizes) {
				camera_features.preview_sizes.add(new CameraController.Size(camera_size.getWidth(), camera_size.getHeight()));
			}

		}
		catch(CameraAccessException e) {
			e.printStackTrace();
		}
	    return camera_features;
	}

	@Override
	SupportedValues setSceneMode(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSceneMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	SupportedValues setColorEffect(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColorEffect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	SupportedValues setWhiteBalance(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWhiteBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	SupportedValues setISO(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getISOKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Size getPictureSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setPictureSize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public Size getPreviewSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setPreviewSize(int width, int height) {
		if( MyDebug.LOG )
			Log.d(TAG, "setPreviewSize: " + width + " , " + height);
		if( holder != null ) {
			holder.setFixedSize(width, height);
		}
	}

	@Override
	void setVideoStabilization(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getVideoStabilization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getJpegQuality() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void setJpegQuality(int quality) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getZoom() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void setZoom(int value) {
		// TODO Auto-generated method stub

	}

	@Override
	int getExposureCompensation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	boolean setExposureCompensation(int new_exposure) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void setPreviewFpsRange(int min, int max) {
		// TODO Auto-generated method stub

	}

	@Override
	void getPreviewFpsRange(int[] fps_range) {
		// TODO Auto-generated method stub

	}

	@Override
	List<int[]> getSupportedPreviewFpsRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultSceneMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultColorEffect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultWhiteBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultISO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setFocusValue(String focus_value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFocusValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setFlashValue(String flash_value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFlashValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setRecordingHint(boolean hint) {
		// TODO Auto-generated method stub

	}

	@Override
	void setAutoExposureLock(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getAutoExposureLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void setRotation(int rotation) {
		// TODO Auto-generated method stub

	}

	@Override
	void setLocationInfo(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	void removeLocationInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	void enableShutterSound(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	boolean setFocusAndMeteringArea(List<Area> areas) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void clearFocusAndMetering() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Area> getFocusAreas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Area> getMeteringAreas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean supportsAutoFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean focusIsVideo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void reconnect() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	void setPreviewDisplay(SurfaceHolder holder) throws IOException {
		if( MyDebug.LOG )
			Log.d(TAG, "setPreviewDisplay");
		this.holder = holder;
	}

	@Override
	void startPreview() {
		if( MyDebug.LOG )
			Log.d(TAG, "startPreview");

		try {
			captureSession = null;
			previewRequest = null;

			final CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			builder.addTarget(holder.getSurface());

			final Object waitObject = new Object();
			callback_done = false;

			camera.createCaptureSession(Arrays.asList(holder.getSurface()),
				new CameraCaptureSession.StateCallback() {
					@Override
					public void onConfigured(CameraCaptureSession session) {
						if( MyDebug.LOG )
							Log.d(TAG, "onConfigured");
						if( camera == null ) {
							synchronized( waitObject ) {
								callback_done = true;
								waitObject.notify();
							}
							return;
						}
						captureSession = session;
						previewRequest = builder.build();
						synchronized( waitObject ) {
							callback_done = true;
							waitObject.notify();
						}
					}

					@Override
					public void onConfigureFailed(CameraCaptureSession session) {
						if( MyDebug.LOG )
							Log.d(TAG, "onConfigureFailed");
						synchronized( waitObject ) {
							callback_done = true;
							waitObject.notify();
						}
					}
		 		},
		 		null);

			if( MyDebug.LOG )
				Log.d(TAG, "wait until preview capture session is configured");
			while( !callback_done ) {
				synchronized( waitObject ) {
					try {
						waitObject.wait();
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if( captureSession == null || previewRequest == null ) {
				if( MyDebug.LOG )
					Log.d(TAG, "preview capture session failed to be configured");
				//throw new IOException();
			}
			if( MyDebug.LOG )
				Log.d(TAG, "preview capture session is configured");
		}
		catch(CameraAccessException e) {
			e.printStackTrace();
			//throw new IOException();
		} 

		if( captureSession == null || previewRequest == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "capture session or preview request not yet available");
			return;
		}
		try {
			captureSession.setRepeatingRequest(previewRequest, null, null);
		}
		catch(CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	void stopPreview() {
		if( MyDebug.LOG )
			Log.d(TAG, "stopPreview");
		if( captureSession == null )
			return;
		try {
			captureSession.stopRepeating();
		}
		catch(CameraAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean startFaceDetection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void setFaceDetectionListener(FaceDetectionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	void autoFocus(AutoFocusCallback cb) {
		// TODO Auto-generated method stub

	}

	@Override
	void cancelAutoFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	void takePicture(PictureCallback raw, PictureCallback jpeg) {
		// TODO Auto-generated method stub

	}

	@Override
	void setDisplayOrientation(int degrees) {
		// TODO Auto-generated method stub

	}

	@Override
	int getDisplayOrientation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int getCameraOrientation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	boolean isFrontFacing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void unlock() {
		// TODO Auto-generated method stub

	}

	@Override
	void initVideoRecorder(MediaRecorder video_recorder) {
		// TODO Auto-generated method stub

	}

	@Override
	String getParametersString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Parameters getCameraParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
