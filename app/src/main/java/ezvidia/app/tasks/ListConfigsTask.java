package ezvidia.app.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import ezvidia.app.MainActivity;
import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;

public class ListConfigsTask extends AsyncTask<String, Void, ArrayList<Config>> {

    Client client;
    MainActivity activity;
    boolean error = false;

    final int DELAY = 500; // 0.5 seconds

    public ListConfigsTask(MainActivity activity, Client client) {
        this.client = client;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.lock();
    }

    @Override
    protected ArrayList<Config> doInBackground(String... params) {
        ArrayList<Config> configs = new ArrayList<>();

        try {
            configs = client.list();
        } catch (IOException e) {
            error = true;
        }

        return configs;
    }

    @Override
    protected void onPostExecute(ArrayList<Config> configs) {

        if (error) {
            String message = "No response from server";
            int duration = Toast.LENGTH_SHORT;

            Context context = activity.getApplicationContext();
            Toast toast = Toast.makeText(context, message, duration);

            toast.show();
        }

        activity.renderConfigs(configs);
        activity.unlock(DELAY);
    }

}
