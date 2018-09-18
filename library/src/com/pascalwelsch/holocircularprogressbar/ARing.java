package com.pascalwelsch.holocircularprogressbar;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;

public class ARing {

    /**
     * used to save the progress on configuration changes
     */
    private static final String INSTANCE_STATE_PROGRESS = "progress";

    /**
     * used to save the marker progress on configuration changes
     */
    private static final String INSTANCE_STATE_MARKER_PROGRESS = "marker_progress";

    /**
     * used to save the background color of the progress
     */
    private static final String INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR = "progress_background_color";

    /**
     * used to save the color of the progress
     */
    private static final String INSTANCE_STATE_PROGRESS_COLOR = "progress_color";

    /**
     * used to save and restore the visibility of the thumb in this instance
     */
    private static final String INSTANCE_STATE_THUMB_VISIBLE = "thumb_visible";

    /**
     * used to save and restore the visibility of the marker in this instance
     */
    private static final String INSTANCE_STATE_MARKER_VISIBLE = "marker_visible";

    /**
     * The Translation offset x which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetX;

    /**
     * The Translation offset y which gives us the ability to use our own coordinates system.
     */
    private float mTranslationOffsetY;

    /**
     * the overdraw is true if the progress is over 1.0.
     */
    private boolean mOverrdraw = false;

    /**
     * The rectangle enclosing the circle.
     */
    private final RectF mCircleBounds = new RectF();

    /**
     * the paint for the background.
     */
    private Paint mBackgroundColorPaint = new Paint();

    /**
     * paint for the progress.
     */
    private Paint mProgressColorPaint;

    /**
     * flag if the marker should be visible
     */
    private boolean mIsMarkerEnabled = false;

    /**
     * The Thumb pos x.
     * <p>
     * Care. the position is not the position of the rotated thumb. The position is only calculated
     * in {@link #measure(float)}
     */
    private float mThumbPosX;

    /**
     * The pointer width (in pixels).
     */
    private int mThumbRadius = 20;

    /**
     * The Thumb pos y.
     * <p>
     * Care. the position is not the position of the rotated thumb. The position is only calculated
     * in {@link #measure(float)}
     */
    private float mThumbPosY;

    /**
     * The Marker color paint.
     */
    private Paint mMarkerColorPaint;

    /**
     * the rect for the thumb square
     */
    private final RectF mSquareRect = new RectF();

    /**
     * The Thumb color paint.
     */
    private Paint mThumbColorPaint = new Paint();

    /**
     * The stroke width used to paint the circle.
     */
    private int mCircleStrokeWidth = 10;

    /**
     * Radius of the circle
     * <p>
     * <p> Note: (Re)calculated in {@link #measure(float)}. </p>
     */
    private float mRadius;

    /**
     * The Horizontal inset calcualted in {@link #computeInsets(int, int, int)}
     */
    private int mHorizontalInset = 0;

    /**
     * The Vertical inset calcualted in {@link #computeInsets(int, int, int)}
     */
    private int mVerticalInset = 0;

    /**
     * The Marker progress.
     */
    private float mMarkerProgress = 0.0f;

    /**
     * The current progress.
     */
    private float mProgress = 0.3f;

    /**
     * The color of the progress background.
     */
    private int mProgressBackgroundColor;

    /**
     * the color of the progress.
     */
    private int mProgressColor;

    /**
     * The gravity of the view. Where should the Circle be drawn within the given bounds
     * <p>
     * {@link #computeInsets(int, int, int)}
     */
    private int mGravity = Gravity.CENTER;

    /**
     * @return true if the marker is visible
     */
    boolean isThumbEnabled() {
        return mIsThumbEnabled;
    }

    /**
     * indicates if the thumb is visible
     */
    private boolean mIsThumbEnabled = true;

