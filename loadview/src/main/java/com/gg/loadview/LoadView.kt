package com.gg.loadview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ViewAnimator

/**
 *  Create by GG on 2018/12/29
 *  mail is gg.jin.yu@gmail.com
 */
class LoadView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    companion object {

        const val DURATION_TIME = 2000L

        // 当前大圆旋转的角度（弧度）
        private var mCurrentRotationAngle = 0f

        // 小圆的颜色列表
        private lateinit var mCircleColors: IntArray
        // 大圆里面包含很多小圆的半径 - 整宽度的 1/4
        private var mRotationRadius: Float = 0.toFloat()
        // 每个小圆的半径 - 大圆半径的 1/8
        private var mCircleRadius: Float = 0.toFloat()
        // 当前大圆的半径
        private var mCurrentRotationRadius: Float = mRotationRadius

        private var mCenterX = 0F
        private var mCenterY = 0F
        private val mPaint: Paint by lazy {
            Paint().apply {
                isDither = true
                isAntiAlias = true
            }
        }

        var mCurrentAngle: Double = 0.0

    }


    init {
        mCircleColors = context.resources.getIntArray(R.array.splash_circle_colors)
        mCurrentAngle = Math.PI * 2 / mCircleColors.size
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mCenterX = measuredWidth / 2f
        mCenterY = measuredHeight / 2f

        mRotationRadius = measuredWidth / 4f
        mCircleRadius = mRotationRadius / 8
    }

    private lateinit var mLoadState: LoadState

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        if (!this::mLoadState.isInitialized) {
            mLoadState = RotationState(this)
        }

        mLoadState.draw(canvas)
    }


    fun disappear() {
        if (this::mLoadState.isInitialized) {
            if (mLoadState is RotationState) {
                (mLoadState as RotationState).disappear()
                mLoadState = MergeState(this)
            }
        }

    }


    private class RotationState(val view: View) : LoadState() {

        private val animator: ValueAnimator by lazy {
            ObjectAnimator.ofFloat(0f, 2f * Math.PI.toFloat()).apply {
                duration = DURATION_TIME

                addUpdateListener {
                    mCurrentRotationAngle = it.animatedValue as Float
                    view.invalidate()
                }
                interpolator = LinearInterpolator()
                repeatCount = -1
            }
        }

        init {
            animator.start()
        }

        override fun draw(canvas: Canvas?) {
            canvas?.drawColor(Color.WHITE)

//            Log.w("mRotationRadius", mRotationRadius.toString())
//            Log.w("mCurrentAngle", mCurrentAngle.toString())

            mCircleColors.forEachIndexed { index, i ->
                val cx = mCenterX + Math.cos(mCurrentRotationAngle + mCurrentAngle * index).toFloat() * mRotationRadius
                val cy = mCenterY + Math.sin(mCurrentRotationAngle + mCurrentAngle * index).toFloat() * mRotationRadius
                mPaint.color = i
//                Log.w("cx", cx.toString())
//                Log.w("cy", cy.toString())
                canvas?.drawCircle(cx, cy, mCircleRadius, mPaint)
            }

        }


        fun disappear() {
            animator.end()
        }
    }

    class MergeState(val view: View) : LoadState() {

        private val animator: ValueAnimator  by lazy {
            ObjectAnimator.ofFloat(mRotationRadius, 0f).apply {
                duration = DURATION_TIME / 2
                addUpdateListener {
                    mCurrentRotationRadius = it.animatedValue as Float
                    view.invalidate()
                }

            }
        }

        init {
            animator.start()
        }


        override fun draw(canvas: Canvas?) {

        }

    }

    abstract class LoadState {
        abstract fun draw(canvas: Canvas?)
    }

}