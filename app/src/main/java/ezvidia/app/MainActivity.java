package ezvidia.app;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;
import ezvidia.app.tasks.ApplyConfigTask;
import ezvidia.app.tasks.ListConfigsTask;

public class MainActivity extends AppCompatActivity {

    private MainActivity activity = this;

    private LinearLayout configs_container;
    private TextView server_label;

    private Client client;
    private String server_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configs_container = findViewById(R.id.configs_container);
        server_label = findViewById(R.id.server_label);

        updateServer("192.168.1.90");

        try {
            client = new Client();
            client.connect(server_address);
        } catch (UnknownHostException e) {
            Log.e("UnknownHostException", "Invalid address", e);
        } catch (SocketException e) {
            Log.e("SocketException", "Socket error", e);
        } catch (IOException e) {
            Log.e("IOException", "Could not receive message", e);
        }
    }

    public void renderConfigs(ArrayList<Config> configs) {

        configs_container.removeAllViews();

        for (Config config : configs) {
            config.render(this, configs_container);
        }

    }

    public void refresh(View v) {
        new ListConfigsTask(this, client).execute();
    }


    public View.OnClickListener applyListener = new View.OnClickListener(){

        public void onClick(View v) {
            Button button_config = (Button) v;
            new ApplyConfigTask(activity, client, button_config.getText().toString()).execute();
        }
    };


    public void updateServer(String address) {
        this.server_address = address;
        server_label.setText("Server: " + address);
    }

}
