package com.example.redditandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private Button refresh;
    private String url;
    private LinearLayout layout;
    private ArrayList<View> temp_view;
    private static final int REQUEST_CODE = 1;
    private ImageView temp_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Define of all future variables
        url = "https://www.reddit.com/top.json";
        refresh = findViewById(R.id.refresh);
        temp_view = new ArrayList<>();
        layout = findViewById(R.id.main_list);
        new GetDataFromURL().execute(url);
        //Refresh Button active
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, R.string.refresh, Toast.LENGTH_SHORT).show();
                new GetDataFromURL().execute(url);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void createPosts(String url, String text, String author, long time, String rating, long comment_count){
        //Get variables
        View view = getLayoutInflater().inflate(R.layout.post_reddit, null);
        TextView text_slot = view.findViewById(R.id.text_slot);
        ImageView image_slot = view.findViewById(R.id.image_slot);
        TextView date_slot = view.findViewById(R.id.date);
        TextView author_slot = view.findViewById(R.id.author);
        TextView rating_slot = view.findViewById(R.id.rating);
        TextView comment_slot = view.findViewById(R.id.comments);
        Button download = view.findViewById(R.id.download);
        Date now = new Date();

        //Set variables
        Picasso.get().load(url).into(image_slot);
        comment_slot.setText("Comment count: " + comment_count);
        author_slot.setText("Posted by: " + author);
        Date date = new Date(time * 1000);
        long milliseconds = now.getTime() - date.getTime();
        int hours = (int) (milliseconds / (60 * 60 * 1000));
        date_slot.setText("Date: " + date + " (" + hours + " hours ago)");
        rating_slot.setText("Rating: " + rating);
        text_slot.setText(text);
        if(!url.contains(".jpg") && !url.contains(".png") && !url.contains(".jpeg")){
            download.setVisibility(View.GONE);
        }

        //Add download func
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp_image = image_slot;
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    saveImage(temp_image);
                }else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_CODE);
                }

            }
        });


        //Add image func
        image_slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(url);
                startActivity( new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        temp_view.add(view);
        layout.addView(view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage(temp_image);
            }else {
                Toast.makeText(MainActivity.this, "Please provide required permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveImage(ImageView image) {
        Uri images;
        ContentResolver contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) temp_image.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);


            Toast.makeText(MainActivity.this, "Image was dwnloaded...", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Image cant be dwnloaded...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private class GetDataFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader bufferedReader = null;

            try {
                //Scan Data from internet connection
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = bufferedReader.readLine()) != null){
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);
                if(!temp_view.isEmpty()){
                    for(int k=0; k<temp_view.size(); k++){
                        layout.removeView(temp_view.get(k));
                    }
                }
                temp_view = new ArrayList<>();
                for(int i =0; i< object.getJSONObject("data").getJSONArray("children").length(); i++){
                    JSONObject data = object.getJSONObject("data").getJSONArray("children").getJSONObject(i).getJSONObject("data");

                    createPosts(data.getString("url_overridden_by_dest"),
                            data.getString("title"),
                            data.getString("author"),
                            data.getLong("created_utc"),
                            data.getString("score"),
                            data.getLong("num_comments"));
                    //Author data rating
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }
}

