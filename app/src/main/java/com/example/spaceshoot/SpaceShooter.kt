package com.example.spaceshoot

import EnemySpaceship
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import android.os.CountDownTimer
import android.media.MediaPlayer

@Suppress("DEPRECATION")
class SpaceShooter(context: Context) : View(context) {
    private var gunmediaPlayer: MediaPlayer? = null
    private var explosionmediaPlayer: MediaPlayer? = null
    private var pauseButtonRect: Rect? = null
    private var pauseButtonText = "Pause"
    private val pauseButtonPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
    }


    private var background: Bitmap
    private var lifeImage: Bitmap
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    val UPDATE_MILLIS: Long = 30
    companion object {
        var screenWidth: Int = 0
        var screenHeight: Int = 0
    }


    private var enemyShotInterval: Long = 2000 // Initial shot interval in milliseconds
    private var bulletSpeed: Int = 15 // Initial bullet speed


    private var gameTimer: CountDownTimer? = null
    private var gameTimeInSeconds: Long = 0
    private var points: Int = 0
    private var life: Int = 3
    private var scorePaint: Paint? = null
    private var TEXT_SIZE = 45
    private var paused = false
    private var ourSpaceship: OurSpaceship? = null
    private var enemySpaceship: EnemySpaceship? = null
    private var enemyShots: ArrayList<Shot>? = null
    private var ourShots: ArrayList<Shot?>? = null
    private var random: Random? = null
    private var explosion: Explosion? = null
    private var explosions: ArrayList<Explosion>? = null
    private var enemyShotAction = false
    private val runnable = Runnable { invalidate() }
    private var gameOver = false
    private var isHitThisFrame = false





    init {
        explosionmediaPlayer = MediaPlayer.create(context, R.raw.explosion)
        gunmediaPlayer = MediaPlayer.create(context, R.raw.gunshot)
        val display: Display = (context as AppCompatActivity).windowManager.defaultDisplay
        val size = Point().apply { display.getSize(this) }
        screenWidth = size.x
        screenHeight = size.y
        random = Random()
        enemyShots = ArrayList()
        ourShots = ArrayList()
        explosions = ArrayList()
        ourSpaceship = OurSpaceship(context)
        enemySpaceship = EnemySpaceship(context)
        mHandler= Handler()
        background = BitmapFactory.decodeResource(context.resources, R.drawable.background)
        lifeImage = BitmapFactory.decodeResource(context.resources, R.drawable.life)
        scorePaint = Paint()
        scorePaint!!.color = Color.RED
        scorePaint!!.textSize = TEXT_SIZE.toFloat()
        scorePaint!!.textAlign = Paint.Align.LEFT
        startGameTimer()

        val buttonWidth = 100
        val buttonHeight = 100
        val buttonRight = screenWidth - 30  // 30 pixels padding from the right edge
        val buttonBottom = screenHeight - 30  // 30 pixels padding from the bottom edge
        val buttonLeft = buttonRight - buttonWidth
        val buttonTop = buttonBottom - buttonHeight

        pauseButtonRect = Rect(buttonLeft, buttonTop, buttonRight, buttonBottom)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Release MediaPlayer when the view is detached from the window
        gunmediaPlayer?.release()
        explosionmediaPlayer?.release()
        explosionmediaPlayer = null
        gunmediaPlayer = null
    }


    private fun startGameTimer() {
        gameTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) { // Update every second
            override fun onTick(millisUntilFinished: Long) {
                gameTimeInSeconds++
                if ((gameTimeInSeconds % 5).toInt() == 0) { // Every 30 seconds
                    enemyShotInterval -= 200 // Decrease shot interval by 200 ms
                    bulletSpeed += 2 // Increase bullet speed by 2
                    if (enemyShotInterval < 500) { // Maintain a reasonable minimum interval
                        enemyShotInterval = 500
                    }
                    if (bulletSpeed > 70) { // Cap the maximum bullet speed
                        bulletSpeed = 70
                    }
                }
            }

            override fun onFinish() {

            }
        }
        gameTimer?.start()
    }



    private fun transitionToGameOver(message: String) {
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        intent.putExtra("message", message)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        isHitThisFrame = false
        // Draw background, Points and life on Canvas
        canvas.drawBitmap(background, 0f, 0f, null)

        canvas.drawText("Score: $points", 0f, TEXT_SIZE.toFloat(), scorePaint!!)
        for (i in life downTo 1) {
            canvas.drawBitmap(lifeImage, (screenWidth - lifeImage.width * i).toFloat(), 0f, null)
        }

        val gameTimeText = "Time: ${formatTime(gameTimeInSeconds)}"
        val textX = TEXT_SIZE.toFloat()-40  // Adjust the X coordinate to leave some space from the left edge
        val textY = (screenHeight - TEXT_SIZE / 2).toFloat()  // Adjust the Y coordinate to position it near the bottom
        canvas.drawText(gameTimeText, textX, textY, scorePaint!!)


        if (pauseButtonRect == null) {
            pauseButtonRect = Rect(50, 50, 350, 150)  // Adjust size and position as needed
        }

        // Draw the button
        pauseButtonRect?.let {
            val textBounds = Rect()
            pauseButtonPaint.getTextBounds(pauseButtonText, 0, pauseButtonText.length, textBounds)
            val x = it.left + (it.width() - textBounds.width()) / 2
            val y = it.top + (it.height() + textBounds.height()) / 2 - textBounds.bottom  // Center vertically
            canvas.drawText(pauseButtonText, x.toFloat(), y.toFloat(), pauseButtonPaint)
        }

        tryShootingEnemyBullet()
        if (life <= 0 && !gameOver) {
            gameOver = true
            paused = true
            mHandler.removeCallbacks(runnable)
            val message =
                if(points>=10) "Excellent Job"
                else if (points >= 5) "Good Job"
                   else "Good luck next time"
            transitionToGameOver(message)
        }
        // Move enemySpaceship
        enemySpaceship!!.ex += enemySpaceship!!.enemyVelocity
        // If enemySpaceship collides with right wall, reverse enemyVelocity
        if (enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() >= screenWidth) {
            enemySpaceship!!.enemyVelocity *= -1
        }
        // If enemySpaceship collides with left wall, again reverse enemyVelocity
        if (enemySpaceship!!.ex <= 0) {
            enemySpaceship!!.enemyVelocity *= -1
        }
        // Till enemyShotAction is false, enemy should fire shots from random travelled distance
        if (!enemyShotAction) {
            if (enemySpaceship!!.ex >= 200 + random!!.nextInt(400) && enemySpaceship!!.ey >= 200 + random!!.nextInt(400)) {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
                // We're making enemyShotAction to true so that enemy can take a shot at a time
                enemyShotAction = true
            }
            enemyShotAction = if (enemySpaceship!!.ex >= 400 + random!!.nextInt(800) && enemySpaceship!!.ey >= 400 + random!!.nextInt(800)) {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
                // We're making enemyShotAction to true so that enemy can take a shot at a time
                true
            } else {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
                // We're making enemyShotAction to true so that enemy can take a shot at a time
                true
            }
        }
        // Draw the enemy Spaceship
        canvas.drawBitmap(
            enemySpaceship!!.getEnemySpaceshipBitmap()!!,
            enemySpaceship!!.ex.toFloat(),
            enemySpaceship!!.ey.toFloat(),
            null
        )
        // Draw our spaceship between the left and right edge of the screen
        if (ourSpaceship!!.ox > screenWidth - ourSpaceship!!.getOurSpaceshipWidth()) {
            ourSpaceship!!.ox = screenWidth - ourSpaceship!!.getOurSpaceshipWidth()
        } else if (ourSpaceship!!.ox < 0) {
            ourSpaceship!!.ox = 0
        }
        // Draw our Spaceship
        canvas.drawBitmap(
            ourSpaceship!!.getOurSpaceshipBitmap()!!,
            ourSpaceship!!.ox.toFloat(),
            ourSpaceship!!.oy.toFloat(),
            null
        )
        
        // Draw the enemy shot downwards our spaceship and if it's being hit, decrement life, remove
        // the shot object from enemyShots ArrayList and show an explosion.
        // Else if, it goes away through the bottom edge of the screen also remove
        // the shot object from enemyShots.
        // When there is no enemyShots no the screen, change enemyShotAction to false, so that enemy
        // can shot.
        val iterator = enemyShots!!.iterator()
        while (iterator.hasNext()) {
            val enemyShot = iterator.next()
            // Use bulletSpeed to determine how fast the shot moves down the screen
            enemyShot.shy += bulletSpeed  // bulletSpeed increases over time as the game progresses
            canvas.drawBitmap(enemyShot.getShot(), enemyShot.shx.toFloat(), enemyShot.shy.toFloat(), null)

            // Check for collisions with our spaceship
            if (!isHitThisFrame && enemyShot.shx >= ourSpaceship!!.ox && enemyShot.shx <= ourSpaceship!!.ox + ourSpaceship!!.getOurSpaceshipWidth() &&

                enemyShot.shy >= ourSpaceship!!.oy && enemyShot.shy <= ourSpaceship!!.oy + ourSpaceship!!.getOurSpaceshipHeight()) {
                explosionmediaPlayer?.start()
                life--
                isHitThisFrame = true  // Set this flag to true to prevent additional hits in this frame
                iterator.remove()  // Remove the shot from the list
                explosion = Explosion(context, ourSpaceship!!.ox, ourSpaceship!!.oy)
                explosions!!.add(explosion!!)
                continue  // Continue to the next iteration to avoid skipping the check for empty list below
            }

            // Remove the shot if it moves beyond the bottom of the screen
            if (enemyShot.shy >= screenHeight) {
                iterator.remove()
            }
        }

// If there are no more enemy shots on the screen, reset the enemyShotAction to allow new shots
        if (enemyShots!!.isEmpty()) {
            enemyShotAction = false
        }

        // Draw our spaceship shots towards the enemy. If there is a collision between our shot and enemy
        // spaceship, increment points, remove the shot from ourShots and create a new Explosion object.
        // Else if, our shot goes away through the top edge of the screen also remove
        // the shot object from enemyShots ArrayList.
        for (i in ourShots!!.indices) {
            ourShots!![i]!!.shy -= 15
            canvas.drawBitmap(
                ourShots!![i]!!.getShot(),
                ourShots!![i]!!.shx.toFloat(),
                ourShots!![i]!!.shy.toFloat(),
                null
            )
            if (ourShots!![i]!!.shx >= enemySpaceship!!.ex &&
                ourShots!![i]!!.shx <= enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() &&
                ourShots!![i]!!.shy >= enemySpaceship!!.ey &&
                ourShots!![i]!!.shy <= enemySpaceship!!.ey + enemySpaceship!!.getEnemySpaceshipHeight()) {
                // Collision detected
                points++
                ourShots!!.removeAt(i)
                explosion = Explosion(context, enemySpaceship!!.ex, enemySpaceship!!.ey)
                explosions!!.add(explosion!!)
            } else if (ourShots!![i]!!.shy <= 0) {
                // Our shot goes beyond the top edge of the screen
                ourShots!!.removeAt(i)
            }
        }
        // Do the explosion
        for (i in explosions!!.indices.reversed()) {
            canvas.drawBitmap(
                explosions!![i].getExplosion(explosions!![i].explosionFrame)!!,
                explosions!![i].eX.toFloat(),
                explosions!![i].eY.toFloat(),
                null
            )
            explosions!![i].explosionFrame++
            if (explosions!![i].explosionFrame > 8) {
                explosions!!.removeAt(i)
            }
        }

        // If not paused, we’ll call the postDelayed() method on handler object which will cause the
        // run method inside Runnable to be executed after 30 milliseconds, that is the value inside
        // UPDATE_MILLIS.
        if (!paused && !gameOver) {
            scheduleNextFrame()
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        // Check if the touch event is within the pause button's rectangle
        pauseButtonRect?.let {
            if (x >= it.left && x <= it.right && y >= it.top && y <= it.bottom) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // Toggle the game state based on whether it is currently paused or not
                    if (paused) {
                        resumeGame()
                    } else {
                        pauseGame()
                    }
                    return true  // Consume the event here
                }
            }
        }

        // Handle other touch events for gameplay
        if (event.action == MotionEvent.ACTION_UP && !paused) {
            if (ourShots!!.size < 1) {
                val ourShot = Shot(
                    context,
                    ourSpaceship!!.ox + ourSpaceship!!.getOurSpaceshipWidth() / 2,
                    ourSpaceship!!.oy
                )
                ourShots!!.add(ourShot)
                gunmediaPlayer?.start()
            }
        }

        if (!paused) {
            // Move ourSpaceship on ACTION_DOWN or ACTION_MOVE
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                ourSpaceship!!.ox = x.coerceIn(0, screenWidth - ourSpaceship!!.getOurSpaceshipWidth())
            }
        }

        return true
    }

    fun pauseGame() {
        if (!paused) {
            paused = true
            pauseButtonText = "Play"
            mHandler.removeCallbacks(runnable)
            gameTimer?.cancel()
            invalidate()
        }
    }

    fun resumeGame() {
        if (paused) {
            paused = false
            pauseButtonText = "Pause"
            gameTimer?.start()
            scheduleNextFrame()
            invalidate()
        }
    }

    private fun scheduleNextFrame() {
        mHandler.removeCallbacks(runnable)
        if (!paused) {
            mHandler.postDelayed(runnable, UPDATE_MILLIS)
        }
    }


    // Assuming there is a method or place where enemy shots are managed
    private var lastShotTime = System.currentTimeMillis()

    private fun tryShootingEnemyBullet() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShotTime > enemyShotInterval && !paused) {
            createEnemyBullet()
            lastShotTime = currentTime
        }
    }

    private fun createEnemyBullet() {
        if (!gameOver && enemySpaceship != null) {
            val shot = Shot(
                context,
                enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                enemySpaceship!!.ey
            )
            enemyShots!!.add(shot)
        }
    }



}


