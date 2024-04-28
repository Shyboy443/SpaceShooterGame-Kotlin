package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Explosion(context: Context, var eX: Int, var eY: Int) {
    private val explosion: Array<Bitmap?> = arrayOfNulls(9)
    var explosionFrame: Int = 0

    init {
        explosion[0] = BitmapFactory.decodeResource(context.resources, R.drawable.heart9)
        explosion[1] = BitmapFactory.decodeResource(context.resources, R.drawable.heart8)
        explosion[2] = BitmapFactory.decodeResource(context.resources, R.drawable.heart7)
        explosion[3] = BitmapFactory.decodeResource(context.resources, R.drawable.heart6)
        explosion[4] = BitmapFactory.decodeResource(context.resources, R.drawable.heart5)
        explosion[5] = BitmapFactory.decodeResource(context.resources, R.drawable.heart4)
        explosion[6] = BitmapFactory.decodeResource(context.resources, R.drawable.heart3)
        explosion[7] = BitmapFactory.decodeResource(context.resources, R.drawable.heart2)
        explosion[8] = BitmapFactory.decodeResource(context.resources, R.drawable.heart1)
    }

    fun getExplosion(explosionFrame: Int): Bitmap? {
        return explosion[explosionFrame]
    }
}
