package com.gg.loadview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import android.view.animation.AnticipateInterpolator
import android.view.animation.LinearInterpolator

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

        var mLoadState: LoadState? = null

        // 屏幕对角线的一半
        private var mDiagonalDist: Float = 0.toFloat()

        // 空心圆初始半径
        private var mHoleRadius = 0f

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

        mDiagonalDist = Math.sqrt(mCenterX * mCenterX + mCenterY * mCenterY.toDouble()).toFloat()
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        if (mLoadState == null) {
            mLoadState = RotationState(this)
        }

        mLoadState?.draw(canvas)
    }


    fun disappear() {
        if (mLoadState != null) {
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
                interpolator = AnticipateInterpolator(5f)

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mLoadState = ExpendState(view)
                    }
                })
            }
        }

        init {
            animator.start()
        }


        override fun draw(canvas: Canvas?) {
            canvas?.drawColor(Color.WHITE)
            mCircleColors.forEachIndexed { index, i ->
                val cx = mCenterX + Math.cos(mCurrentAngle * index).toFloat() * mCurrentRotationRadius
                val cy = mCenterY + Math.sin(mCurrentAngle * index).toFloat() * mCurrentRotationRadius
                mPaint.color = i
//                Log.w("cx", cx.toString())
//                Log.w("cy", cy.toString())
                canvas?.drawCircle(cx, cy, mCircleRadius, mPaint)
            }

        }

    }


    class ExpendState(val view: View) : LoadState() {

        private val animator: ValueAnimator by lazy {
            ObjectAnimator.ofFloat(0f, mDiagonalDist).apply {
                duration = DURATION_TIME
                addUpdateListener {
                    mHoleRadius = it.animatedValue as Float
                    view.invalidate()
                }

            }
        }

        init {
            animator.start()
        }

        override fun draw(canvas: Canvas?) {
            // 画笔的宽度
            val strokeWidth = mDiagonalDist - mHoleRadius
            mPaint.strokeWidth = strokeWidth
            mPaint.color = Color.WHITE
            mPaint.style = Paint.Style.STROKE
            Log.e("TAG", "mHoleRadius -> $mHoleRadius")
            val radius = strokeWidth / 2 + mHoleRadius
            // 绘制一个圆
            canvas?.drawCircle(mCenterX, mCenterY, radius, mPaint)
        }

    }


    abstract class LoadState {
        abstract fun draw(canvas: Canvas?)
    }

}