package ezvidia.app.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

import ezvidia.app.MainActivity;
import ezvidia.app.domain.Client;
import ezvidia.app.domain.Config;

public class ListConfigsTask extends AsyncTask<String, Void, ArrayList<Config>> {

    Client client;
    MainActivity activity;

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
            e.printStackTrace();
        }

        return configs;
    }

    @Override
    protected void onPostExecute(ArrayList<Config> configs) {
        activity.renderConfigs(configs);
        activity.unlock();
    }

}
