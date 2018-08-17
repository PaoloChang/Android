package com.map524s1a.snakegame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends Activity {

    // Declare an instance of SnakeEngine
    SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        snakeView = new SnakeView(this, size);

        // Make snakeEngine the view of the Activity
        setContentView(snakeView);
    }

    // Start the thread in snakeView
    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    // Stop the thread in snakeView
    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }
}
