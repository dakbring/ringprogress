package com.pascalwelsch.holocircularprogressbar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

public class RingProgressBar extends View {

    /**
     * used to save the super state on configuration change
     */
    private static final String INSTANCE_STATE_SAVEDSTATE = "saved_state";

    /**
     * TAG constant for logging
     */
    private static final String TAG = RingProgressBar.class.getSimpleName();

    private static final int DEFAULT_ANIMATION_DURATION = 1000;

    /**
     * The Translation offset x which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetX;

    /**
     * The Translation offset y which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetY;

    /**
     * used to draw ring progress
     */
    private ARing mFirst100;

    /**
     * used to draw ring progress
     */
    private ARing mSecond100;

    /**
     * used to draw ring progress
     */
    private ARing mThird100;

    /**
     * The Horizontal inset calcualted in {@link #computeInsets(int, int, int)}
     */
    private int mHorizontalInset = 0;

    /**
     * The Vertical inset calcualted in {@link #computeInsets(int, int, int)}
     */
    private int mVerticalInset = 0;

    /**
     * The gravity of the view. Where should the Circle be drawn within the given bounds
     * <p>
     * {@link #computeInsets(int, int, int)}
     */
    private int mGravity = Gravity.CENTER;

    /**
     * true if not all properties are set. then the view isn't drawn and there are no errors in the
     * LayoutEditor
     */
    private boolean mIsInitializing = true;

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
    public RingProgressBar(final Context context, final AttributeSet attrs,
                           final int defStyle) {
        super(context, attrs, defStyle);

        mFirst100 = new ARing();
        mSecond100 = new ARing();
        mThird100 = new ARing();

        // load the styled attributes and set their properties
        final TypedArray attributes = context
                .obtainStyledAttributes(attrs, R.styleable.RingProgressBar,
                        defStyle, 0);
        if (attributes != null) {
            try {
                setProgressColor(attributes
                        .getColor(R.styleable.RingProgressBar_progress_color, Color.CYAN));
                setProgressBackgroundColor(attributes
                        .getColor(R.styleable.RingProgressBar_progress_background_color,
                                Color.GREEN));
                setProgress(
                        attributes.getFloat(R.styleable.RingProgressBar_progress, 0.0f));
                setMarkerProgress(
                        attributes.getFloat(R.styleable.RingProgressBar_marker_progress,
                                0.0f));
                setWheelSize((int) attributes
                        .getDimension(R.styleable.RingProgressBar_stroke_width, 10));
                setThumbEnabled(attributes
                        .getBoolean(R.styleable.RingProgressBar_thumb_visible, true));
                setMarkerEnabled(attributes
                        .getBoolean(R.styleable.RingProgressBar_marker_visible, true));

                setGravity(attributes
                        .getInt(R.styleable.RingProgressBar_android_gravity,
                                Gravity.CENTER));
            } finally {
                // make sure recycle is always called.
                attributes.recycle();
            }
        }

        mFirst100.initLayout();
        mSecond100.initLayout();
        mThird100.initLayout();
        invalidate();

        // the view has now all properties and can be drawn
        mIsInitializing = false;
    }

