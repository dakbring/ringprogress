package com.pascalwelsch.holocircularprogressbar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
     * used to draw ring progress
     */
    private ARing mARing;

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

        mARing = new ARing();

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

        mARing.initLayout();
        invalidate();

        // the view has now all properties and can be drawn
        mIsInitializing = false;
    }

    /**
     * set gravity of the progress
     * @param gravity
     */
    private void setGravity(int gravity) {
        mARing.setGravity(gravity);
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        // All of our positions are using our internal coordinate system.
        // Instead of translating
        // them we let Canvas do the work for us.

        mARing.draw(canvas);
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
            mARing.computeInsets(getLayoutDirection(),0, 0);
        } else if (widthMeasureSpec == MeasureSpec.UNSPECIFIED) {
            // HorizontalScrollView
            diameter = height;
            mARing.computeInsets(getLayoutDirection(),0, 0);
        } else {
            // Default
            diameter = Math.min(width, height);
            mARing.computeInsets(getLayoutDirection(),width - diameter, height - diameter);
        }

        setMeasuredDimension(diameter, diameter);

        final float halfWidth = diameter * 0.5f;
        mARing.measure(halfWidth);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mARing.restoreInstanceState(bundle);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE_SAVEDSTATE));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE_SAVEDSTATE, super.onSaveInstanceState());
        mARing.saveInstanceState(bundle);
        return bundle;
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(final float progress) {
        mARing.setProgress(progress);

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
        mARing.setMarkerProgress(progress);
    }

    /**
     * Sets the progress background color.
     *
     * @param color the new progress background color
     */
    public void setProgressBackgroundColor(final int color) {
        mARing.setProgressBackgroundColor(color);
        invalidate();
    }

    /**
     * Sets the progress color.
     *
     * @param color the new progress color
     */
    public void setProgressColor(final int color) {
        mARing.setProgressColor(color);
        invalidate();
    }

    /**
     * shows or hides the thumb of the progress bar
     *
     * @param enabled true to show the thumb
     */
    public void setThumbEnabled(final boolean enabled) {
        mARing.setThumbEnabled(enabled);
    }

    /**
     * Sets the wheel size.
     *
     * @param dimension the new wheel size
     */
    public void setWheelSize(final int dimension) {
        mARing.setWheelSize(dimension);
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
        mARing.setMarkerEnabled(enabled);
    }

    /**
     * gives the current progress of the ProgressBar. Value between 0..1 if you set the progress to
     * >1 you'll get progress % 1 as return value
     *
     * @return the progress
     */
    public float getProgress() {
        return mARing.getProgress();
    }
}
