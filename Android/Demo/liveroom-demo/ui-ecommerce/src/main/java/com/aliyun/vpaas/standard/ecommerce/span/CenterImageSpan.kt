package com.aliyun.vpaas.standard.ecommerce.span

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

/**
 * @author puke
 * @version 2022/5/19
 */
class CenterImageSpan(context: Context, resourceId: Int) :
    ImageSpan(context, resourceId) {

    private var mDrawableRef: WeakReference<Drawable>? = null

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val b: Drawable = getCachedDrawable()!!
        canvas.save()

        val transY = top + (bottom - top) / 2 - b.bounds.height() / 2

        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

    private fun getCachedDrawable(): Drawable? {
        val wr: WeakReference<Drawable>? = mDrawableRef
        var d: Drawable? = null
        if (wr != null) {
            d = wr.get()
        }
        if (d == null) {
            d = drawable
            mDrawableRef = WeakReference(d)
        }
        return d
    }
}