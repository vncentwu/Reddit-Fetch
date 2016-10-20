package edu.utexas.cs371m.witchel.redfetch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class URLFetch implements RateLimit.RateLimitCallback {
    public interface Callback {
        void fetchStart();
        void fetchComplete(String result);
        void fetchCancel(String url);
    }
    protected Callback callback = null;
    protected URL url;

    public URLFetch(Callback callback, URL url, boolean front) {
        this.callback = callback;
        this.url = url;
        if( front ) {
            RateLimit.getInstance().addFront(this);
        } else {
            RateLimit.getInstance().add(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return url.equals(((URLFetch)obj).url);
    }

    public void rateLimitReady() {
        new AsyncDownloader().execute(this.url);

        // XXX write me: execute async downloader
    }

    public class AsyncDownloader extends AsyncTask<URL, Integer, String> {
        @Override
        protected String doInBackground(URL... params) {

            URL url = params[0];
            URLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            urlConnection.setRequestProperty("User-Agent", "android:edu.utexas.cs371m.vncentwu.redfetch:v1.0 (by /u/vncentwu)");

            if(urlConnection.getHeaderField("Content-Type").startsWith("image/")){
                //urlConnection.setDoInput(true);
               try{
                   urlConnection.connect();
                   InputStream input = urlConnection.getInputStream();
                   Bitmap bm = BitmapFactory.decodeStream(input);
                   BitmapCache.getInstance().setBitmap(url.toString(), bm);
                   return url.toString();
               }
               catch(IOException e){
                   return null;
               }
            }
            else{
                InputStream in = null;
                try {
                    in = new BufferedInputStream(urlConnection.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuffer buffer = new StringBuffer();

                try {
                    while((line = reader.readLine()) != null){
                        buffer.append(line+"\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("URL", url.toString());
                Log.d("hwefoiw", buffer.toString());
                return buffer.toString();
            }

        }

        protected void onPostExecute(String result)
        {
            callback.fetchComplete(result);
        }
        // XXX Write me

        // Note:
        // At some point in this code you will open a network
        // connection.  That code will look something like this.
        //  urlConn = (HttpURLConnection) url.openConnection();
        // Once you open the connection, you MUST set the User-Agent,
        // which is an identifying string for your app.  This is what
        // you should use
        //  urlConn.setRequestProperty("User-Agent",
        // "android:edu.utexas.cs371m.YOURID.redfetch:v1.0 (by
        // /u/YOURREDDITID)");
        // In the above code you should substitute something identifying like your CS
        // username or eid for YOURID, and if you have a reddit ID, use
        // it for YOURREDDITID.  If not, use YOURID again.
        // If you don't set User-Agent properly, you will lose a lot of points.

    }
}
