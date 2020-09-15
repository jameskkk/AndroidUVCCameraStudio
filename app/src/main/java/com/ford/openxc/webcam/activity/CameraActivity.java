package com.ford.openxc.webcam.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ford.openxc.webcam.R;
import com.ford.openxc.webcam.webcam.WebcamPreview;

public class CameraActivity extends Activity {
    private EditText mEtVideo;
    private WebcamPreview mPreview;
    private Button mButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtVideo = (EditText) findViewById(R.id.et_video);
        mPreview = (WebcamPreview) findViewById(R.id.preview);
        mButton = (Button) findViewById(R.id.bt_preview);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.stopPreview();
                mPreview.startPreview(mEtVideo.getText().toString());
            }
        });

    }
}
