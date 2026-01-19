package ro.pub.cs.systems.eim.practicaltest02v1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ClientActivity extends AppCompatActivity {

    protected Button connectButton;
    protected TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        connectButton = findViewById(R.id.get_autocomplete_button);
        outputTextView = findViewById(R.id.result_text_view);

        connectButton.setOnClickListener(v -> {
            new ClientThread().start();
        });
    }

    // THREAD GENERIC DE SOCKET
    protected class ClientThread extends Thread {
        @Override
        public void run() {
            // TODO: implementare concreta
        }
    }

    protected void updateUI(String text) {
        runOnUiThread(() -> outputTextView.setText(text));
    }
}