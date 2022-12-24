package com.example.mediapipe.edgedetection;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor; // FrameProcessor Import 
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.glutil.EglManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    static {
        System.loadLibrary("mediapipe_jni");
        try {
            System.loadLibrary("opencv_java3");
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.loadLibrary("opencv_java4");
        }
    }

    private SurfaceTexture previewFrameTexture;
    private SurfaceView previewDisplayView;
    private CameraXPreviewHelper cameraHelper;
    private ApplicationInfo applicationInfo;
    private EglManager eglManager;
    private ExternalTextureConverter converter;
    private FrameProcessor processor; // FrameProcessor 오브젝트 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        previewDisplayView = new SurfaceView(this);
        setupPreviewDisplayView();

        AndroidAssetUtil.initializeNativeAssetManager(this);

        eglManager = new EglManager(null);

        // EGL Manager 초기화 후 Frame Processor 초기화
        processor = new FrameProcessor(
                this,
                eglManager.getNativeContext(),
                applicationInfo.metaData.getString("binaryGraphName"),
                applicationInfo.metaData.getString("inputVideoStreamName"),
                applicationInfo.metaData.getString("outputVideoStreamName"));

        PermissionHelper.checkAndRequestCameraPermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter = new ExternalTextureConverter(eglManager.getContext());
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera();
        }
        converter.setConsumer(processor); // 변환된 프레임을 proccessor로 전송
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface()); // processor의 결과물을
                                                                                                   // View로 전송
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                Size viewSize = new Size(width, height);
                                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);

                                converter.setSurfaceTextureAndAttachToGLContext(
                                        previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null); // surface destroy시 초기화
                            }
                        });
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    previewFrameTexture = surfaceTexture;
                    previewDisplayView.setVisibility(View.VISIBLE);
                });

        CameraHelper.CameraFacing cameraFacing = applicationInfo.metaData.getBoolean("cameraFacingFront", false)
                ? CameraHelper.CameraFacing.FRONT
                : CameraHelper.CameraFacing.BACK;
        cameraHelper.startCamera(this, cameraFacing, /* unusedSurfaceTexture= */ null);
    }
}