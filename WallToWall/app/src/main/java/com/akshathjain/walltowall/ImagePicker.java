package com.akshathjain.walltowall;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImagePicker extends AppCompatActivity {
    public final int NUM_COLUMNS = 3;

    private RecyclerView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        imageView = (RecyclerView) findViewById(R.id.recycler_view_image_view);
        imageView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));

        JSONRetriever retriever = new JSONRetriever();
        retriever.addAsyncFinishedListener(new AsyncFinished<JSONObject>() {
            @Override
            public void onAsyncFinished(final JSONObject o) {
                ImageViewAdapter adapter = new ImageViewAdapter(ImagePicker.this, o);

                //adds a custom on item click method
                adapter.addRecyclerViewItemClickListener(new ImageViewAdapter.RecyclerViewItemClick() {
                    @Override
                    public void onClick(final int position) {
                        System.out.println("position: " + position);
                        final WallpaperManager wm = WallpaperManager.getInstance(ImagePicker.this); //get the wallpaper manager
                        final ProgressDialog loader = new ProgressDialog(ImagePicker.this);
                        loader.setCancelable(false);
                        loader.setMessage("Setting wallpaper...");
                        loader.show();

                        try {
                            String url = o.getString("rootPath") + o.getJSONArray("data").getJSONObject(position).getString("name");
                            Glide.with(ImagePicker.this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> transition) {

                                    //multithread because the wallpaper manager takes a really long time
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                wm.setBitmap(resource);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            loader.dismiss();
                                        }
                                    }).start();

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                imageView.setAdapter(adapter);
            }
        });
        retriever.execute("http://akshathjain.com/WallToWall/json/directory.json");
    }

    //this class will retrieve the json on a background thread
    class JSONRetriever extends AsyncTask<String, Void, JSONObject> {
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
        protected JSONObject doInBackground(String... jsonUrl) {
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

                return new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            super.onPostExecute(data);
            loader.dismiss(); //dismiss the loader dialog
            reference.onAsyncFinished(data); //uses the AsyncJSONRetrieved interface to call the json received method in the main class
        }
    }
}




