package com.pascalwelsch.circularprogressbarsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pascalwelsch.holocircularprogressbar.RingProgressBar;

public class HomeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private RingProgressBar mActiveTimeProgressBar;
    private RingProgressBar mStepsProgressBar;
    private RingProgressBar mCaloriesProgressBar;
    private RingProgressBar mSleepProgressBar;

    private Button m00;
    private Button m01;
    private Button mOne;
    private Button mZero;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mActiveTimeProgressBar = (RingProgressBar) findViewById(R.id.activeTimeProgressBar);
        mStepsProgressBar = (RingProgressBar) findViewById(R.id.stepsProgressBar);
        mCaloriesProgressBar = (RingProgressBar) findViewById(R.id.caloriesProgressBar);
        mSleepProgressBar = (RingProgressBar) findViewById(R.id.sleepProgressBar);

        m00 = (Button) findViewById(R.id.animate_00);
        m01 = (Button) findViewById(R.id.animate_01);
        mZero = (Button) findViewById(R.id.zero);
        mOne = (Button) findViewById(R.id.one);

        m01.setOnClickListener(this);
        mZero.setOnClickListener(this);
        mOne.setOnClickListener(this);

        startAnimation();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.animate_00:
//                mActiveTimeProgressBar.startScaleDown();
                break;
            case R.id.animate_01:
                mActiveTimeProgressBar.setProgress(0f);
                mStepsProgressBar.setProgress(0f);
                mCaloriesProgressBar.setProgress(0f);
                mSleepProgressBar.setProgress(0f);
                startAnimation();
                break;
            case R.id.zero:
                mActiveTimeProgressBar.stopUpdateProgress();
                mActiveTimeProgressBar.startUpdateProgress(0f, 2000);
                break;
            case R.id.one:
                mActiveTimeProgressBar.stopUpdateProgress();
                mActiveTimeProgressBar.startUpdateProgress(mActiveTimeProgressBar.getProgress() + 1f, 2000);
                break;
        }
    }

    private void startAnimation() {
        mActiveTimeProgressBar.startUpdateProgress(1f * 7 / 10);
        mStepsProgressBar.startUpdateProgress(1f * 9 / 10);
        mCaloriesProgressBar.startUpdateProgress(1f / 3);
        mSleepProgressBar.startUpdateProgress(1f * 5 / 4);
    }
}
