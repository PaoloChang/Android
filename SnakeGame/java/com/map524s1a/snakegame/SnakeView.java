package com.map524s1a.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;


class SnakeView extends SurfaceView implements Runnable {

    // Our game thread for the main game loop
    private Thread thread = null;

    // To hold a reference to the Activity
    private Context context;

    // for plaing sound effects
    private SoundPool soundPool;
    private int eat_rat = -1;
    private int snake_crash = -1;

    // For tracking movement Heading
    public enum Heading {UP, RIGHT, DOWN, LEFT}
    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // To hold the screen size in pixels
    private int screenX;
    private int screenY;

    // How long is the snake
    private int snakeLength;

    // Where is Rat hiding?
    private int ratX;
    private int ratY;

    // The size in pixels of a snake segment
    private int blockSize;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 20;
    private int numBlocksHigh;

    // Control pausing between updates
    private long nextFrameTime;
    // Update the game 10 times per second
    private final long FPS = 10;
    // There are 1000 milliseconds in a second
    private final long MILLIS_PER_SECOND = 1000;
    // We will draw the frame much more often

    // How many points does the player have
    private int score;

    // The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    // Everything we need for drawing
    // Is the game currently playing?
    private volatile boolean isPlaying;

    // A canvas for our paint
    private Canvas canvas;

    // Required to use canvas
    private SurfaceHolder surfaceHolder;

    // Some paint for our canvas
    private Paint snakePaint;
    private Paint scorePaint;
    private Paint ratPaint;

    public SnakeView(Context context, Point size) {
        super(context);

        context = context;

        screenX = size.x;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenY / blockSize;

        // Set the sound up
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Create objects of the 2 required classes
            // Use m_Context because this is a reference to the Activity
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the two sounds in memory
            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            eat_rat = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            snake_crash = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }

        // Initialize the drawing objects
        surfaceHolder = getHolder();

        snakePaint = new Paint();
        scorePaint = new Paint();
        ratPaint = new Paint();


        // If you score 200 you are rewarded with a crash achievement!
        snakeXs = new int[200];
        snakeYs = new int[200];

        // Start the game
        newGame();
    }

    @Override
    public void run() {

        while (isPlaying) {

            // Update 10 times a second
            if(updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        // Start with a single snake segment
        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        // Get Rat ready for dinner
        spawnRat();

        // Reset the score
        score = 0;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnRat() {
        Random random = new Random();
        ratX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        ratY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatRat(){

        // Increase the size of the snake
        snakeLength++;

        // Replace Rat
        // This reminds me of Edge of Tomorrow. Oneday Bob will be ready!
        spawnRat();
        //add to the score
        score = score + 1;
        soundPool.play(eat_rat, 1, 1, 0, 0, 1);
    }

    private void moveSnake(){
        // Move the body
        for (int i = snakeLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate heading
        switch (heading) {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath(){

        // Check snake alive
        boolean dead = false;

        // Collision detection on the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // Collision detection on snake itself
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        // Did the head of the snake eat Bob?
        if (snakeXs[0] == ratX && snakeYs[0] == ratY) {
            eatRat();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);

            newGame();
        }
    }

    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Fill the screen with background color
            canvas.drawColor(getResources().getColor(R.color.background));

            // Set the color of the paint to draw the snake
            snakePaint.setColor(getResources().getColor(R.color.snake));

            // Set the color of the paint to draw the text
            scorePaint.setColor(getResources().getColor(R.color.score));

            // Scale the HUD text
            scorePaint.setTextSize(90);
            scorePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Rats killed: " + score, 10, 70, scorePaint);

            Bitmap snakeHeadUp = BitmapFactory.decodeResource(getResources(), R.drawable.snake_head_up);
            Bitmap snakeHeadDown = BitmapFactory.decodeResource(getResources(), R.drawable.snake_head_down);
            Bitmap snakeHeadLeft = BitmapFactory.decodeResource(getResources(), R.drawable.snake_head_left);
            Bitmap snakeHeadRight = BitmapFactory.decodeResource(getResources(), R.drawable.snake_head_right);
            Bitmap snakeBody = BitmapFactory.decodeResource(getResources(), R.drawable.snake_body);

            // Draw the snake one block at a time
            for (int i = 0; i < snakeLength; i++) {

                Rect drawSnake = new Rect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize);

                if(i == 0) {
                    switch (heading) {
                        case UP:
                            canvas.drawBitmap(snakeHeadUp, null, drawSnake, snakePaint);
                            break;
                        case DOWN:
                            canvas.drawBitmap(snakeHeadDown, null, drawSnake, snakePaint);
                            break;
                        case LEFT:
                            canvas.drawBitmap(snakeHeadLeft, null, drawSnake, snakePaint);
                            break;
                        case RIGHT:
                            canvas.drawBitmap(snakeHeadRight, null, drawSnake, snakePaint);
                            break;
                    }
                }
                else {
                    canvas.drawBitmap(snakeBody, null, drawSnake, snakePaint);
                }
            }

            // Set the color of the paint to draw Rat Dark gray
            ratPaint.setColor(getResources().getColor(R.color.rat));

            Bitmap ratImage = BitmapFactory.decodeResource(getResources(), R.drawable.rat_image);

            // Draw Rat
            Rect rect = new Rect(ratX * blockSize,
                    (ratY * blockSize),
                    (ratX * blockSize) + blockSize,
                    (ratY * blockSize) + blockSize);

            canvas.drawBitmap(ratImage, null, rect, ratPaint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {

        // Are we due to update the frame
        if(nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + (MILLIS_PER_SECOND * 5) / (FPS * 4);

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch(heading){
                        case UP:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            break;
                    }
                } else {
                    switch(heading){
                        case UP:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.UP;
                            break;
                    }
                }
        }
        return true;
    }
}
