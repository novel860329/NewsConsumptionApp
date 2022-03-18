package com.example.accessibility_detect.naturallanguage;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import com.example.accessibility_detect.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;

import java.io.IOException;
import java.io.InputStream;

;


public class AccessTokenLoader extends AsyncTaskLoader<String> {

    private static final String TAG = "AccessTokenLoader";

    private static final String PREFS = "AccessTokenLoader";
    private static final String PREF_ACCESS_TOKEN = "access_token";

    public AccessTokenLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
//    private String loadCredentialJson(String filename, Context context) {
//        // Log.d("BootCompleteReceiver","In MainActivity loadQuestionnaireJson");
//        try {
//            InputStream is = context.getAssets().open(filename);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            return new String(buffer, "UTF-8");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//
//    }
    @Override
    public String loadInBackground() {

        final SharedPreferences prefs =
                getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String currentToken = prefs.getString(PREF_ACCESS_TOKEN, null);

        // Check if the current token is still valid for a while
        if (currentToken != null) {
            try {
//                GoogleCredentials credential = GoogleCredentials.fromStream(this.getContext().getResources().openRawResource(R.raw.credential));
//                credential.refreshIfExpired();
//                AccessToken token = credential.getAccessToken();
            final GoogleCredential credential = new GoogleCredential()
                    .setAccessToken(currentToken)
                    .createScoped(CloudNaturalLanguageScopes.all());

                final Long seconds = credential.getExpiresInSeconds();
//                final Long seconds = 0L;
                if (seconds != null && seconds > 3600) {
                    return currentToken;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // ***** WARNING *****
        // In this sample, we load the credential from a JSON file stored in a raw resource folder
        // of this client app. You should never do this in your app. Instead, store the file in your
        // server and obtain an access token from there.
        // *******************
        final InputStream stream = getContext().getResources().openRawResource(R.raw.credential);
        try {
//            GoogleCredentials credential = GoogleCredentials.fromStream(this.getContext().getResources().openRawResource(R.raw.credential));
//            credential.refreshIfExpired();
//            AccessToken accessToken = credential.getAccessToken();
            final GoogleCredential credential = GoogleCredential.fromStream(stream)
                    .createScoped(CloudNaturalLanguageScopes.all());
            credential.refreshToken();
            final String accessToken = credential.getAccessToken();
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply();
            return accessToken;
        } catch (IOException e) {
            Log.e(TAG, "Failed to obtain access token.", e);
        }
        return null;
    }
}
