package edu.utexas.cs371m.witchel.redfetch;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

// Code adapted from https://stackoverflow.com/questions/17520750/list-view-item-swipe-left-and-swipe-right
public class SwipeDetector implements RecyclerView.OnItemTouchListener {
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mSwipeDetected = Action.None;
                downX = event.getX();
                downY = event.getY();
            }
            case MotionEvent.ACTION_UP: {
                upY = event.getY();
                upX = event.getX();
                float deltaY = downY - upY;
                float deltaX = downX - upX;

                if (deltaX < deltaY) {
                    return false;
                }
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        mSwipeDetected = Action.LR;
                    }
                    if (deltaX > 0) {
                        mSwipeDetected = Action.RL;
                    }
                    View child = rv.findChildViewUnder(upX, upY);
                    if (child != null)
                        child.callOnClick();
                }
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        /* we never return true from intercept so this call should be impossible! */
        throw new Error("onTouchEvent Should not be called!");
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        /* unused */
    }

    public static enum Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    private static final String logTag = "SwipeDetector";
    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY, totalx;
    private Action mSwipeDetected = Action.None;

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    public Action getAction() {
        return mSwipeDetected;
    }
}