package com.example.drago.senddataapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse{
    public static final MediaType FORM_DATA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private final String oldURL = "https://docs.google.com/forms/d/1U7hJDI5br8rYoKiXTo5J0Kh3n9xztI07dDNLO9gIhCY/formResponse";
    private final String newURL = "https://docs.google.com/forms/d/1rw99PhRbHt8msdHyT9XMaVftszPc0hta2RW-UosU0YI/formResponse";
    private final String[] spreadsheetURLs = {oldURL, newURL};
    private int currentSpreadsheet = 1;

    //input element ids found from the live form page
    public static final String[] NAME_KEY = {"", "entry_425409985"};
    public static final String[] TEAM_NUMBER_KEY = {"entry_1577205917", "entry_1450594725"};
    public static final String[] MATCH_NUMBER_KEY = {"", "entry_1966563731"};
    public static final String[] BREACHES_DEFENSES_IN_AUTONOMOUS_KEY = {"entry_1309176866", "entry_2004340847"};
    public static final String[] DEFENSE_BREACHED_IN_AUTONOMOUS_KEY = {"", "entry_655027564"};
    public static final String[] SCORE_IN_AUTONOMOUS_KEY = {"entry_365970572", "entry_1072905336"};
    public static final String[] MISS_IN_HIGH_GOAL_KEY = {"", "entry_257301727"};
    public static final String[] SCORE_IN_HIGH_GOAL_KEY = {"entry_1686842457", "entry_1861626737"};
    public static final String[] MISS_IN_LOW_GOAL_KEY = {"", "entry_1592979112"};
    public static final String[] SCORE_IN_LOW_GOAL_KEY = {"entry_801197027", "entry_2040967630"};
    public static final String[] ROBOT_HANGS_KEY = {"entry_1539080045", "entry_1637215245"};
    public static final String[] ROBOT_DEFENDS_KEY = {"entry_1748393174", "entry_1325768637"};
    public static final String[] ROBOT_DEFENDED_AGAINST_KEY = {"", "entry_446274271"};
    public static final String[] PORTCULLIS_KEY = {"entry_1464581347", "entry_1443136194"};
    public static final String[] CHEVAL_DE_FRISE_KEY = {"entry_1394768057", "entry_2145332144"};
    public static final String[] MOAT_KEY = {"entry_1370432337", "entry_405176034"};
    public static final String[] RAMPARTS_KEY = {"entry_1355477437", "entry_244598787"};
    public static final String[] DRAWBRIDGE_KEY = {"entry_1394300935", "entry_603786741"};
    public static final String[] SALLY_PORT_KEY = {"entry_135357103", "entry_579955452"};
    public static final String[] ROCK_WALL_KEY = {"entry_559108952", "entry_613405738"};
    public static final String[] ROUGH_TERRAIN_KEY = {"entry_439784406", "entry_774349350"};
    public static final String[] LOW_BAR_KEY = {"entry_631241315", "entry_1940807735"};
    public static final String[] COMMENTS_KEY = {"", "entry_1127654160"};

    Context context;

    Button sendButton;
    ListView filesListView;

    ArrayList<File> filesList = new ArrayList<File>();
    private TextView versionText;

    File sentFile;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        sendButton = (Button)findViewById(R.id.sendButton);
        versionText = (TextView)findViewById(R.id.versionText);
        filesListView = (ListView)findViewById(R.id.filesListView);

        versionText.setText("Version: " + BuildConfig.VERSION_NAME + " - Compatible with: V.1.5.0+");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }

    private void send(){
        if(Environment.getExternalStorageState().equals("mounted")){
            filesList.clear();
            walkDir(Environment.getExternalStorageDirectory());

            if(filesList.isEmpty()){
                displayText("File not found");
                return;
            }
            ArrayList<String> filePaths = new ArrayList<>();
            for(File file : filesList){
                filePaths.add(file.getAbsolutePath());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filePaths);
            filesListView.setAdapter(arrayAdapter);

            AdapterView.OnItemLongClickListener onFileClickedListener = new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ArrayList<String> data = new ArrayList();
                    File selectedFile = filesList.get(position);
                    sentFile = selectedFile;
                    data = load(selectedFile);

                    /*
                    for(int i = 0; i < data.size(); i++){
                        String[] entryElements = split(data.get(i));
                        PostDataTask pdt = new PostDataTask(this);
                        pdt.execute(spreadsheetURLs[currentSpreadsheet], entryElements[1], entryElements[2], entryElements[3], entryElements[4],
                                entryElements[5], entryElements[6], entryElements[7], entryElements[8], entryElements[9],
                                entryElements[10], entryElements[11], entryElements[12], entryElements[13], entryElements[14],
                                entryElements[15], entryElements[16], entryElements[17], entryElements[0], entryElements[18], entryElements[19],
                                entryElements[20], entryElements[21], entryElements[22]);
                    }*/
                    sendRoutine(data);
                    return true;
                }
            };

            filesListView.setOnItemLongClickListener(onFileClickedListener);
        }
        else {
            displayText("External storage not writable");
        }
    }

    public void sendRoutine(ArrayList<String> data){
        for(int i = 0; i < data.size(); i++){
            String[] entryElements = split(data.get(i));
            PostDataTask pdt = new PostDataTask(this);
            pdt.execute(spreadsheetURLs[currentSpreadsheet], entryElements[1], entryElements[2], entryElements[3], entryElements[4],
                    entryElements[5], entryElements[6], entryElements[7], entryElements[8], entryElements[9],
                    entryElements[10], entryElements[11], entryElements[12], entryElements[13], entryElements[14],
                    entryElements[15], entryElements[16], entryElements[17], entryElements[0], entryElements[18], entryElements[19],
                    entryElements[20], entryElements[21], entryElements[22]);
        }
    }

    private ArrayList<String> load(File file){
        //Returns an ArrayList with each entry as an element
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch(FileNotFoundException e) { e.printStackTrace();}

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line;
        int i = 0;
        ArrayList<String> data = new ArrayList();
        try {
            while((line=br.readLine()) != null){
                data.add(i, line);
                i++;
            }
        } catch(IOException e){ e.printStackTrace();}
        return data;
    }

    private void walkDir(File dir){
        File[] listFile;
        listFile = dir.listFiles();

        if(listFile != null){
            for(int i = 0; i < listFile.length; i++){
                if(listFile[i].isDirectory()){
                    walkDir(listFile[i]);
                }
                else {
                    if(listFile[i].getName().startsWith("Stronghold Scouting App Data")){
                        filesList.add(listFile[i]);
                    }
                }
            }
        }
    }

    private String[] split(String s){
        String[] temp = s.split(" ");
        String[] temp2 = new String[23];
        String comment = "";
        String name = "";
        boolean commentFound = false;
        boolean nameFound = false;
        int commentMarkerIndex = -1;
        int nameMarkerIndex = -1;


        for(int i = 0; i < temp.length; i++){
            if(commentFound){
                temp2[i - commentMarkerIndex] = temp[i];
            }
            if(temp[i].equals("!@#$%^&")){
                temp2[0] = comment;
                commentFound = true;
                commentMarkerIndex = i;
            }
            else {
                comment += temp[i] + " ";
            }
        }
        return temp2;
    }

    private void displayText(String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void processFinish(boolean result){
        if(result){
            displayText("Data successfully sent!");
            if(!sentFile.delete()){
                displayText("Couldn't delete file");
            }
        }
        else {
            displayText("There was a problem with sending data. It was probably due to lack of Internet");
        }
    }

    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<String, Void, Boolean> {
        public AsyncResponse delegate;
        public PostDataTask(AsyncResponse a){
            this.delegate = a;
        }

        @Override
        protected Boolean doInBackground(String... contactData){
            Boolean result = true;
            String url = contactData[0];
            String postBody = "";
            try {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = TEAM_NUMBER_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[1], "UTF-8") +
                        "&" + BREACHES_DEFENSES_IN_AUTONOMOUS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[2],"UTF-8") +
                        "&" + SCORE_IN_AUTONOMOUS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[3],"UTF-8") +
                        "&" + SCORE_IN_HIGH_GOAL_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[4],"UTF-8") +
                        "&" + SCORE_IN_LOW_GOAL_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[5],"UTF-8") +
                        "&" + ROBOT_HANGS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[6],"UTF-8") +
                        "&" + ROBOT_DEFENDS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[7],"UTF-8") +
                        "&" + PORTCULLIS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[8],"UTF-8") +
                        "&" + CHEVAL_DE_FRISE_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[9],"UTF-8") +
                        "&" + MOAT_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[10],"UTF-8") +
                        "&" + RAMPARTS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[11],"UTF-8") +
                        "&" + DRAWBRIDGE_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[12],"UTF-8") +
                        "&" + SALLY_PORT_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[13],"UTF-8") +
                        "&" + ROCK_WALL_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[14],"UTF-8") +
                        "&" + ROUGH_TERRAIN_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[15],"UTF-8") +
                        "&" + LOW_BAR_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[16],"UTF-8") +
                        "&" + MATCH_NUMBER_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[17],"UTF-8") +
                        "&" + COMMENTS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[18],"UTF-8") +
                        "&" + MISS_IN_HIGH_GOAL_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[19],"UTF-8") +
                        "&" + MISS_IN_LOW_GOAL_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[20],"UTF-8") +
                        "&" + DEFENSE_BREACHED_IN_AUTONOMOUS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[21],"UTF-8") +
                        "&" + NAME_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[22],"UTF-8") +
                        "&" + ROBOT_DEFENDED_AGAINST_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[23],"UTF-8");
                ;
            } catch (UnsupportedEncodingException ex){
                result = false;
            }
            try {
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                //Send the request
                Response response = client.newCall(request).execute();
            }catch (IOException exception){
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result){
            delegate.processFinish(result);
        }
    }
}
