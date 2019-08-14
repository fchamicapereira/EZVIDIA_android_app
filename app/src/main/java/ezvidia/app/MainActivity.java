package ezvidia.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;

public class MainActivity extends AppCompatActivity {

    LinearLayout configs_container;
    Client client;
    ArrayList<Config> configs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configs_container = findViewById(R.id.configs_container);

        try {
            client = new Client("192.168.1.90");
            configs = client.list();

            for (Config config : configs) {
                config.render(this, configs_container);
            }
        } catch (UnknownHostException e) {
            Log.e("UnknownHostException", "Invalid address", e);
        } catch (SocketException e) {
            Log.e("SocketException", "Socket error", e);
        } catch (IOException e) {
            Log.e("IOException", "Could not receive message", e);
        }
    }
}
