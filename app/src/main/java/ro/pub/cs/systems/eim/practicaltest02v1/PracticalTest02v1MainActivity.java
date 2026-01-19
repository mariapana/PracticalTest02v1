package ro.pub.cs.systems.eim.practicaltest02v1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

public class PracticalTest02v1MainActivity extends AppCompatActivity {

    private static final String TAG = "EIM_AUTO";
    private static final String ACTION_AUTOCOMPLETE = "ro.pub.cs.systems.eim.AUTOCOMPLETE";
    private static final String EXTRA_RESULTS = "results";
    private TextView resultTextView;


    private final IntentFilter intentFilter = new IntentFilter(ACTION_AUTOCOMPLETE);

    private final BroadcastReceiver autocompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String results = intent.getStringExtra(EXTRA_RESULTS);
            if (results == null) results = "";
            resultTextView.setText(results);
            Log.d(TAG, "UI updated via BroadcastReceiver");
        }
    };


    EditText prefixEditText;
    EditText serverPortEditText;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v1_main);

        prefixEditText = findViewById(R.id.prefix_edit_text);
        serverPortEditText = findViewById(R.id.server_port_edit_text);

        btn = findViewById(R.id.get_autocomplete_button);
        resultTextView = findViewById(R.id.result_text_view);

        btn.setOnClickListener(v -> {
            String prefix = prefixEditText.getText().toString().trim();

            if (prefix.isEmpty()) {
                Toast.makeText(this, "Scrie un prefix!", Toast.LENGTH_SHORT).show();
                return;
            }

            doAutocompleteRequest(prefix, Integer.parseInt(serverPortEditText.getText().toString()));
        });
    }
    private void doAutocompleteRequest(String prefix, int port) {
        new Thread(() -> {
            Log.d(TAG, "THREAD STARTED, prefix=" + prefix + ", port=" + port);

            try {
                String urlStr =
                        "https://suggestqueries.google.com/complete/search?client=firefox&q="
                                + URLEncoder.encode(prefix, "UTF-8");

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int code = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                conn.disconnect();

                if (code < 200 || code >= 300) {
                    Log.e(TAG, "HTTP error");
                    return;
                }

                JSONArray root = new JSONArray(sb.toString());
                JSONArray suggestions = root.getJSONArray(1);

                StringBuilder resultBuilder = new StringBuilder();
                for (int i = 0; i < suggestions.length(); i++) {
                    resultBuilder.append(i + 1)
                            .append(". ")
                            .append(suggestions.getString(i))
                            .append("\n");
                }

                String result = resultBuilder.toString();

                sendResultToServer(result, port);
                sendResultsBroadcast(result);

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
            }
        }).start();
    }

    private void sendResultToServer(String result, int port) {
        try {
            Log.d(TAG, "Sending result to port " + port);

            Socket socket = new Socket("10.0.2.2", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(result);

            socket.close();

        } catch (Exception e) {
            Log.e(TAG, "Port send error: " + e.getMessage(), e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ContextCompat.registerReceiver(
                this,
                autocompleteReceiver,
                intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onPause() {
        unregisterReceiver(autocompleteReceiver);
        super.onPause();
    }
    private void sendResultsBroadcast(String results) {
        Intent intent = new Intent(ACTION_AUTOCOMPLETE);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_RESULTS, results);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent");
    }

}