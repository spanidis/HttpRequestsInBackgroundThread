package el.ps.httprequestsinthread;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Button btnMakCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMakCall = findViewById(R.id.btnCall);

        btnMakCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SynchronousHttpRequests().executeRequests();
            }
        });
    }

    private class SynchronousHttpRequests {

        // Main executor for background tasks
        private ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Handler to post results to the main thread
        private Handler handler = new Handler(Looper.getMainLooper());

        public void executeRequests() {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // First HTTP request
                        String response1 = makeHttpRequest("http://nextetruck.nng.com:5001/?ApiKey=WsJhIClUoGhV1ohCNFnAxk5tJT7hAd6X3qkIhbgBNOMMXIpgsgd1WUoFKahck8wW&SetPathFormat=CompressedJSON&Vehicle=Certh_Vehicle1");
                        Log.d("HTTP Response 1", response1);

                        if(response1.equals("Request accepted, but processing not complete."))
                        {
                            // Second HTTP request
                            String response2 = makeHttpRequest("http://nextetruck.nng.com:5001/?ApiKey=WsJhIClUoGhV1ohCNFnAxk5tJT7hAd6X3qkIhbgBNOMMXIpgsgd1WUoFKahck8wW&TargetCoordinate=40.5220970,22.2030905&Vehicle=Certh_Vehicle1");
                            Log.d("HTTP Response 2", response2);

                            if(response2.equals("Request accepted, but processing not complete."))
                            {
                                // Third HTTP request
                                String response3 = makeHttpRequest("http://nextetruck.nng.com:5001/?ApiKey=WsJhIClUoGhV1ohCNFnAxk5tJT7hAd6X3qkIhbgBNOMMXIpgsgd1WUoFKahck8wW&SourceCoordinate=40.5678963,22.9966733:3.51&Vehicle=Certh_Vehicle1");
                                Log.d("HTTP Response 3", response3);
                            }
                        }

                        // Post results to the main thread if necessary
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Update UI with results here if needed
                            }
                        });
                    } catch (Exception e) {
                        Log.e("SynchronousHttpRequests", "Error during HTTP requests", e);
                        e.printStackTrace();
                    }
                }
            });
        }

        private String makeHttpRequest(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } else {
                    Log.w("SynchronousHttpRequests", "Non-OK response: " + responseCode + " for URL: " + urlString);
                    if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                        // Handle 202 Accepted specifically if needed
                        return "Request accepted, but processing not complete.";
                    } else {
                        throw new Exception("Failed to make request: " + responseCode);
                    }
                }
            }
            catch (Exception e) {
                Log.e("SynchronousHttpRequests", "Error in makeHttpRequest: " + urlString, e);
                throw e;
            }
            finally {
                urlConnection.disconnect();
            }
        }
    }
}