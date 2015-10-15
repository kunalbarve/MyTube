package com.cmpe277.lab2.mytube;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.cmpe277.lab2.mytube.Utility.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Came in create------>", "Try may be successful");

       session = new SessionManager(getApplicationContext());

        if(session.checkLogin()){
            finish();
        }else{
            setContentView(R.layout.activity_main);
            findViewById(R.id.sign_in_button).setOnClickListener(this);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .addScope(new Scope(Scopes.EMAIL))
                    .build();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        mShouldResolve = false;

        Log.d("MYTUBE", "Is loggedin" + session.isLoggedIn() + ":" + session.isDisconnected());
        if(!session.isLoggedIn() && session.isDisconnected()){
            Log.d("MYTUBE", "inside if before:" + session.isLoggedIn() + ":" + session.isDisconnected());
            session.setIsDisconnected(false);
            Log.d("MYTUBE", "inside if before:" + session.isLoggedIn() + ":" + session.isDisconnected());
            session.createLoginSession("Test", "Test@mail");
            //new Connection().execute("");
            //new GetToken().execute("");
            finish();
        }else{
            Log.d("MYTUBE", "inside else");
            onSignOutClicked();
            Log.d("MYTUBE", "Before:"+session.isDisconnected());
            session.setIsDisconnected(true);
            Log.d("MYTUBE", "After:"+session.isDisconnected());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class Connection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(Constatnts.requestAccessUrl());
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.d("MYTUBE","Result>>>"+convertStreamToString(in));

            }catch (Exception e){
                Log.e("MYTUBE","EXCEPTION",e);
            }finally {
                urlConnection.disconnect();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class GetToken extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                String urlParameters  = Constatnts.requestTokenParameters();
                byte[] postData       = urlParameters.getBytes("UTF-8");
                int    postDataLength = postData.length;
                String request        = Constatnts.ACCESS_TOKEN_URL;

                URL url = new URL(request);
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setDoOutput( true );
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                conn.setUseCaches(false);
                DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
                wr.write(postData);

                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                Log.d("MYTUBE", "Result>>>" + response.toString());

//                InputStream in = conn.getInputStream();
//                Log.d("MYTUBE", "Result>>>" + convertStreamToString(in));
            }catch (Exception e){
                Log.e("MYTUBE","TOKEN EXCEPTION",e);
            }


            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void getCode(){

    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "EMPTY";
        }
    }


    @Override
    public void onClick(View v) {
        Log.d("Came in On Click", "Clicked the button");
        if (v.getId() == R.id.sign_in_button) {
            onSignInClicked();
        }

        // ...
    }

    private void onSignInClicked() {
        Log.d("Came in Sign In","Sign in successful");
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();

        // Show a message to the user that we are signing in.

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.

        Log.d("failed Con>>", "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                    Log.d("Came in conn fail--->","Try may be successful");
                } catch (IntentSender.SendIntentException e) {
                    Log.d("Came in conn fail--->","Try may be exception");
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.

            }
        } else {
            // Show the signed-out UI

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d("Came in Response >>","Bad Luck Bro");
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }

        //ShowSignedOutUI
    }


}
