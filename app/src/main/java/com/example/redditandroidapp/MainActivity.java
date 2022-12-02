package com.example.redditandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button refresh;
    private String url;
    private LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Define of all future variables
        url = "https://www.reddit.com/top.json";
        refresh = findViewById(R.id.refresh);

        layout = findViewById(R.id.main_list);

        //Refresh Button active
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, R.string.refresh, Toast.LENGTH_SHORT).show();
                new GetDataFromURL().execute(url);
            }
        });
    }

    private void createPosts(String url, String text){
        View view = getLayoutInflater().inflate(R.layout.post_reddit, null);
        TextView text_slot = view.findViewById(R.id.text_slot);
        ImageView image_slot = view.findViewById(R.id.image_slot);
        Picasso.get().load(url).into(image_slot);
        text_slot.setText(text);


        layout.addView(view);
    }

    private class GetDataFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, R.string.refresh, Toast.LENGTH_SHORT);
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
                for(int i =0; i< object.getJSONObject("data").getJSONArray("children").length(); i++){
                    System.out.println("Test of text: " + object.getJSONObject("data").getJSONArray("children").getJSONObject(i).getJSONObject("data").getString("title"));
                    createPosts(object.getJSONObject("data").getJSONArray("children").getJSONObject(i).getJSONObject("data").getString("url_overridden_by_dest"),
                            object.getJSONObject("data").getJSONArray("children").getJSONObject(i).getJSONObject("data").getString("title"));
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}

