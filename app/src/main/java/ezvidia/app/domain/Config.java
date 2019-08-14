package ezvidia.app.domain;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import ezvidia.app.MainActivity;

public class Config {

    private String name;

    public Config(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void render(MainActivity activity, ViewGroup view) {
        Button myButton = new Button(activity);
        myButton.setText(name);
        myButton.setHeight(250);
        myButton.setOnClickListener(activity.applyListener);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        view.addView(myButton, lp);
    }
}
