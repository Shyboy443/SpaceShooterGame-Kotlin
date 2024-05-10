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
    private var lifeCatchplayer: MediaPlayer? = null
    private var explosionmediaPlayer: MediaPlayer? = null
    private var bombexplosionmediaPlayer: MediaPlayer? = null
    private var pauseButtonRect: Rect? = null
    private var pauseButtonText = "Pause"
    private val pauseButtonPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
    }
    private var bombs: ArrayList<Bomb>? = ArrayList()
    private var background: Bitmap
    private var lifeImage: Bitmap
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    val UPDATE_MILLIS: Long = 30
    companion object {
        var screenWidth: Int = 0
        var screenHeight: Int = 0
    }
    private val maxDescentHeight: Int = screenHeight / 6
    private var bombDropCat: BombDropCat? = BombDropCat(context)
    private var showBombDropCat: Boolean = false
    private var bombDropped: Boolean = false
    private var lastAppearanceTime: Long = System.currentTimeMillis()
    private var appearanceInterval: Long = 20000
    private var enemyShotInterval: Long = 2000
    private var bulletSpeed: Int = 15
    private var enemyShotCollisionOccurred = false
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
    private var outOfScreenMediaPlayer: MediaPlayer? = null
    private var lives: ArrayList<Life>? = null
    private var lastBombDropTime: Long = System.currentTimeMillis()
    private val bombDropInterval: Long = 10000
    private val bombDropChance: Double = 0.5
    private var highestScore: Int = 0
    private var bombexplosions: ArrayList<BombExplosion> = ArrayList()



    init {
        bombexplosionmediaPlayer =MediaPlayer.create(context, R.raw.catcry)
        loadHighestScore()
        lives = ArrayList()
        outOfScreenMediaPlayer = MediaPlayer.create(context, R.raw.out_of_screen_sound)
        explosionmediaPlayer = MediaPlayer.create(context, R.raw.explosion)
        lifeCatchplayer = MediaPlayer.create(context, R.raw.levelupsound)
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
        background = BitmapFactory.decodeResource(context.resources, R.drawable.gameplaybackground3)
        lifeImage = BitmapFactory.decodeResource(context.resources, R.drawable.life)
        scorePaint = Paint()
        scorePaint!!.color = Color.RED
        scorePaint!!.textSize = TEXT_SIZE.toFloat()
        scorePaint!!.textAlign = Paint.Align.LEFT
        startGameTimer()

        val buttonWidth = 100
        val buttonHeight = 100
        val buttonRight = screenWidth - 30
        val buttonBottom = screenHeight - 30
        val buttonLeft = buttonRight - buttonWidth
        val buttonTop = buttonBottom - buttonHeight

        pauseButtonRect = Rect(buttonLeft, buttonTop, buttonRight, buttonBottom)

        background = getFullScreenBitmap(
            BitmapFactory.decodeResource(context.resources, R.drawable.gameplaybackground3),
            screenWidth,
            screenHeight
        )
    }



    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        isHitThisFrame = false
        canvas.drawBitmap(background, 0f, 0f, null)

        canvas.drawText("Score: $points", 0f, TEXT_SIZE.toFloat(), scorePaint!!)
        val smallerTextSize = (TEXT_SIZE * 0.8).toFloat()
        scorePaint!!.textSize = smallerTextSize
        canvas.drawText("High Score: $highestScore", 0f, TEXT_SIZE * 2.toFloat(), scorePaint!!)

        scorePaint!!.textSize = TEXT_SIZE.toFloat()

        for (i in life downTo 1) {
            canvas.drawBitmap(lifeImage, (screenWidth - lifeImage.width * i).toFloat(), 0f, null)
        }


        val gameTimeText = "Time: ${formatTime(gameTimeInSeconds)}"
        val textX = TEXT_SIZE.toFloat()-40
        val textY = (screenHeight - TEXT_SIZE / 2).toFloat()
        canvas.drawText(gameTimeText, textX, textY, scorePaint!!)



        if (pauseButtonRect == null) {
            pauseButtonRect = Rect(50, 50, 350, 150)
        }


        // Draw the button
        pauseButtonRect?.let {
            val textBounds = Rect()
            pauseButtonPaint.getTextBounds(pauseButtonText, 0, pauseButtonText.length, textBounds)
            val x = it.left + (it.width() - textBounds.width()) / 2
            val y = it.top + (it.height() + textBounds.height()) / 2 - textBounds.bottom
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
        updateBombDropCat()
        if (showBombDropCat) {
            canvas.drawBitmap(bombDropCat!!.getBombDropCatBitmap(), bombDropCat!!.ex.toFloat(), bombDropCat!!.ey.toFloat(), null)
        }

        updateBombs(15)

        val bombIterator = bombs!!.iterator()
        while (bombIterator.hasNext()) {
            val bomb = bombIterator.next()
            if (checkCollisionWithBomb(bomb)) {
                life -= 2
                bombIterator.remove()
                bombexplosionmediaPlayer?.start()
            }
        }
        // Draw each bomb
        bombs?.forEach { bomb ->
            canvas.drawBitmap(bomb.getBomb(), bomb.bx.toFloat(), bomb.by.toFloat(), null)
        }

        tryDroppingBomb()

        // Life Drop
        val lifeiterator = lives!!.iterator()
        while (lifeiterator.hasNext()) {
            val bomb = lifeiterator.next()
            bomb.updatePosition(5)

            // Draw the bomb
            canvas.drawBitmap(bomb.getLife(), bomb.bx.toFloat(), bomb.by.toFloat(), null)


            if (checkCollision(ourSpaceship!!, bomb)) {
                if (life < 5) {
                    life++
                    lifeCatchplayer?.start()
                }
                lifeiterator.remove()
            }

            // Remove bomb if it goes off screen
            if (bomb.by > screenHeight) {
                lifeiterator.remove()
            }
        }





        // Move enemySpaceship
        enemySpaceship!!.ex += enemySpaceship!!.enemyVelocity
        if (enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() >= screenWidth) {
            enemySpaceship!!.enemyVelocity *= -1
        }
        if (enemySpaceship!!.ex <= 0) {
            enemySpaceship!!.enemyVelocity *= -1
        }
        if (!enemyShotAction) {
            if (enemySpaceship!!.ex >= 200 + random!!.nextInt(400) && enemySpaceship!!.ey >= 200 + random!!.nextInt(400)) {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
                enemyShotAction = true
            }
            enemyShotAction = if (enemySpaceship!!.ex >= 400 + random!!.nextInt(800) && enemySpaceship!!.ey >= 400 + random!!.nextInt(800)) {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
                true
            } else {
                val enemyShot = Shot(
                    context,
                    enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2,
                    enemySpaceship!!.ey
                )
                enemyShots!!.add(enemyShot)
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

        val it = bombexplosions.iterator()
        while (it.hasNext()) {
            val explosion = it.next()
            val bitmap = explosion.getBombExplosion(explosion.explosionFrame)
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, explosion.eX.toFloat(), explosion.eY.toFloat(), null)
                explosion.explosionFrame++
                if (explosion.explosionFrame >= explosion.explosion.size) {
                    it.remove() // Remove the explosion after the last frame is shown
                }
            }
        }
        val iterator = enemyShots!!.iterator()
        while (iterator.hasNext()) {
            val enemyShot = iterator.next()
            enemyShot.shy += bulletSpeed
            canvas.drawBitmap(enemyShot.getShot(), enemyShot.shx.toFloat(), enemyShot.shy.toFloat(), null)

            // Check for collisions with our spaceship
            if (!isHitThisFrame && enemyShot.shx >= ourSpaceship!!.ox && enemyShot.shx <= ourSpaceship!!.ox + ourSpaceship!!.getOurSpaceshipWidth() &&
                enemyShot.shy >= ourSpaceship!!.oy && enemyShot.shy <= ourSpaceship!!.oy + ourSpaceship!!.getOurSpaceshipHeight()) {
                explosionmediaPlayer?.start()
                points++
                isHitThisFrame = true
                iterator.remove()
                explosion = Explosion(context, ourSpaceship!!.ox, ourSpaceship!!.oy)
                explosions!!.add(explosion!!)
                enemyShotCollisionOccurred = true
                continue
            }

            if (enemyShot.shy >= screenHeight) {
                iterator.remove()
                life--
                outOfScreenMediaPlayer?.start()
            }
        }

        if (enemyShots!!.isEmpty()) {
            enemyShotAction = false
        }

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
        if (!paused && !gameOver) {
            scheduleNextFrame()
        }
        tryDroppingLife()

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        pauseButtonRect?.let {
            if (x >= it.left && x <= it.right && y >= it.top && y <= it.bottom) {
                if (event.action == MotionEvent.ACTION_DOWN) {

                    if (paused) {
                        resumeGame()
                    } else {
                        pauseGame()
                    }
                    return true
                }
            }
        }
        // Handle other touch events for gameplay
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

    private var lastShotTime = System.currentTimeMillis()

    private fun tryShootingEnemyBullet() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShotTime > enemyShotInterval && !paused && !enemyShotCollisionOccurred) {
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

    private fun tryDroppingLife() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBombDropTime > bombDropInterval && Math.random() < bombDropChance && life < 5 && !paused && !gameOver) {
            dropLife()
            lastBombDropTime = currentTime
        }
    }

    private fun dropLife() {

        val life = Life(context, enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2, enemySpaceship!!.ey)
        lives!!.add(life)
    }

    private fun checkCollision(ourSpaceship: OurSpaceship, life: Life): Boolean {
        return life.bx + life.getLife().width > ourSpaceship.ox &&
                life.bx < ourSpaceship.ox + ourSpaceship.getOurSpaceshipWidth() &&
                life.by + life.getLife().height > ourSpaceship.oy &&
                life.by < ourSpaceship.oy + ourSpaceship.getOurSpaceshipHeight()
    }

    private fun dropBomb() {
        val bomb = Bomb(context, enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() / 2, enemySpaceship!!.ey)
        bombs!!.add(bomb)
    }

    private fun updateBombs(speed: Int) {
        val iterator = bombs!!.iterator()
        while (iterator.hasNext()) {
            val bomb = iterator.next()
            bomb.updatePosition(speed)


            if (bomb.by > screenHeight) {
                iterator.remove()
            }
        }
    }

    private fun tryDroppingBomb() {
        val currentTime = System.currentTimeMillis()
        // Check if the enemy spaceship is on the screen
        val enemyOnScreen = enemySpaceship!!.ex + enemySpaceship!!.getEnemySpaceshipWidth() > 0 &&
                enemySpaceship!!.ex < screenWidth &&
                enemySpaceship!!.ey + enemySpaceship!!.getEnemySpaceshipHeight() > 0 &&
                enemySpaceship!!.ey < screenHeight

        if (currentTime - lastBombDropTime > bombDropInterval &&
            Math.random() < bombDropChance &&
            !paused && !gameOver && enemyOnScreen) {
            dropBomb()
            lastBombDropTime = currentTime  // Reset the timer
        }
    }



    private fun checkCollisionWithBomb(bomb: Bomb): Boolean {
        val bombRect = Rect(bomb.bx, bomb.by, bomb.bx + bomb.getBomb().width, bomb.by + bomb.getBomb().height)
        val spaceshipRect = Rect(
            ourSpaceship!!.ox,
            ourSpaceship!!.oy,
            ourSpaceship!!.ox + ourSpaceship!!.getOurSpaceshipWidth(),
            ourSpaceship!!.oy + ourSpaceship!!.getOurSpaceshipHeight()
        )

        if (Rect.intersects(bombRect, spaceshipRect)) {
            bombexplosions.add(BombExplosion(context, ourSpaceship!!.ox, ourSpaceship!!.oy))
            return true
        }
        return false
    }


    private fun updateBombDropCat() {
        if (!showBombDropCat && shouldAppear()) {
            bombDropCat!!.ex = Random().nextInt(screenWidth - bombDropCat!!.getBombDropCatWidth())
            bombDropCat!!.ey = -bombDropCat!!.getBombDropCatHeight()
            showBombDropCat = true
            bombDropped = false
        }

        if (showBombDropCat) {

            if (bombDropCat!!.ey < maxDescentHeight && !bombDropped) {
                bombDropCat!!.ey += 10
            } else if (!bombDropped && bombDropCat!!.ey >= 0) {

                dropBombFromCat()
                bombDropped = true
            }


            if (bombDropped) {
                bombDropCat!!.ey -= 10  // Move up
                if (bombDropCat!!.ey <= -bombDropCat!!.getBombDropCatHeight()) {
                    showBombDropCat = false
                    lastAppearanceTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun dropBombFromCat() {
        if (bombDropCat!!.ey in 0..screenHeight) {
            val bomb = Bomb(context, bombDropCat!!.ex + bombDropCat!!.getBombDropCatWidth() / 2, bombDropCat!!.ey)
            bombs!!.add(bomb)
        }
    }

    private fun loadHighestScore() {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)
    }

    private fun shouldAppear(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastAppearanceTime > appearanceInterval && Math.random() < 0.5
    }
    private fun updateHighestScore() {
        if (points > highestScore) {
            highestScore = points
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("highestScore", highestScore)
            editor.apply()
        }
    }

    private fun startGameTimer() {
        gameTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                gameTimeInSeconds++
                if ((gameTimeInSeconds % 5).toInt() == 0) {
                    enemyShotInterval -= 200
                    bulletSpeed += 2
                    if (enemyShotInterval < 500) {
                        enemyShotInterval = 500
                    }
                    if (bulletSpeed > 70) {
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
        updateHighestScore()
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

    private fun getFullScreenBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifeCatchplayer?.release()
        explosionmediaPlayer?.release()
        bombexplosionmediaPlayer?.release()
        outOfScreenMediaPlayer?.release()

        lifeCatchplayer = null
        explosionmediaPlayer = null
        outOfScreenMediaPlayer = null
        bombexplosionmediaPlayer = null
    }



}


