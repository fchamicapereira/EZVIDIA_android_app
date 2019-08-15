package ezvidia.app;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroupOverlay;
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
    public static final String SAVED_ADDRESS_KEY = "ADDRESS";

    private ConstraintLayout root;
    private LinearLayout configs_container;
    private TextView server_label;

    private Button set_server_button;
    private Button refresh_button;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.root);
        configs_container = findViewById(R.id.configs_container);
        server_label = findViewById(R.id.server_label);
        refresh_button = findViewById(R.id.refresh_button);
        set_server_button = findViewById(R.id.set_server_button);

        client = new Client();

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

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.set_server_address_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popup_set_server = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popup_set_server.showAtLocation(v, Gravity.CENTER, 0, 0);

        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, root.getWidth(), root.getHeight());
        dim.setAlpha((int) (255 * 0.5f));

        ViewGroupOverlay overlay = root.getOverlay();
        overlay.add(dim);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popup_set_server.dismiss();
                ViewGroupOverlay overlay = root.getOverlay();
                overlay.clear();

                return true;
            }
        });

        EditText address_input = popupView.findViewById(R.id.address_input);

        address_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button save_button = popupView.findViewById(R.id.save_address_button);
                String address = s.toString();

                boolean valid = client.validateIpAddress(address);
                save_button.setEnabled(valid);
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });

        Button save_address_buton = popupView.findViewById(R.id.save_address_button);

        save_address_buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText address_input = popupView.findViewById(R.id.address_input);
                String address = address_input.getText().toString();

                if (updateServer(address)) {
                    popup_set_server.dismiss();
                    ViewGroupOverlay overlay = root.getOverlay();
                    overlay.clear();
                }

            }
        });

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