    /**
     * @return true if the marker is visible
     */
    boolean isMarkerEnabled() {
        return mIsMarkerEnabled;
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
     * Gets the marker rotation.
     *
     * @return the marker rotation
     */
    private float getMarkerRotation() {
        return 360 * mMarkerProgress;
    }

    void measure(float halfWidth) {
        // width of the drawed circle (+ the drawedThumb)
        final float drawedWith;
        if (isThumbEnabled()) {
            drawedWith = mThumbRadius * (5f / 6f);
        } else if (isMarkerEnabled()) {
            drawedWith = mCircleStrokeWidth * 1.4f;
        } else {
            drawedWith = mCircleStrokeWidth / 2f;
        }

        // -0.5f for pixel perfect fit inside the viewbounds
        mRadius = halfWidth - drawedWith - 0.5f;

        mCircleBounds.set(-mRadius, -mRadius, mRadius, mRadius);

        mThumbPosX = (float) (mRadius * Math.cos(0));
        mThumbPosY = (float) (mRadius * Math.sin(0));

        mTranslationOffsetX = halfWidth + mHorizontalInset;
        mTranslationOffsetY = halfWidth + mVerticalInset;
    }

    void draw(final Canvas canvas) {
        canvas.translate(mTranslationOffsetX, mTranslationOffsetY);

        final float progressRotation = getCurrentRotation();

        // draw the background
        if (!mOverrdraw) {
            canvas.drawArc(mCircleBounds, 270, -(360 - progressRotation), false,
                    mBackgroundColorPaint);
        }

        // draw the progress or a full circle if overdraw is true
        canvas.drawArc(mCircleBounds, 270, mOverrdraw ? 360 : progressRotation, false,
                mProgressColorPaint);

        // draw the marker at the correct rotated position
        if (mIsMarkerEnabled) {
            final float markerRotation = getMarkerRotation();

            canvas.save();
            canvas.rotate(markerRotation - 90);
            canvas.drawLine((float) (mThumbPosX + mThumbRadius / 2 * 1.4), mThumbPosY,
                    (float) (mThumbPosX - mThumbRadius / 2 * 1.4), mThumbPosY, mMarkerColorPaint);
            canvas.restore();
        }

        if (isThumbEnabled()) {
            // draw the thumb square at the correct rotated position
            canvas.save();
            canvas.rotate(progressRotation - 90);
            // rotate the square by 45 degrees
            canvas.rotate(45, mThumbPosX, mThumbPosY);
            mSquareRect.left = mThumbPosX - mThumbRadius / 3;
            mSquareRect.right = mThumbPosX + mThumbRadius / 3;
            mSquareRect.top = mThumbPosY - mThumbRadius / 3;
            mSquareRect.bottom = mThumbPosY + mThumbRadius / 3;
            canvas.drawRect(mSquareRect, mThumbColorPaint);
            canvas.restore();
        }
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
     * updates the paint of the background
     */
    private void updateBackgroundColor() {
        mBackgroundColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundColorPaint.setColor(mProgressBackgroundColor);
        mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
        mBackgroundColorPaint.setStrokeWidth(mCircleStrokeWidth);
    }

    /**
     * updates the paint of the marker
     */
    private void updateMarkerColor() {
        mMarkerColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerColorPaint.setColor(mProgressBackgroundColor);
        mMarkerColorPaint.setStyle(Paint.Style.STROKE);
        mMarkerColorPaint.setStrokeWidth(mCircleStrokeWidth / 2);
    }

    /**
     * updates the paint of the progress and the thumb to give them a new visual style
     */
    private void updateProgressColor() {
        mProgressColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressColorPaint.setColor(mProgressColor);
        mProgressColorPaint.setStyle(Paint.Style.STROKE);
        mProgressColorPaint.setStrokeWidth(mCircleStrokeWidth);

        mThumbColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbColorPaint.setColor(mProgressColor);
        mThumbColorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mThumbColorPaint.setStrokeWidth(mCircleStrokeWidth);
    }

    int getCircleStrokeWidth() {
        return mCircleStrokeWidth;
    }

    /**
     * similar to {@link #getProgress}
     */
    float getMarkerProgress() {
        return mMarkerProgress;
    }

    /**
     * gives the current progress of the ProgressBar. Value between 0..1 if you set the progress to
     * >1 you'll get progress % 1 as return value
     *
     * @return the progress
     */
    float getProgress() {
        return mProgress;
    }

    /**
     * Gets the progress color.
     *
     * @return the progress color
     */
    int getProgressColor() {
        return mProgressColor;
    }

    /**
     * Sets the marker enabled.
     *
     * @param enabled the new marker enabled
     */
    void setMarkerEnabled(final boolean enabled) {
        mIsMarkerEnabled = enabled;
    }

    /**
     * Sets the marker progress.
     *
     * @param progress the new marker progress
     */
    void setMarkerProgress(final float progress) {
        mIsMarkerEnabled = true;
        mMarkerProgress = progress;
    }

    /**
     * Sets the progress color.
     *
     * @param color the new progress color
     */
    void setProgressColor(final int color) {
        mProgressColor = color;

        updateProgressColor();
    }

    /**
     * Sets the progress background color.
     *
     * @param color the new progress background color
     */
    void setProgressBackgroundColor(final int color) {
        mProgressBackgroundColor = color;

        updateMarkerColor();
        updateBackgroundColor();
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    void setProgress(final float progress) {
        if (progress == mProgress) {
            return;
        }

        if (progress == 1) {
            mOverrdraw = false;
            mProgress = 1;
        } else {

            if (progress >= 1) {
                mOverrdraw = true;
            } else {
                mOverrdraw = false;
            }

            mProgress = progress % 1.0f;
        }
    }

    /**
     * Sets the wheel size.
     *
     * @param dimension the new wheel size
     */
    void setWheelSize(final int dimension) {
        mCircleStrokeWidth = dimension;

        // update the paints
        updateBackgroundColor();
        updateMarkerColor();
        updateProgressColor();
    }

    /**
     * shows or hides the thumb of the progress bar
     *
     * @param enabled true to show the thumb
     */
    void setThumbEnabled(final boolean enabled) {
        mIsThumbEnabled = enabled;
    }

    /**
     * set gravity of the View
     * @param gravity
     */
    void setGravity(int gravity) {
        mGravity = gravity;
    }

    /**
     * update progress layout
     */
    void initLayout() {
        mThumbRadius = mCircleStrokeWidth * 2;

        updateBackgroundColor();

        updateMarkerColor();

        updateProgressColor();
    }

    void restoreInstanceState(Bundle bundle) {
        setProgress(bundle.getFloat(INSTANCE_STATE_PROGRESS));
        setMarkerProgress(bundle.getFloat(INSTANCE_STATE_MARKER_PROGRESS));

        final int progressColor = bundle.getInt(INSTANCE_STATE_PROGRESS_COLOR);
        if (progressColor != mProgressColor) {
            mProgressColor = progressColor;
            updateProgressColor();
        }

        final int progressBackgroundColor = bundle
                .getInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR);
        if (progressBackgroundColor != mProgressBackgroundColor) {
            mProgressBackgroundColor = progressBackgroundColor;
            updateBackgroundColor();
        }

        mIsThumbEnabled = bundle.getBoolean(INSTANCE_STATE_THUMB_VISIBLE);

        mIsMarkerEnabled = bundle.getBoolean(INSTANCE_STATE_MARKER_VISIBLE);
    }

    void saveInstanceState(Bundle bundle) {
        bundle.putFloat(INSTANCE_STATE_PROGRESS, mProgress);
        bundle.putFloat(INSTANCE_STATE_MARKER_PROGRESS, mMarkerProgress);
        bundle.putInt(INSTANCE_STATE_PROGRESS_COLOR, mProgressColor);
        bundle.putInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR, mProgressBackgroundColor);
        bundle.putBoolean(INSTANCE_STATE_THUMB_VISIBLE, mIsThumbEnabled);
        bundle.putBoolean(INSTANCE_STATE_MARKER_VISIBLE, mIsMarkerEnabled);
    }
}