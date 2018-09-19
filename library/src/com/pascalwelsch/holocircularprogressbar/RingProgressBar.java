package com.pascalwelsch.holocircularprogressbar;

import android.animation.Animator;
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

    /**
     * TAG constant for logging
     */
    private static final String TAG = RingProgressBar.class.getSimpleName();

    /**
     * used to save the super state on configuration change
     */
    private static final String INSTANCE_STATE_SAVED_STATE = "saved_state";

    /**
     * used to save the progress on configuration changes
     */
    private static final String INSTANCE_STATE_PROGRESS = "progress";

    /**
     * used to save the background color of the progress
     */
    private static final String INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR = "progress_background_color";

    /**
     * used to save the color of the progress
     */
    private static final String INSTANCE_STATE_PROGRESS_COLOR = "progress_color";

    /**
     * used to load default duration for update progress animation
     */
    private static final int DEFAULT_ANIMATION_DURATION = 1000;

    /**
     * used to load default size of icon if don't have value from attribute
     */
    private static final int DEFAULT_ICON_SIZE = 100;

    private static final int MAX_PROGRESS_ONE_CIRCLE = 1;
    private static final int MAX_PROGRESS_TWO_CIRCLE = 2;
    private static final int MAX_PROGRESS_THREE_CIRCLE = 3;

    /**
     * The rectangle enclosing the circle.
     */
    private final RectF mCircleBounds_1 = new RectF();
    private final RectF mCircleBounds_2 = new RectF();
    private final RectF mCircleBounds_3 = new RectF();

    /**
     * the paint for the background.
     */
    private Paint mBackgroundColorPaint = new Paint();

    /**
     * The stroke width used to paint the circle.
     */
    private int mCircleStrokeWidth = 10;

    /**
     * true if not all properties are set. then the view isn't drawn and there are no errors in the
     * LayoutEditor
     */
    private boolean mIsInitializing = true;

    /**
     * The current progress.
     */
    private float mProgress;

    /**
     * The color of the progress background.
     */
    private int mProgressBackgroundColor;

    /**
     * the color of the progress.
     */
    private int mProgressColor;

    /**
     * paint for the progress.
     */
    private Paint mProgressColorPaint;

    /**
     * The Translation offset x which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetX;

    /**
     * The Translation offset y which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetY;

    /**
     * The icon get from attribute
     */
    private Bitmap mIcon;

    /**
     * The icon size get from attribute
     */
    private int mIconSize;

    /**
     * The max progress can show
     */
    private float mMaxProgress;

    /**
     * The animator run update progress
     */
    private ObjectAnimator mObjectAnimator;

    /**
     * Instantiates a new holo circular progress bar.
     *
     * @param context the context
     */
    public RingProgressBar(final Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new holo circular progress bar.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public RingProgressBar(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.circularProgressBarStyle);
    }

    /**
     * Instantiates a new holo circular progress bar.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public RingProgressBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        // load the styled attributes and set their properties
        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar, defStyle, 0);
        if (attributes != null) {
            try {
                setProgressColor(attributes.getColor(R.styleable.RingProgressBar_progress_color, Color.CYAN));
                setProgressBackgroundColor(attributes.getColor(R.styleable.RingProgressBar_progress_background_color, Color.GREEN));
                setProgress(attributes.getFloat(R.styleable.RingProgressBar_progress, 0.0f));
                setWheelSize((int) attributes.getDimension(R.styleable.RingProgressBar_stroke_width, 10));
                mIconSize = (int) attributes.getDimension(R.styleable.RingProgressBar_icon_size, 60);
                mMaxProgress = attributes.getFloat(R.styleable.RingProgressBar_max_progress, 1f);

                Bitmap icon = BitmapFactory.decodeResource(getResources(), attributes.getResourceId(R.styleable.RingProgressBar_icon, 0));
                mIcon = Bitmap.createScaledBitmap(icon, mIconSize, mIconSize, false);
            } finally {
                // make sure recycle is always called.
                attributes.recycle();
            }
        }

        updateBackgroundColor();
        updateProgressColor();

        // the view has now all properties and can be drawn
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
        final int width = getDefaultSize(getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(), widthMeasureSpec);
        setMeasuredDimension(width, width);

        final float halfWidth = width / 2;

        // width of the circle
        final float ringWith = mCircleStrokeWidth * 1.1f;

        // -0.5f for pixel perfect fit inside the view bounds
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

    /**
     * gives the current progress of the ProgressBar. Value between 0..1 if you set the progress to
     * >1 you'll get progress % 1 as return value
     *
     * @return the progress
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(final float progress) {
        if (progress == mProgress) {
            return;
        }

        mMaxProgress = mMaxProgress < MAX_PROGRESS_THREE_CIRCLE ? mMaxProgress : MAX_PROGRESS_THREE_CIRCLE;
        mProgress = progress < MAX_PROGRESS_THREE_CIRCLE && progress < mMaxProgress ? progress : mMaxProgress;

        if (!mIsInitializing) {
            invalidate();
        }
    }

    /**
     * Sets the progress background color.
     *
     * @param color the new progress background color
     */
    public void setProgressBackgroundColor(final int color) {
        mProgressBackgroundColor = color;
        updateBackgroundColor();
    }

    /**
     * Sets the progress color.
     *
     * @param color the new progress color
     */
    public void setProgressColor(final int color) {
        mProgressColor = color;

        updateProgressColor();
    }

    /**
     * Gets the progress color.
     *
     * @return the progress color
     */
    public int getProgressColor() {
        return mProgressColor;
    }

    /**
     * Sets the wheel size.
     *
     * @param dimension the new wheel size
     */
    public void setWheelSize(final int dimension) {
        mCircleStrokeWidth = dimension;

        // update the paints
        updateBackgroundColor();
        updateProgressColor();
    }

    /**
     * Gets the current rotation.
     *
     * @return the current rotation
     */
    private float getCurrentRotation() {
        return 360 * mProgress;
    }

    /**
     * updates the paint of the background
     */
    private void updateBackgroundColor() {
        mBackgroundColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundColorPaint.setColor(mProgressBackgroundColor);
        mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
        mBackgroundColorPaint.setStrokeWidth(mCircleStrokeWidth);

        invalidate();
    }

    /**
     * updates the paint of the progress and the thumb to give them a new visual style
     */
    private void updateProgressColor() {
        mProgressColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressColorPaint.setColor(mProgressColor);
        mProgressColorPaint.setStyle(Paint.Style.STROKE);
        mProgressColorPaint.setStrokeWidth(mCircleStrokeWidth);
        mProgressColorPaint.setStrokeCap(Paint.Cap.ROUND);

        invalidate();
    }

    public void startUpdateProgress(final float progress) {
        startUpdateProgress(null, progress);
    }

    public void startUpdateProgress(final float progress, final int duration) {
        startUpdateProgress(null, progress, duration);
    }

    public void startUpdateProgress(Animator.AnimatorListener listener, final float progress) {
        startUpdateProgress(listener, progress, DEFAULT_ANIMATION_DURATION);
    }

    public void startUpdateProgress(Animator.AnimatorListener listener, final float progress, final int duration) {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        mObjectAnimator.setDuration(duration);

        if (listener != null) {
            mObjectAnimator.addListener(listener);
        } else {
            mObjectAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationCancel(final Animator animation) {
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    setProgress(progress);
                }

                @Override
                public void onAnimationRepeat(final Animator animation) {
                }

                @Override
                public void onAnimationStart(final Animator animation) {
                }
            });
        }
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
