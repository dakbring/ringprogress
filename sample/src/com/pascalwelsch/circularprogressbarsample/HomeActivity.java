package com.pascalwelsch.circularprogressbarsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pascalwelsch.holocircularprogressbar.RingProgressBar;

public class HomeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private RingProgressBar mRingProgressBar;

    private Button vPlus;
    private Button vMinus;
    private Button mThree;
    private Button mZero;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mRingProgressBar = (RingProgressBar) findViewById(
                R.id.holoCircularProgressBar);

        mRingProgressBar.setMarkerEnabled(false);
        mRingProgressBar.setThumbEnabled(true);

        vPlus = (Button) findViewById(R.id.plus);
        vMinus = (Button) findViewById(R.id.minus);
        mZero = (Button) findViewById(R.id.zero);
        mThree = (Button) findViewById(R.id.three);

        vPlus.setOnClickListener(this);
        vMinus.setOnClickListener(this);
        mZero.setOnClickListener(this);
        mThree.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.minus:
                mRingProgressBar.startUpdateProgress(mRingProgressBar.getProgress() - 0.1f);
                break;
            case R.id.plus:
                mRingProgressBar.startUpdateProgress(mRingProgressBar.getProgress() + 0.1f);
                break;
            case R.id.zero:
                mRingProgressBar.stopUpdateProgress();
                mRingProgressBar.startUpdateProgress(0f, 2000);
                break;
            case R.id.three:
                mRingProgressBar.stopUpdateProgress();
                mRingProgressBar.startUpdateProgress(3f, 2000);
                break;
        }
    }
}
