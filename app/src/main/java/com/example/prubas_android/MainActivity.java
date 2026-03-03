package com.example.prubas_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facetec.sdk.FaceTecInitializationError;
import com.facetec.sdk.FaceTecSDK;
import com.facetec.sdk.FaceTecSDKInstance;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    public FaceTecSDKInstance sdkInstance;
    private Button livenessCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        livenessCheck = findViewById(R.id.livenessCheck);
        livenessCheck.setText("Verificando permisos...");
        livenessCheck.setEnabled(false);

        livenessCheck.setOnClickListener(v -> {
            if (sdkInstance != null) {
                sdkInstance.start3DLiveness(this, new SessionRequestProcessor());
            }
        });

        // Verificar permiso de cámara
        checkCameraPermissionAndInitialize();
    }

    private void checkCameraPermissionAndInitialize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initializeFaceTecSDK();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeFaceTecSDK() {
        livenessCheck.setText("Inicializando...");
        Log.d("FaceTec", "Iniciando inicialización del SDK...");

        try {
            FaceTecSDK.initializeWithSessionRequest(this, Config.DeviceKeyIdentifier, new SessionRequestProcessor(), new FaceTecSDK.InitializeCallback() {
                @Override
                public void onSuccess(@NonNull FaceTecSDKInstance _sdkInstance) {
                    Log.d("FaceTec", "SDK inicializado exitosamente");
                    sdkInstance = _sdkInstance;
                    runOnUiThread(() -> {
                        livenessCheck.setText("Estoy listo");
                        livenessCheck.setEnabled(true);
                    });
                }

                @Override
                public void onError(@NonNull FaceTecInitializationError error) {
                    Log.e("FaceTec", "Error de inicialización: " + error.name());
                    runOnUiThread(() -> {
                        livenessCheck.setText("Error: " + error.name());
                        livenessCheck.setEnabled(false);
                    });
                }
            });
        } catch (Exception e) {
            Log.e("FaceTec", "Excepción durante inicialización: " + e.getMessage(), e);
            runOnUiThread(() -> {
                livenessCheck.setText("Excepción: " + e.getMessage());
                livenessCheck.setEnabled(false);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFaceTecSDK();
            } else {
                livenessCheck.setText("Permiso de cámara denegado");
                livenessCheck.setEnabled(false);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}