    /**
     * Compute insets.
     * <p>
     * <pre>
     *  ______________________
     * |_________dx/2_________|
     * |......| /'''''\|......|
     * |-dx/2-|| View ||-dx/2-|
     * |______| \_____/|______|
     * |________ dx/2_________|
     * </pre>
     *
     * @param dx the dx the horizontal unfilled space
     * @param dy the dy the horizontal unfilled space
     */
    @SuppressLint("NewApi")
    void computeInsets(int layoutDirection, final int dx, final int dy) {
        int absoluteGravity = mGravity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            absoluteGravity = Gravity.getAbsoluteGravity(mGravity, layoutDirection);
        }

        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                mHorizontalInset = 0;
                break;
            case Gravity.RIGHT:
                mHorizontalInset = dx;
                break;
            case Gravity.CENTER_HORIZONTAL:
            default:
                mHorizontalInset = dx / 2;
                break;
        }
        switch (absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                mVerticalInset = 0;
                break;
            case Gravity.BOTTOM:
                mVerticalInset = dy;
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                mVerticalInset = dy / 2;
                break;
        }
    }

    /**
     * set gravity of the View
     * @param gravity
     */
    void setGravity(int gravity) {
        mGravity = gravity;
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        // All of our positions are using our internal coordinate system.
        // Instead of translating
        // them we let Canvas do the work for us.
        canvas.translate(mTranslationOffsetX, mTranslationOffsetY);
        mFirst100.draw(canvas);
        mSecond100.draw(canvas);
        mThird100.draw(canvas);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int height = getDefaultSize(
                getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom(),
                heightMeasureSpec);
        final int width = getDefaultSize(
                getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(),
                widthMeasureSpec);

        final int diameter;
        if (heightMeasureSpec == MeasureSpec.UNSPECIFIED) {
            // ScrollView
            diameter = width;
            computeInsets(getLayoutDirection(),0, 0);
        } else if (widthMeasureSpec == MeasureSpec.UNSPECIFIED) {
            // HorizontalScrollView
            diameter = height;
            computeInsets(getLayoutDirection(),0, 0);
        } else {
            // Default
            diameter = Math.min(width, height);
            computeInsets(getLayoutDirection(),width - diameter, height - diameter);
        }

        setMeasuredDimension(diameter, diameter);

        final float halfWidth = diameter * 0.5f;
        mTranslationOffsetX = halfWidth + mHorizontalInset;
        mTranslationOffsetY = halfWidth + mVerticalInset;

        mFirst100.measure(halfWidth);
        mSecond100.measure(halfWidth * 0.9f);
        mThird100.measure(halfWidth * 0.8f);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mFirst100.restoreInstanceState(bundle);
            mSecond100.restoreInstanceState(bundle);
            mThird100.restoreInstanceState(bundle);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE_SAVEDSTATE));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE_SAVEDSTATE, super.onSaveInstanceState());
        mFirst100.saveInstanceState(bundle);
        mSecond100.saveInstanceState(bundle);
        mThird100.saveInstanceState(bundle);
        return bundle;
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(float progress) {
            mFirst100.setProgress(progress <= 0f ? 0f : progress >= 1f ? 1f : progress % 1f);
            progress -= 1f;
            mSecond100.setProgress(progress <= 0f ? 0f : progress >= 1f ? 1f : progress % 1f);
            progress -= 1f;
            mThird100.setProgress(progress <= 0f ? 0f : progress >= 1f ? 1f : progress % 1f);
        if (!mIsInitializing) {
            invalidate();
        }
    }

    /**
     * Sets the marker progress.
     *
     * @param progress the new marker progress
     */
    public void setMarkerProgress(final float progress) {
        mFirst100.setMarkerProgress(progress);
        mSecond100.setMarkerProgress(progress);
        mThird100.setMarkerProgress(progress);
    }

    /**
     * Sets the progress background color.
     *
     * @param color the new progress background color
     */
    public void setProgressBackgroundColor(final int color) {
        mFirst100.setProgressBackgroundColor(color);
        mSecond100.setProgressBackgroundColor(color);
        mThird100.setProgressBackgroundColor(color);
        invalidate();
    }

    /**
     * Sets the progress color.
     *
     * @param color the new progress color
     */
    public void setProgressColor(final int color) {
        mFirst100.setProgressColor(color);
        mSecond100.setProgressColor(color);
        mThird100.setProgressColor(color);
        invalidate();
    }

    /**
     * shows or hides the thumb of the progress bar
     *
     * @param enabled true to show the thumb
     */
    public void setThumbEnabled(final boolean enabled) {
        mFirst100.setThumbEnabled(enabled);
        mSecond100.setThumbEnabled(enabled);
        mThird100.setThumbEnabled(enabled);
        invalidate();
    }

    /**
     * Sets the wheel size.
     *
     * @param dimension the new wheel size
     */
    public void setWheelSize(final int dimension) {
        mFirst100.setWheelSize(dimension);
        mSecond100.setWheelSize(dimension);
        mThird100.setWheelSize(dimension);
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

    /**
     * Sets the marker enabled.
     *
     * @param enabled the new marker enabled
     */
    public void setMarkerEnabled(final boolean enabled) {
        mFirst100.setMarkerEnabled(enabled);
        mSecond100.setMarkerEnabled(enabled);
        mThird100.setMarkerEnabled(enabled);
    }

    /**
     * gives the current progress of the ProgressBar. Value between 0..3 if you set the progress to
     * >1 you'll get progress % 1 as return value
     *
     * @return the progress
     */
    public float getProgress() {
        return mFirst100.getProgress() + mSecond100.getProgress() + mThird100.getProgress();
    }
}
