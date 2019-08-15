package ezvidia.app;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import ezvidia.app.domain.Client;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SetAddressPopupWindow extends PopupWindow {

    private View popupView;
    private PopupWindow set_server_popup;
    private MainActivity parent;
    private Client client;

    private SetAddressPopupWindow instance;

    public SetAddressPopupWindow(MainActivity activity, Client parent_client) {
        parent = activity;
        client = parent_client;
        instance = this;
    }

    public void open() {
        LayoutInflater inflater = (LayoutInflater) parent.getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.set_server_address_popup, null);

        View root = popupView.getRootView();

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        set_server_popup = new PopupWindow(popupView, width, height, true);
        set_server_popup.showAtLocation(root, Gravity.CENTER, 0, 0);

        dimBackground();

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                close();
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

        Button save_address_button = popupView.findViewById(R.id.save_address_button);

        save_address_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText address_input = popupView.findViewById(R.id.address_input);
                String address = address_input.getText().toString();

                if (parent.updateServer(address)) {
                    close();
                }

            }
        });

        parent.lock();
    }

    public void close() {
        undimBackground();
        set_server_popup.dismiss();
        parent.unlock();
    }

    private void dimBackground() {
        View root = popupView.getRootView();
        Context context = set_server_popup.getContentView().getContext();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) root.getLayoutParams();

        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        p.dimAmount = 0.3f;
        wm.updateViewLayout(root, p);
    }

    private void undimBackground() {
        View root = popupView.getRootView();
        Context context = set_server_popup.getContentView().getContext();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) root.getLayoutParams();

        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        p.dimAmount = 0.0f;
        wm.updateViewLayout(root, p);
    }

}
