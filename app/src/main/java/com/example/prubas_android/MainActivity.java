package com.example.prubas_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facetec.sdk.FaceTecSessionResult;
import com.facetec.sdk.FaceTecSessionStatus;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FaceTecLivenessButton livenessButton;

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

        livenessButton = findViewById(R.id.livenessButton);
        livenessButton.setResultListener(new FaceTecLivenessButton.LivenessResultListener() {
            @Override
            public void onLivenessSuccess(FaceTecSessionResult result) {
                Log.d(TAG, "Liveness exitoso!");
            }

            @Override
            public void onLivenessError(FaceTecSessionStatus status) {
                Log.d(TAG, "Liveness error: " + status.name());
            }

            @Override
            public void onInitializationError(String error) {
                Log.e(TAG, "Error de inicialización: " + error);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FaceTecLivenessButton.CAMERA_PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            livenessButton.onPermissionResult(granted);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        livenessButton.handleActivityResult(requestCode, resultCode, data);
    }
}
