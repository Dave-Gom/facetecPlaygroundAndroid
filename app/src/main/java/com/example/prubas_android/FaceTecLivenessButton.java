package com.example.prubas_android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facetec.sdk.FaceTecInitializationError;
import com.facetec.sdk.FaceTecSDK;
import com.facetec.sdk.FaceTecSDKInstance;
import com.facetec.sdk.FaceTecSessionResult;
import com.facetec.sdk.FaceTecSessionStatus;

public class FaceTecLivenessButton extends AppCompatButton {
    private static final String TAG = "FaceTecLivenessButton";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private FaceTecSDKInstance sdkInstance;
    private boolean isInitialized = false;
    private LivenessResultListener resultListener;

    public interface LivenessResultListener {
        void onLivenessSuccess(FaceTecSessionResult result);
        void onLivenessError(FaceTecSessionStatus status);
        void onInitializationError(String error);
    }

    public FaceTecLivenessButton(@NonNull Context context) {
        super(context);
        init();
    }

    public FaceTecLivenessButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceTecLivenessButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setText("Inicializando");
        setEnabled(false);
        setOnClickListener(v -> startLiveness());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkCameraPermissionAndInitialize();
    }

    public void setResultListener(LivenessResultListener listener) {
        this.resultListener = listener;
    }

    private void checkCameraPermissionAndInitialize() {
        Context context = getContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initializeFaceTecSDK();
        } else if (context instanceof Activity) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    public void onPermissionResult(boolean granted) {
        if (granted) {
            initializeFaceTecSDK();
        } else {
            post(() -> {
                setText("Permiso de cámara denegado");
                setEnabled(false);
            });
            if (resultListener != null) {
                resultListener.onInitializationError("Permiso de cámara denegado");
            }
        }
    }

    private void initializeFaceTecSDK() {
        Context context = getContext();
        Log.d(TAG, "Iniciando inicialización del SDK...");

        try {
            FaceTecSDK.initializeWithSessionRequest(
                    context,
                    Config.DeviceKeyIdentifier,
                    new SessionRequestProcessor(),
                    new FaceTecSDK.InitializeCallback() {
                        @Override
                        public void onSuccess(@NonNull FaceTecSDKInstance _sdkInstance) {
                            Log.d(TAG, "SDK inicializado exitosamente");
                            sdkInstance = _sdkInstance;
                            isInitialized = true;
                            post(() -> {
                                setText("Iniciar prueba de vida");
                                setEnabled(true);
                            });
                        }

                        @Override
                        public void onError(@NonNull FaceTecInitializationError error) {
                            Log.e(TAG, "Error de inicialización: " + error.name());
                            post(() -> {
                                setText("Error: " + error.name());
                                setEnabled(false);
                            });
                            if (resultListener != null) {
                                resultListener.onInitializationError(error.name());
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Excepción durante inicialización: " + e.getMessage(), e);
            post(() -> {
                setText("Error: " + e.getMessage());
                setEnabled(false);
            });
            if (resultListener != null) {
                resultListener.onInitializationError(e.getMessage());
            }
        }
    }

    private void startLiveness() {
        if (sdkInstance != null && isInitialized && getContext() instanceof Activity) {
            sdkInstance.start3DLiveness((Activity) getContext(), new SessionRequestProcessor());
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FaceTecSDK.REQUEST_CODE_SESSION) {
            FaceTecSessionResult result = FaceTecSDK.getActivitySessionResult(requestCode, resultCode, data);

            if (result != null) {
                FaceTecSessionStatus status = result.getStatus();
                Log.d(TAG, "Resultado de liveness: " + status.name());

                if (status == FaceTecSessionStatus.SESSION_COMPLETED) {
                    Log.d(TAG, "Liveness check exitoso!");
                    if (resultListener != null) {
                        resultListener.onLivenessSuccess(result);
                    }
                } else {
                    if (resultListener != null) {
                        resultListener.onLivenessError(status);
                    }
                }
            }
        }
    }
}
