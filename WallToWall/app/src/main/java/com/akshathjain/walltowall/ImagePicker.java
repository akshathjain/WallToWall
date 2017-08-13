package com.akshathjain.walltowall;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ImagePicker extends AppCompatActivity {
    private ImageView tempImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        tempImageView = (ImageView) findViewById(R.id.tempImageView);

        ImageRetriever retriever = new ImageRetriever();
        retriever.addAsyncFinishedListener(new AsyncFinished<Bitmap[]>() {
            @Override
            public void onAsyncFinished(Bitmap[] b) {
                tempImageView.setImageBitmap(b[0]);
            }
        });
        retriever.execute("http://akshathjain.com/WallToWall/json/directory.json");
    }

    //this class will retrieve the json on a background thread
    class ImageRetriever extends AsyncTask<String, Void, Bitmap[]> {
        private ProgressDialog loader;
        private AsyncFinished reference;

        //adds an interface that is called when task is finished executing
        public void addAsyncFinishedListener(AsyncFinished reference) {
            this.reference = reference;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //set up a loader dialog
            loader = new ProgressDialog(ImagePicker.this);
            loader.setMessage("Retrieving wallpaper data...");
            loader.setCancelable(false);
            loader.show();
        }

        @Override
        protected Bitmap[] doInBackground(String... jsonUrl) {
            String result = null;

            try {
                //set up a HTTP request
                URL url = new URL(jsonUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Scanner scanner = new Scanner(connection.getInputStream());

                result = new String();
                while (scanner.hasNext())
                    result += scanner.nextLine();

                //get the bitmaps from json
                JSONObject data = new JSONObject(result);
                String rootPath = data.getString("rootPath");
                JSONArray imageData = data.getJSONArray("data");
                Bitmap[] bitmaps = new Bitmap[imageData.length()];
                for(int i = 0; i < bitmaps.length; i++){
                    HttpURLConnection con = (HttpURLConnection)new URL(rootPath + imageData.getJSONObject(i).getString("name")).openConnection();
                    Bitmap fullSize = BitmapFactory.decodeStream(con.getInputStream());

                    //compress the bitmap
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    fullSize.compress(Bitmap.CompressFormat.PNG, 0, out);
                    bitmaps[i] = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                }
                return bitmaps;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap[] data) {
            super.onPostExecute(data);
            loader.dismiss(); //dismiss the loader dialog
            reference.onAsyncFinished(data); //uses the AsyncJSONRetrieved interface to call the json received method in the main class
        }
    }
}



