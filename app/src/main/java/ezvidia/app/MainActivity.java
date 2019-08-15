package ezvidia.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.ArrayList;

import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;
import ezvidia.app.tasks.ApplyConfigTask;
import ezvidia.app.tasks.ListConfigsTask;

public class MainActivity extends AppCompatActivity {

    private MainActivity activity = this;
    private SetAddressPopupWindow set_address_popup;

    public static final String SAVED_ADDRESS_KEY = "ADDRESS";

    private LinearLayout configs_container;
    private TextView server_label;

    private Button set_server_button;
    private Button refresh_button;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configs_container = findViewById(R.id.configs_container);
        server_label = findViewById(R.id.server_label);
        refresh_button = findViewById(R.id.refresh_button);
        set_server_button = findViewById(R.id.set_server_button);

        client = new Client();

        set_address_popup = new SetAddressPopupWindow(this, client);

        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.shared_preferences), MODE_PRIVATE);

        String address = prefs.getString(SAVED_ADDRESS_KEY, "");

        if (address.length() > 0) {
            updateServer(address);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        set_address_popup.close();

        if (client.getAddress().length() == 0) return;

        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.shared_preferences), MODE_PRIVATE).edit();

        editor.putString(SAVED_ADDRESS_KEY, client.getAddress());
        editor.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (client.getAddress().length() == 0) return;
        savedInstanceState.putString(SAVED_ADDRESS_KEY, client.getAddress());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (client.getAddress().length() == 0) return;

        String address = savedInstanceState.getString(SAVED_ADDRESS_KEY);

        if (address != null) {
            updateServer(address);
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

    public void openPopupSetServer(View v) {
        set_address_popup.open();
    }

    public View.OnClickListener applyListener = new View.OnClickListener(){

        public void onClick(View v) {
            Button button_config = (Button) v;
            new ApplyConfigTask(activity, client, button_config.getText().toString()).execute();
        }
    };

    public void lock() {

        set_server_button.setEnabled(false);
        refresh_button.setEnabled(false);

        for (int i = 0; i < configs_container.getChildCount(); i++) {
            Button config_button = (Button) configs_container.getChildAt(i);
            config_button.setEnabled(false);
        }

    }

    public void unlock() {

        set_server_button.setEnabled(true);
        refresh_button.setEnabled(true);

        for (int i = 0; i < configs_container.getChildCount(); i++) {
            Button config_button = (Button) configs_container.getChildAt(i);
            config_button.setEnabled(true);
        }

    }

    public boolean updateServer(String address) {
        boolean success = false;

        try {
            client.setAddress(address);
            server_label.setText("Server: " + address);
            refresh_button.setEnabled(true);

            success = true;
        } catch (UnknownHostException e) {
            server_label.setText("Server:");
            refresh_button.setEnabled(false);

            Toast toast = Toast.makeText(this, "Unknown host", Toast.LENGTH_SHORT);
            toast.show();
        }

        renderConfigs(new ArrayList<Config>());

        return success;
    }

}
