package taylor.gerard.hw4;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class AlienLocationService extends Service {

    private RequestThread requestThread; // request thread field.
    List<Reporter> reporters = new ArrayList<>();

    // override methods //

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //when destroying the service we want to interrupt the thread.

    @Override
    public void onDestroy() {
        requestThread.interrupt();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public synchronized IBinder  onBind(Intent intent) {
        if(requestThread == null){ // if thread is null initialize it and start it.
            requestThread = new RequestThread();
            requestThread.start();
        }
        return new AlienLocationServiceBinder(); // return the binder.
    }

    public interface Reporter {
        void report(List<UFOPosition> positions);
    }

    //binder class for reporting purposes.

    public class AlienLocationServiceBinder extends Binder {
        public void addReporter(Reporter reporter){
            reporters.add(reporter);
        }

        public void removeReporter(Reporter reporter){
            reporters.remove(reporter);
        }
    }

    //REST REQUEST.

    public class RequestThread extends Thread {

        public static final String URISTRING = "http://javadude.com/aliens/"; // ROOT URI

        @Override
        public void run() {
            int i = 1; // start at first page.
            while(true){ // loop until 404
                try {
                    URL url = new URL(URISTRING + i + ".json"); // URL concat.
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); // open connection
                    urlConnection.connect(); // connect.
                    if(urlConnection.getResponseCode() == 404){ // if bad request
                        for(Reporter reporter : reporters){
                            reporter.report(null); // make all reporters null
                        }
                        break; // break out of the loop
                    }

                    InputStream in = urlConnection.getInputStream(); // get inputstream
                    InputStreamReader isr = new InputStreamReader(in); // set isr
                    BufferedReader br = new BufferedReader(isr); // set buffered reader
                    String line; // line holder
                    String content = ""; // total content
                    while((line = br.readLine()) != null){ // read line was line != null
                        content += line + "\n"; // concat to content
                    }
                    //send to all registered reporters.
                    for(Reporter reporter : reporters){
                        reporter.report(parseJsonString(content));
                    }
                    br.close(); // close reader
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //sleep the thread for 1 second.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    interrupt();
                }
                i++; // increment i.
            }
        }

        // JSON PARSING

        public List<UFOPosition> parseJsonString(String s) {
            List<UFOPosition> positions = new ArrayList<>(); //list of UFO Position objects
            try {
                JSONArray jsonArray = new JSONArray(s); // create json array
                for(int i = 0; i < jsonArray.length(); i++){ // iterate over array
                    JSONObject jsonObject = jsonArray.getJSONObject(i); // create jsonObject
                    int ship = jsonObject.getInt("ship"); // get ship number
                    double lat = jsonObject.getDouble("lat"); // get latitude
                    double lon = jsonObject.getDouble("lon"); // get longitutde
                    positions.add(new UFOPosition(ship, lat, lon)); // add to list of ppositions.
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return positions; //return list.
        }
    }
}
