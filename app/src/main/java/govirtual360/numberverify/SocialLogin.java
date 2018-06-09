package govirtual360.numberverify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class SocialLogin extends  AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 7;
    private static final int FB_SIGN_IN = 2;
    private CallbackManager callbackManager;
    LoginButton fbLoginButton;
    private AccessTokenTracker accessTokenTracker;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    private LinearLayout llProfileLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_social_login);
        btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        imgProfilePic = (ImageView) findViewById(R.id.UsrerprofilePic);
        txtName = (TextView) findViewById(R.id.GUser_name);
        userId = (TextView) findViewById(R.id.UserId);
        txtEmail = (TextView) findViewById(R.id.email);
        fbLoginButton=findViewById(R.id.Flogin_button);

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
        /*Sign in With FaceBook*/
        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                Log.d("AccessToken",oldToken+","+newToken);
            }
        };
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker.startTracking();
//        profileTracker.startTracking();
        fbLoginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
//Intent in= FacebookActivity.class;
        fbLoginButton.registerCallback(callbackManager,
                // If the login attempt is successful, then call onSuccess and pass the LoginResult//
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Print the user’s ID and the Auth Token to Android Studio’s Logcat Monitor//
                        Log.d(TAG, "User ID: " +
                                loginResult.getAccessToken().getUserId() + "\n" +
                                "Auth Token: " + loginResult.getAccessToken().getToken());
                        AccessToken accessToken = loginResult.getAccessToken();
                        getUserDetails(accessToken);
                        Profile profile = Profile.getCurrentProfile();
                       // displayMessage(profile);
                    }

                    // If the user cancels the login, then call onCancel//
                    @Override
                    public void onCancel() {
                        Log.w("Cancel","inFacebook");
                    }

                    // If an error occurs, then call onError//
                    @Override
                    public void onError(FacebookException exception) {
                        Log.w("Erron in facebook",exception.toString());
                    }
                });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customizing G+ button
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setScopes(gso.getScopeArray());
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id) {
            case R.id.sign_in_button:
                signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            case R.id.btn_revoke_access:
                revokeAccess();
                break;
        }

    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess())
        {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            String id = acct.getId();
            String personPhotoUrl="";
            try {
                    personPhotoUrl = Objects.requireNonNull(acct.getPhotoUrl()).toString();
                    Picasso.with(getApplicationContext()).load(personPhotoUrl).error(R.drawable.common_google_signin_btn_icon_light)
                            .into(imgProfilePic);
            }
            catch (Exception ignored){
                imgProfilePic.setImageResource(R.drawable.common_google_signin_btn_icon_light_normal);
            }
            finally {
                String email = acct.getEmail();
                Log.e(TAG, "Name: " + personName + ", email: " + email
                        + ", Image: " + personPhotoUrl+ ", UserId: " + id);

                txtName.setText(personName);
                txtEmail.setText(email);
                userId.setText(id);
                updateUI(true);
            }

        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void signWithFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Log.d("inSign","inSignUp"+isLoggedIn);
        if (isLoggedIn)
            getUserDetails(accessToken);
        // Register your callback//

    }
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
              btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
//        else   if (requestCode == FB_SIGN_IN)
        {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Log.d("inSign","inSignUp"+isLoggedIn);
        if (isLoggedIn) {

            updateUI(false);
            btnSignIn.setVisibility(View.INVISIBLE);
            getUserDetails(accessToken);
        }
        else {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
            fbLoginButton.setVisibility(View.INVISIBLE);
        }
        else{
            updateUI(false);
            fbLoginButton.setVisibility(View.VISIBLE);/*else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });*/}

        }
    }
    protected void getUserDetails(AccessToken accessToken) {
        Log.w("inAccess",accessToken.toString());
        GraphRequest data_request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject json_object,
                            GraphResponse response) {

                        if (json_object!=null)
                        {
                            try {
                                if (json_object.has("email"))
                                    Log.w("\nEmail",json_object.getString("email"));
                                else
                                    Log.w("\nEmail","Does not exist.");
                                Log.w("\nId",json_object.getString("id"));
                                Log.w("\nName",json_object.getString("name"));
                                Log.w("\nbirthday",json_object.getString("birthday"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            setProfile(json_object);
                        }
                        Log.w("Responce",response+"");
                    }

                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,birthday,picture.width(60).height(60)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();

    }
    public void setProfile( JSONObject response )
    {

        Log.w("setProfile",response+"");
        JSONObject  profile_pic_data, profile_pic_url;
        try {
//        response = new JSONObject(jsondata);
/*if (response.get("email")!=null)
        Log.w("Email",response.get("email").toString());
else
    Log.w("Email"," not exists");*/

            txtName.setText(response.get("name").toString());
            userId.setText(response.get("id").toString());
            profile_pic_data = new JSONObject(response.get("picture").toString());
            profile_pic_url = new JSONObject(profile_pic_data.getString("data"));
            Log.w("\nPic",profile_pic_url.getString("url"));
//        Picasso.with(this).load(profile_pic_url.getString("url"))
            Log.w("ImageUrl","http://graph.facebook.com/" + response.getString("id") + "/picture?type=large");
            Picasso.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(60,60))
                    .into(imgProfilePic);

        } catch(Exception e){
            e.printStackTrace();
            Log.w("Error"," in loading profile");
        }
    }

}
