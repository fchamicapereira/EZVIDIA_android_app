package ezvidia.app.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import ezvidia.app.MainActivity;
import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;

public class ApplyConfigTask extends AsyncTask<String, Void, Boolean> {

    Client client;
    MainActivity activity;
    String config;

    final int DELAY = 3000; // 3 seconds

    public ApplyConfigTask(MainActivity activity, Client client, String config) {
        this.client = client;
        this.activity = activity;
        this.config = config;
    }

    @Override
    protected void onPreExecute() {
        activity.lock();
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            client.apply(config);
        } catch (IOException e) {
            return new Boolean(false);

        }

        return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean success) {

        if (!success.booleanValue()) {

            String message = "No response from server";
            int duration = Toast.LENGTH_SHORT;

            Context context = activity.getApplicationContext();
            Toast toast = Toast.makeText(context, message, duration);

            toast.show();

            return;
        }

        String message = String.format("Applied configuration \"%s\"", config);
        int duration = Toast.LENGTH_SHORT;

        Context context = activity.getApplicationContext();
        Toast toast = Toast.makeText(context, message, duration);

        toast.show();

        activity.unlock(DELAY);
    }

}
