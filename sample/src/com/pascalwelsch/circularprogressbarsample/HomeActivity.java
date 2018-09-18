package com.pascalwelsch.circularprogressbarsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pascalwelsch.holocircularprogressbar.RingProgressBar;

public class HomeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private RingProgressBar mRingProgressBar;

    private Button m01;
    private Button mOne;
    private Button mZero;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mRingProgressBar = (RingProgressBar) findViewById(
                R.id.holoCircularProgressBar);

        mRingProgressBar.setMarkerEnabled(false);
        mRingProgressBar.setThumbEnabled(true);

        m01 = (Button) findViewById(R.id.animate_01);
        mZero = (Button) findViewById(R.id.zero);
        mOne = (Button) findViewById(R.id.one);

        m01.setOnClickListener(this);
        mZero.setOnClickListener(this);
        mOne.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.animate_01:
                mRingProgressBar.startUpdateProgress(mRingProgressBar.getProgress() + 0.1f);
                break;
            case R.id.zero:
                mRingProgressBar.stopUpdateProgress();
                mRingProgressBar.startUpdateProgress(0f, 2000);
                break;
            case R.id.one:
                mRingProgressBar.stopUpdateProgress();
                mRingProgressBar.startUpdateProgress(1f, 2000);
                break;
        }
    }
}
