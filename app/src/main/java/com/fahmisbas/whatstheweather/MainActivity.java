package com.fahmisbas.whatstheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    EditText edt_city;
    TextView tv_main, tv_desc;
    Button btn_generateWeatherInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edt_city = findViewById(R.id.edt_city);
        tv_main = findViewById(R.id.tv_main);
        tv_desc = findViewById(R.id.tv_desc);
        btn_generateWeatherInfo = findViewById(R.id.btn_generateWeatherInfo);

        generateWeatherInfo();


        tv_main.setText("Unkown");
        tv_desc.setText("Unknown");

    }

    private void generateWeatherInfo() {

        btn_generateWeatherInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (edt_city.getText().toString().isEmpty()) {
                        edt_city.setError("Name the city!");
                    } else {
                        String city = edt_city.getText().toString();
                        DownloadingTask task = new DownloadingTask();
                        task.execute("https://openweathermap.org/data/2.5/weather?q=" + city + "&appid=b6907d289e10d714a6e88b30761fae22").get();

                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (manager != null) {
                            manager.hideSoftInputFromWindow(edt_city.getWindowToken(), 0);
                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public class DownloadingTask extends AsyncTask<String, Void, String> {

        String result = "";

        @Override
        protected String doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Could not find the weather", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");

                JSONArray array = new JSONArray(weatherInfo);

                String message = "";
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonPart = array.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String desc = jsonPart.getString("description");

                    if (!main.equals("") && !desc.equals("")) {
                        message += main + ": " + desc + "\r\n";
                    }
                }

                if (!message.equals("")) {
                    tv_main.setText(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
