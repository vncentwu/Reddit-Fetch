package edu.utexas.cs371m.witchel.redfetch;

import android.os.Handler;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

// This is a Singleton class that makes sure the app as a whole does not
// fetch more than a URL every 2 seconds.  It is a bit fancier than the version
// I distributed in class because it will "save up" two seconds so that a
// new request after 2 seconds of inactivity will immediately fetch.
public class RateLimit {
    public interface RateLimitCallback {
        void rateLimitReady();
    }

    protected Handler handler;
    protected Runnable rateLimitRequest;
    protected final int rateLimitMillis = 2000; // 2 sec
    // Lock protects okToRun and rateLimitCallbacks
    protected ReentrantLock l = new ReentrantLock();
    protected boolean okToRun;
    protected LinkedList<RateLimitCallback> rateLimitCallbacks;

    private RateLimit() {
        handler = new Handler();
        l = new ReentrantLock();
        okToRun = false;
        rateLimitCallbacks = new LinkedList<>();
        rateLimitRequest = new Runnable() {
            @Override
            public void run() {
                l.lock();
                okToRun = true;
                l.unlock();
                // Don't hold lock for runIfOk, because don't hold lock during callback
                runIfOk();
                handler.postDelayed(this, rateLimitMillis);
            }
        };
        handler.postDelayed(rateLimitRequest, rateLimitMillis);
    }

    // Don't hold lock for runIfOk, because don't hold lock during callback
    protected void runIfOk() {
        l.lock();
        if (!rateLimitCallbacks.isEmpty() && okToRun) {
            okToRun = false;
            RateLimitCallback rlc = rateLimitCallbacks.pop();
            l.unlock();
            // Do callback without holding the lock because we don't know how long
            // it will take, and we are done protecting okToRun and rateLimitCallbacks
            rlc.rateLimitReady();
            return;
        }
        l.unlock();
    }

    // See https://en.wikipedia.org/wiki/Double-checked_locking
    // To understand this idiom
    private static class RateLimitHolder {
        public static final RateLimit rateLimit = new RateLimit();
    }

    public static RateLimit getInstance() {
        return RateLimitHolder.rateLimit;
    }

    // Called from an AsyncTask
    public void add(RateLimitCallback rlc) {
        l.lock();
        try {
            if (!rateLimitCallbacks.contains(rlc)) {
                rateLimitCallbacks.add(rlc);
            }
        } finally {
            l.unlock();
        }
        // Call runIfOk because we might have waited rateLimitMillis
        runIfOk();
    }

    // Called from AsyncTask
    // Add to front of queue, for important fetches
    public void addFront(RateLimitCallback rlc) {
        l.lock();
        try {
            if (!rateLimitCallbacks.contains(rlc)) {
                rateLimitCallbacks.push(rlc);
            }
        } finally {
            l.unlock();
        }
        // Call runIfOk because we might have waited rateLimitMillis
        runIfOk();
    }
}

