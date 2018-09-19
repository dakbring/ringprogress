package com.pascalwelsch.holocircularprogressbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class RingProgressBar extends View {

    private static final String TAG = RingProgressBar.class.getSimpleName();

    private static final String INSTANCE_STATE_SAVED_STATE = "saved_state";

    private static final String INSTANCE_STATE_PROGRESS = "progress";

    private static final String INSTANCE_STATE_PROGRESS_COLOR = "progress_color";

    private static final String INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR = "progress_background_color";

    private static final int DEFAULT_ANIMATION_DURATION = 1000;

    private static final int DEFAULT_ICON_SIZE = 60;
    private static final int DEFAULT_STROKE_WIDTH = 10;

    private static final int MAX_PROGRESS_ONE_CIRCLE = 1;
    private static final int MAX_PROGRESS_TWO_CIRCLE = 2;
    private static final int MAX_PROGRESS_THREE_CIRCLE = 3;

    private RectF mCircleBounds_1 = new RectF();
    private RectF mCircleBounds_2 = new RectF();
    private RectF mCircleBounds_3 = new RectF();

    private Paint mBackgroundColorPaint = new Paint();

    private int mRingStrokeWidth;

    private boolean mIsInitializing = true;

    private float mProgress;

    private int mProgressBackgroundColor;

    private int mProgressColor;

    private Paint mProgressColorPaint;

    private float mTranslationOffsetX;

    private float mTranslationOffsetY;

    private Bitmap mIcon;

    private int mIconSize;

    private float mMaxProgress;

    private ObjectAnimator mObjectAnimator;

    public RingProgressBar(final Context context) {
        this(context, null);
    }

    public RingProgressBar(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.circularProgressBarStyle);
    }

    public RingProgressBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar, defStyle, 0);
        if (attributes != null) {
            try {
                setProgressColor(attributes.getColor(R.styleable.RingProgressBar_progress_color, Color.CYAN));
                setProgressBackgroundColor(attributes.getColor(R.styleable.RingProgressBar_progress_background_color, Color.GREEN));
                setProgress(attributes.getFloat(R.styleable.RingProgressBar_progress, 0.0f));
                setStrokeWidth((int) attributes.getDimension(R.styleable.RingProgressBar_stroke_width, DEFAULT_STROKE_WIDTH));
                setIconSize((int) attributes.getDimension(R.styleable.RingProgressBar_icon_size, DEFAULT_ICON_SIZE));
                setMaxProgress(attributes.getFloat(R.styleable.RingProgressBar_max_progress, 1f));

                Bitmap icon = BitmapFactory.decodeResource(getResources(), attributes.getResourceId(R.styleable.RingProgressBar_icon, 0));
                mIcon = Bitmap.createScaledBitmap(icon, mIconSize, mIconSize, false);
            } finally {
                // make sure recycle is always called.
                attributes.recycle();
            }
        }

        updateBackgroundColor();
        updateProgressColor();

        // the view has all the properties and ready to be draw
        mIsInitializing = false;

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.translate(mTranslationOffsetX, mTranslationOffsetY);

        final float progressRotation = getCurrentRotation();

        if (mProgress <= MAX_PROGRESS_THREE_CIRCLE) {
            canvas.drawArc(mCircleBounds_1, 270, -(360 - progressRotation), false, mBackgroundColorPaint);
            canvas.drawArc(mCircleBounds_1, 270, mProgress > 1 ? 360 : progressRotation, false, mProgressColorPaint);

            if (mProgress > MAX_PROGRESS_ONE_CIRCLE) {
                canvas.drawArc(mCircleBounds_2, 270, mProgress > 2 ? 360 : progressRotation - 360, false, mProgressColorPaint);
            }
            if (mProgress > MAX_PROGRESS_TWO_CIRCLE) {
                canvas.drawArc(mCircleBounds_3, 270, mProgress == 3 ? 360 : progressRotation - 720, false, mProgressColorPaint);
            }
        }

        canvas.translate(0, -mTranslationOffsetY);
        canvas.drawBitmap(mIcon, -mIcon.getWidth() / 2, 0, null);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int mainValue = widthMeasureSpec > heightMeasureSpec ? widthMeasureSpec : heightMeasureSpec;
        final int width = getDefaultSize(getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(), mainValue);
        setMeasuredDimension(width, width);

        final float halfWidth = width / 2;

        // width of the circle
        final float ringWith = mRingStrokeWidth * 1.1f;

        // radius to draw max 3 rings
        float radius1 = halfWidth - mIconSize / 2;
        float radius2 = radius1 - ringWith;
        float radius3 = radius2 - ringWith;

        mCircleBounds_1.set(-radius1, -radius1, radius1, radius1);
        mCircleBounds_2.set(-radius2, -radius2, radius2, radius2);
        mCircleBounds_3.set(-radius3, -radius3, radius3, radius3);

        mTranslationOffsetX = halfWidth;
        mTranslationOffsetY = halfWidth;

    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            setProgress(bundle.getFloat(INSTANCE_STATE_PROGRESS));

            final int progressColor = bundle.getInt(INSTANCE_STATE_PROGRESS_COLOR);
            if (progressColor != mProgressColor) {
                mProgressColor = progressColor;
                updateProgressColor();
            }

            final int progressBackgroundColor = bundle.getInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR);
            if (progressBackgroundColor != mProgressBackgroundColor) {
                mProgressBackgroundColor = progressBackgroundColor;
                updateBackgroundColor();
            }

            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE_SAVED_STATE));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE_SAVED_STATE, super.onSaveInstanceState());
        bundle.putFloat(INSTANCE_STATE_PROGRESS, mProgress);
        bundle.putInt(INSTANCE_STATE_PROGRESS_COLOR, mProgressColor);
        bundle.putInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR, mProgressBackgroundColor);
        return bundle;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        if (progress == mProgress) {
            return;
        }

        mMaxProgress = mMaxProgress < MAX_PROGRESS_THREE_CIRCLE ? mMaxProgress : MAX_PROGRESS_THREE_CIRCLE;
        mProgress = progress < MAX_PROGRESS_THREE_CIRCLE && progress < mMaxProgress ? progress : mMaxProgress;

        if (!mIsInitializing) {
            invalidate();
        }
    }

    public void setProgressBackgroundColor(int color) {
        mProgressBackgroundColor = color;
        updateBackgroundColor();
    }

    public void setProgressColor(int color) {
        mProgressColor = color;

        updateProgressColor();
    }

    public void setStrokeWidth(int dimension) {
        mRingStrokeWidth = dimension;

        // update the paints
        updateBackgroundColor();
        updateProgressColor();
    }

    public void setMaxProgress(float maxProgress) {
        mMaxProgress = maxProgress;

        invalidate();
    }

    public void setIconSize(int iconSize) {
        mIconSize = iconSize;

        invalidate();
    }

    private float getCurrentRotation() {
        return 360 * mProgress;
    }

    private void updateBackgroundColor() {
        mBackgroundColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundColorPaint.setColor(mProgressBackgroundColor);
        mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
        mBackgroundColorPaint.setStrokeWidth(mRingStrokeWidth);

        invalidate();
    }

    private void updateProgressColor() {
        mProgressColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressColorPaint.setColor(mProgressColor);
        mProgressColorPaint.setStyle(Paint.Style.STROKE);
        mProgressColorPaint.setStrokeWidth(mRingStrokeWidth);
        mProgressColorPaint.setStrokeCap(Paint.Cap.ROUND);

        invalidate();
    }

    public void startUpdateProgress(float progress) {
        startUpdateProgress(progress, DEFAULT_ANIMATION_DURATION);
    }

    public void startUpdateProgress(float progress, int duration) {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        mObjectAnimator.setDuration(duration);

        mObjectAnimator.reverse();
        mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                setProgress((Float) animation.getAnimatedValue());
            }
        });
        mObjectAnimator.start();
    }

    public void stopUpdateProgress() {
        if (mObjectAnimator != null) {
            mObjectAnimator.cancel();
        }
    }

}
