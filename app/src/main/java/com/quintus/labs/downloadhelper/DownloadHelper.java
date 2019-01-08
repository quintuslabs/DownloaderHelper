package com.quintus.labs.downloadhelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Quintus Labs on 08/01/19.
 * www.quintuslabs.com
 */

public class DownloadHelper {
    public static String TAG = "DownloadHelper==>";
    Context context;
    String storeDir;

    String new_directory, urlString;
    Intent intent;

    public DownloadHelper(Context context) {
        this.context = context;
    }


    public void download(String directory, String url) {
        storeDir = Environment.getExternalStorageDirectory().toString();
        new_directory = directory;
        urlString = url;

        BackTask bt = new BackTask();
        bt.execute(url);


    }

    private void openWebViewActivity(String url) {
        intent = new Intent(context, WebVewActivity.class);
        intent.putExtra("link", url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private class BackTask extends AsyncTask<String, Integer, Void> {
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;

        protected void onPreExecute() {
            super.onPreExecute();
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle("File Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.mipmap.ic_launcher);

        }

        protected Void doInBackground(String... params) {
            URL url = null;
            int count;
            try {
                url = new URL(params[0]);
                String pathl = "";
                try {
                    File f = new File(storeDir + "/" + new_directory + "/");
                    f.mkdir();
                    if (f.exists()) {
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        InputStream is = con.getInputStream();
                        String pathr = url.getPath();
                        String filename = pathr.substring(pathr.lastIndexOf('/') + 1);
                        pathl = storeDir + "/" + new_directory + "/" + filename;
                        FileOutputStream fos = new FileOutputStream(pathl);

                        int lenghtOfFile = con.getContentLength();
                        byte data[] = new byte[1024];
                        long total = 0;
                        while ((count = is.read(data)) != -1) {
                            total += count;
                            // publishing the progress
                            publishProgress((int) ((total * 100) / lenghtOfFile));
                            // writing data to output file
                            fos.write(data, 0, count);
                        }

                        is.close();
                        fos.flush();
                        fos.close();
                    } else {
                        Log.e("Error", "Not found: " + storeDir);
                        openWebViewActivity(urlString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                    openWebViewActivity(urlString);
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                openWebViewActivity(urlString);
                Log.d(TAG, e.getMessage());

            }

            return null;

        }

        protected void onProgressUpdate(Integer... progress) {

            mBuilder.setProgress(100, progress[0], false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel("100", "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotifyManager != null;
                mBuilder.setChannelId("100");
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
            assert mNotifyManager != null;
            // Displays the progress bar on notification
            mNotifyManager.notify(0, mBuilder.build());
        }

        protected void onPostExecute(Void result) {
            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(0, mBuilder.build());
        }

    }

}
