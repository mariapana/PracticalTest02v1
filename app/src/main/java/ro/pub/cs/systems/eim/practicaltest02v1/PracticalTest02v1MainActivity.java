package ro.pub.cs.systems.eim.practicaltest02v1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PracticalTest02v1MainActivity extends AppCompatActivity {

    private static final String TAG = "EIM_AUTO";

    private EditText prefixEditText;
    private Button getAutocompleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v1_main);

        prefixEditText = findViewById(R.id.prefix_edit_text);
        getAutocompleteButton = findViewById(R.id.get_autocomplete_button);

        getAutocompleteButton.setOnClickListener(v -> {
            String prefix = prefixEditText.getText().toString().trim();

            if (prefix.isEmpty()) {
                Toast.makeText(this, "Scrie un prefix!", Toast.LENGTH_SHORT).show();
                return;
            }

            doAutocompleteRequest(prefix);
        });
    }

    private void doAutocompleteRequest(String prefix) {
        new Thread(() -> {
            try {
                String urlStr = "https://www.google.com/complete/search?client=chrome&q="
                        + URLEncoder.encode(prefix, "UTF-8");

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Ajută să nu te blocheze Google (uneori)
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int code = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream()
                ));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                reader.close();
                conn.disconnect();

                Log.d(TAG, "HTTP " + code);
                Log.d(TAG, "FULL RESPONSE:\n" + sb.toString());

            } catch (Exception e) {
                Log.e(TAG, "Request failed: " + e.getMessage(), e);
            }
        }).start();
    }
}