// In Class Assignment 04
// MainActivity.java
// Sharan Girdhani     - 800960333
// Salman Mujtaba   - 800969897
//

package com.example.sharangirdhani.inclass04;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharangirdhani.inclass04.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Handler handler;

    final static int COUNT_PASSWORDS = 5;

    private ArrayList<String> passwords;

    private EditText edtName;
    private EditText edtDept;
    private EditText edtAge;
    private EditText edtZIP;


    private ProgressDialog progressDialog;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.progressLabel));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        // Setup handler for threading
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    // Start generating Passwords
                    case GeneratePasswords.STATUS_START:
                        progressDialog.show();
                        progressDialog.setProgress(0);
                        break;
                    // Update progress
                    case GeneratePasswords.STATUS_PROGRESS:
                        progressDialog.setProgress((int) msg.obj);
                        break;
                    // End generating passwords
                    case GeneratePasswords.STATUS_DONE:
                        passwords = msg.getData().getStringArrayList("PASSWORDS");
                        progressDialog.dismiss();
                        showPasswords();
                        break;
                }
                return false;
            }
        });


    }

    public void showPasswords(){
        // Create new alert dialog to pick password
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.alertLabel));

        CharSequence[] psw = passwords.toArray(new CharSequence[passwords.size()]);
        alertDialog.setItems(psw, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binding.textViewShowPass.setText(passwords.get(which));
            }
        });
        alertDialog.show();
    }

    public ArrayList<String> getInputData() {
        ArrayList<String> input = new ArrayList<>();
        input.add(binding.edtName.getText().toString());
        input.add(binding.edtDept.getText().toString());
        input.add(binding.edtAge.getText().toString());
        input.add(binding.edtZIP.getText().toString());
        return input;
    }


    // Handle button clicks
    public void generatePasswordsUsingThreads(View v){
        if(!isValid()) {
            Toast.makeText(this, getResources().getString(R.string.errGen), Toast.LENGTH_SHORT).show();
        }
        else if(!isNameValid()) {
            Toast.makeText(this, getResources().getString(R.string.errName), Toast.LENGTH_SHORT).show();
        }
        else if(!isAgeValid()) {
            Toast.makeText(this, getResources().getString(R.string.errAge), Toast.LENGTH_SHORT).show();
        }
        else if(!isZIPValid()) {
            Toast.makeText(this, getResources().getString(R.string.errZIP), Toast.LENGTH_SHORT).show();
        }
        else if(!isDeptValid()) {
            Toast.makeText(this, getResources().getString(R.string.errDept), Toast.LENGTH_SHORT).show();
        }
        else {
            ArrayList<String> inputData = getInputData();

            // Create a thread pool of 2
            ExecutorService threadPool = Executors.newFixedThreadPool(2);
            threadPool.execute(new GeneratePasswords(inputData.get(0), inputData.get(1), inputData.get(2), inputData.get(3)));
        }

    }

    public void generatePasswordsUsingAsync(View v){
        if(!isValid()) {
            Toast.makeText(this, getResources().getString(R.string.errGen), Toast.LENGTH_SHORT).show();
        }
        else if(!isNameValid()) {
            Toast.makeText(this, getResources().getString(R.string.errName), Toast.LENGTH_SHORT).show();
        }
        else if(!isAgeValid()) {
            Toast.makeText(this, getResources().getString(R.string.errAge), Toast.LENGTH_SHORT).show();
        }
        else if(!isZIPValid()) {
            Toast.makeText(this, getResources().getString(R.string.errZIP), Toast.LENGTH_SHORT).show();
        }
        else if(!isDeptValid()) {
            Toast.makeText(this, getResources().getString(R.string.errDept), Toast.LENGTH_SHORT).show();
        }
        else {
            ArrayList<String> inputData = getInputData();
            new GeneratePasswordsAsync().execute(inputData.get(0), inputData.get(1), inputData.get(2), inputData.get(3));
        }
    }

    // Inner class implementing runnables for THREAD implementation
    class GeneratePasswords implements Runnable{

        ArrayList<String> passwords = new ArrayList<>();
        Message msg = new Message();
        Bundle msgData = new Bundle();
        String Name;
        String Dept;
        int Age;
        int ZIP;

        public GeneratePasswords(String name, String dept, String age, String ZIP) {
            this.Name = name;
            this.Dept = dept;
            this.Age = Integer.parseInt(age);
            this.ZIP = Integer.parseInt(ZIP);
        }

        // Message Codes
        final static int STATUS_START = 0x00;
        final static int STATUS_PROGRESS = 0x03;
        final static int STATUS_DONE = 0x01;

        // Run in background
        @Override
        public void run() {
            // Send Start message
            msg = new Message();
            msg.what = STATUS_START;
            handler.sendMessage(msg);

            for (int index = 1; index <= COUNT_PASSWORDS; index++){
                // Use utility to generate password
                passwords.add(Util.getPassword(this.Name, this.Dept, this.Age, this.ZIP));
                // Send progress message
                msg = new Message();
                msg.what = STATUS_PROGRESS;
                int progress = 100 / COUNT_PASSWORDS;
                msg.obj = index * progress;
                handler.sendMessage(msg);
            }
            // Send complete message
            msg = new Message();
            msgData.putStringArrayList("PASSWORDS",passwords);
            msg.what = STATUS_DONE;
            msg.setData(msgData);
            handler.sendMessage(msg);
        }
    }


    // Inner class for AsyncTask implementation
    class GeneratePasswordsAsync extends AsyncTask<String, Integer, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            ArrayList<String> passwords = new ArrayList<>();

            String Name = params[0];
            String DEPT = params[1];
            int Age = Integer.parseInt(params[2]);
            int ZIP = Integer.parseInt(params[3]);

            for (int index = 1; index <= COUNT_PASSWORDS; index++){
                passwords.add(Util.getPassword(Name, DEPT, Age, ZIP));
                int progressStep = 100 / COUNT_PASSWORDS;
                publishProgress(index * progressStep);
            }
            return passwords;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            progressDialog.setProgress(0);
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
            passwords = strings;
            showPasswords();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }
    }

    public boolean isNameValid() {
        String name = binding.edtName.getText().toString();
        if(name == null || name == "") {
            return false;
        }
        return true;
    }

    public boolean isDeptValid() {
        String dept = binding.edtDept.getText().toString();
        if(dept == null || dept == "") {
            return false;
        }
        return true;
    }

    public boolean isAgeValid() {
        try {
            int age = Integer.parseInt(binding.edtAge.getText().toString());
            // Setting Age limit between (0,150]
            if(age <= 0 || age > 150) {
                return false;
            }
            return true;
        } catch (Exception r) {
            return false;
        }

    }

    public boolean isZIPValid() {
        try {
            int zip = Integer.parseInt(binding.edtZIP.getText().toString());
            if(!(zip >= 10000 && zip <= 99999)) {
                return false;
            }
            return true;
        } catch (Exception r) {
            return false;
        }

    }

    public boolean isValid() {
        ArrayList<String> arr = getInputData();
        if(arr.get(0).isEmpty() || arr.get(1).isEmpty() || arr.get(2).isEmpty() || arr.get(3).isEmpty()) {
            return false;
        }
        return true;
    }

    public void buttonClear(View view) {
        binding.edtName.setText("");
        binding.edtDept.setText("");
        binding.edtAge.setText("");
        binding.edtZIP.setText("");
        passwords.clear();
        binding.textViewShowPass.setText("");
    }

    public void buttonClose(View view) {
        finish();
    }

}
