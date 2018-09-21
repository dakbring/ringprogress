package com.pascalwelsch.holocircularprogressbar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View


class RingProgressBar : View {

    private val mCircleBounds_1 = RectF()
    private val mCircleBounds_2 = RectF()
    private val mCircleBounds_3 = RectF()

    private var mRingStrokeWidth: Int = 0

    private var mIsInitializing = true

    private var mProgress: Float = 0f

    private var mProgressBackgroundColor: Int = 0

    private var mProgressColor: Int = 0

    private var mBackgroundColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mProgressColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mGradientColorPaint_1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mGradientColorPaint_2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mGradientColorPaint_3 = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mTranslationOffsetX: Float = 0f
    private var mTranslationOffsetY: Float = 0f

    private var mIcon: Bitmap? = null

    private var mIconBig: Bitmap? = null

    private var mIconSize: Int = 0

    private var mMaxProgress: Float = 0f

    private var mObjectAnimator: ObjectAnimator? = null

    private var mReachAnimateState = REACH_ANIMATE_STATE_NONE

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar, defStyleAttr, 0)
        if (attributes != null) {
            try {
                setProgressColor(attributes.getColor(R.styleable.RingProgressBar_ring_progress_color, Color.CYAN))
                setProgressBackgroundColor(attributes.getColor(R.styleable.RingProgressBar_ring_progress_background_color, Color.GREEN))
                setProgress(attributes.getFloat(R.styleable.RingProgressBar_progress, 0.0f))
                setStrokeWidth(attributes.getDimension(R.styleable.RingProgressBar_stroke_width, DEFAULT_STROKE_WIDTH.toFloat()).toInt())
                setIconSize(attributes.getDimension(R.styleable.RingProgressBar_icon_size, DEFAULT_ICON_SIZE.toFloat()).toInt())
                setMaxProgress(attributes.getFloat(R.styleable.RingProgressBar_max_progress, 1f))

                val icon = BitmapFactory.decodeResource(resources, attributes.getResourceId(R.styleable.RingProgressBar_icon_source, 0))
                mIcon = Bitmap.createScaledBitmap(icon, mIconSize, mIconSize, false)
                mIconBig = Bitmap.createScaledBitmap(mIcon, (mIconSize * 1.15).toInt(), (mIconSize * 1.15).toInt(), false)
            } finally {
                // make sure recycle is always called.
                attributes.recycle()
            }
        }

        updateBackgroundColor()
        updateProgressColor()

        // the view has all the properties and ready to be draw
        mIsInitializing = false
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(mTranslationOffsetX, mTranslationOffsetY)

        val progressRotation = mProgress * 360

        canvas.drawArc(mCircleBounds_1, 270f, -(360 - progressRotation), false, mBackgroundColorPaint)

        canvas.drawArc(mCircleBounds_1, 270f, if (mProgress > 1) 360f else progressRotation, false, mProgressColorPaint)

        if (mProgress > MAX_PROGRESS_ONE_CIRCLE) {
            canvas.drawArc(mCircleBounds_2, 270f, if (mProgress > 2) 360f else progressRotation - 360, false, mProgressColorPaint)
        }
        if (mProgress > MAX_PROGRESS_TWO_CIRCLE) {
            canvas.drawArc(mCircleBounds_3, 270f, if (mProgress == 3f) 360f else progressRotation - 720, false, mProgressColorPaint)
        }

        drawGradient(canvas, progressRotation)

        canvas.translate(0f, -mTranslationOffsetY)
        drawIcon(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val diameter = if (widthMeasureSpec > heightMeasureSpec) widthMeasureSpec else heightMeasureSpec
        val radius = View.getDefaultSize(suggestedMinimumWidth + paddingLeft + paddingRight, diameter)
        setMeasuredDimension(radius, radius)

        val halfWidth = (radius / 2).toFloat()

        val ringWith = mRingStrokeWidth * 1.1f

        val radius1 = halfWidth - mIconSize / 2
        val radius2 = radius1 - ringWith
        val radius3 = radius2 - ringWith

        mCircleBounds_1.set(-radius1, -radius1, radius1, radius1)
        mCircleBounds_2.set(-radius2, -radius2, radius2, radius2)
        mCircleBounds_3.set(-radius3, -radius3, radius3, radius3)

        mTranslationOffsetX = halfWidth
        mTranslationOffsetY = halfWidth
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            setProgress(state.getFloat(INSTANCE_STATE_PROGRESS))

            val progressColor = state.getInt(INSTANCE_STATE_PROGRESS_COLOR)
            if (progressColor != mProgressColor) {
                mProgressColor = progressColor
                updateProgressColor()
            }

            val progressBackgroundColor = state.getInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR)
            if (progressBackgroundColor != mProgressBackgroundColor) {
                mProgressBackgroundColor = progressBackgroundColor
                updateBackgroundColor()
            }
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE_SAVED_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE_SAVED_STATE, super.onSaveInstanceState())
        bundle.putFloat(INSTANCE_STATE_PROGRESS, mProgress)
        bundle.putInt(INSTANCE_STATE_PROGRESS_COLOR, mProgressColor)
        bundle.putInt(INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR, mProgressBackgroundColor)
        return bundle
    }

    fun getColorWithAlpha(yourColor: Int, alpha: Int): Int {
        val red = Color.red(yourColor)
        val blue = Color.blue(yourColor)
        val green = Color.green(yourColor)
        return Color.argb(alpha, red, green, blue)
    }

    fun setProgress(progress: Float) {
        if (progress == mProgress) {
            return
        }

        mMaxProgress = if (mMaxProgress < MAX_PROGRESS_THREE_CIRCLE) mMaxProgress else MAX_PROGRESS_THREE_CIRCLE
        mProgress = if (progress < mMaxProgress) progress else mMaxProgress

        if (!mIsInitializing) {
            invalidate()
        }
    }

    fun getProgress() = mProgress

    fun setProgressBackgroundColor(color: Int) {
        mProgressBackgroundColor = color
        updateBackgroundColor()
    }

    fun setProgressColor(color: Int) {
        mProgressColor = color
        updateProgressColor()
    }

    fun setStrokeWidth(dimension: Int) {
        mRingStrokeWidth = dimension
        updateBackgroundColor()
        updateProgressColor()
    }

    fun setMaxProgress(maxProgress: Float) {
        mMaxProgress = maxProgress
        invalidate()
    }

    fun setIconSize(iconSize: Int) {
        mIconSize = iconSize
        invalidate()
    }

    private fun updateBackgroundColor() {
        updatePaintColor(mBackgroundColorPaint, mProgressBackgroundColor, mRingStrokeWidth.toFloat())
    }

    private fun updateProgressColor() {
        updatePaintColor(mProgressColorPaint, mProgressColor, mRingStrokeWidth.toFloat())

        updatePaintColor(mGradientColorPaint_1, getColorWithAlpha(mProgressColor, 80), mRingStrokeWidth * 1.7f)
        updatePaintColor(mGradientColorPaint_2, getColorWithAlpha(mProgressColor, 50), mRingStrokeWidth * 2.3f)
        updatePaintColor(mGradientColorPaint_3, getColorWithAlpha(mProgressColor, 20), mRingStrokeWidth * 3.5f)
    }

    private fun updatePaintColor(paint: Paint, color: Int, strokeWidth: Float) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = strokeWidth

        invalidate()
    }

    private fun drawGradient(canvas: Canvas, progressRotation: Float) {
        val paints = when (mReachAnimateState) {
            REACH_ANIMATE_STATE_GRADIENT_1 -> {
                arrayOf(mGradientColorPaint_1)
            }
            REACH_ANIMATE_STATE_GRADIENT_2 -> {
                arrayOf(mGradientColorPaint_2, mGradientColorPaint_1)
            }
            REACH_ANIMATE_STATE_GRADIENT_3 -> {
                arrayOf(mGradientColorPaint_3, mGradientColorPaint_2, mGradientColorPaint_1)
            }
            else -> {
                arrayOf()
            }
        }
        for (paint in paints) {
            canvas.drawArc(mCircleBounds_1, 270f, if (mProgress > 1) 360f else progressRotation, false, paint)
        }
    }

    private fun drawIcon(canvas: Canvas) {
        if (mReachAnimateState == REACH_ANIMATE_STATE_ICON) {
            canvas.drawBitmap(mIconBig!!, (-mIconBig!!.width / 2).toFloat(), 0f, null)
        } else {
            canvas.drawBitmap(mIcon!!, (-mIcon!!.width / 2).toFloat(), mIconSize * 0.1f, null)
        }
    }

    fun startUpdateProgress(progress: Float) {
        startUpdateProgress(progress, DEFAULT_ANIMATION_DURATION)
    }

    fun startUpdateProgress(progress: Float, duration: Int) {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "progress", progress)
        mObjectAnimator!!.duration = duration.toLong()

        mObjectAnimator!!.reverse()
        mObjectAnimator!!.addUpdateListener { animation ->
            setProgress(animation.animatedValue as Float)
        }
        mObjectAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (mProgress >= 1 && mReachAnimateState == REACH_ANIMATE_STATE_NONE) {
                    runReachGoalAnimate()
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        })
        mObjectAnimator!!.start()
    }

    fun stopUpdateProgress() {
        if (mObjectAnimator != null) {
            mObjectAnimator!!.cancel()
        }
    }

    private fun runReachGoalAnimate() {
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
                createScaleIconAnimator(170),
                startGradient(70, REACH_ANIMATE_STATE_GRADIENT_1),
                startGradient(70, REACH_ANIMATE_STATE_GRADIENT_2),
                startGradient(150, REACH_ANIMATE_STATE_GRADIENT_3),
                startGradient(70, REACH_ANIMATE_STATE_GRADIENT_2),
                startGradient(70, REACH_ANIMATE_STATE_GRADIENT_1),
                startGradient(70, REACH_ANIMATE_STATE_NONE)
        )
        animatorSet.start()
    }

    private fun createScaleIconAnimator(duration: Long): ObjectAnimator {
        val scaleAnimator = ObjectAnimator.ofFloat(this, "scale", 1f)
        scaleAnimator.duration = duration
        scaleAnimator.addUpdateListener { _ ->
            invalidate()
        }
        scaleAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                mReachAnimateState = REACH_ANIMATE_STATE_ICON
            }

            override fun onAnimationEnd(animator: Animator) {
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        return scaleAnimator
    }

    private fun startGradient(duration: Long, state: Int): ObjectAnimator {
        val gradientAnimator = ObjectAnimator.ofInt(this, "gradient", 0)
        gradientAnimator.duration = duration
        gradientAnimator.startDelay = 33
        gradientAnimator.addUpdateListener { _ ->
            invalidate()
        }
        gradientAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                mReachAnimateState = state
            }

            override fun onAnimationEnd(animator: Animator) {
                invalidate()
            }

            override fun onAnimationCancel(animator: Animator) {

            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })
        return gradientAnimator
    }

    companion object {

        private val TAG = RingProgressBar::class.java.simpleName

        private val INSTANCE_STATE_SAVED_STATE = "saved_state"
        private val INSTANCE_STATE_PROGRESS = "mProgress"
        private val INSTANCE_STATE_PROGRESS_COLOR = "progress_color"
        private val INSTANCE_STATE_PROGRESS_BACKGROUND_COLOR = "progress_background_color"

        private val DEFAULT_ANIMATION_DURATION = 1000

        private val DEFAULT_ICON_SIZE = 50
        private val DEFAULT_STROKE_WIDTH = 10

        private val MAX_PROGRESS_ONE_CIRCLE = 1f
        private val MAX_PROGRESS_TWO_CIRCLE = 2f
        private val MAX_PROGRESS_THREE_CIRCLE = 3f

        private val REACH_ANIMATE_STATE_NONE = 0
        private val REACH_ANIMATE_STATE_ICON = 1
        private val REACH_ANIMATE_STATE_GRADIENT_1 = 2
        private val REACH_ANIMATE_STATE_GRADIENT_2 = 3
        private val REACH_ANIMATE_STATE_GRADIENT_3 = 4
    }
}
