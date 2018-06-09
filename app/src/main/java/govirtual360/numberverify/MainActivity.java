package govirtual360.numberverify;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements
        View.OnClickListener {

    private static final String TAG = "FirebaseEmailPassword";
    private CallbackManager callbackManager;
    private TextView txtStatus;
    private TextView txtDetail;
    private EditText edtEmail;
    private EditText edtPassword;
    private TextView textView;
    LoginButton fbLoginButton;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    ImageView imageView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);
        txtStatus =  findViewById(R.id.status);
        txtDetail = findViewById(R.id.detail);
        edtEmail =  findViewById(R.id.edt_email);
        edtPassword =  findViewById(R.id.edt_password);
        fbLoginButton=findViewById(R.id.login_button);
         imageView=findViewById(R.id.profilePic);
// Initialize your instance of callbackManager//
        callbackManager = CallbackManager.Factory.create();
        findViewById(R.id.btn_email_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_email_create_account).setOnClickListener(this);
        findViewById(R.id.btn_sign_out).setOnClickListener(this);
        findViewById(R.id.btn_verify_email).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
            //signWithFacebook();
        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                Log.d("AccessToken",oldToken+","+newToken);
            }
        };
    /*    profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                Log.w("profileTrackerN",newProfile+"");
                Log.w("\nprofileTrackerO",oldProfile+"");
                    signWithFacebook();
                //displayMessage(newProfile);
            }
        };*/
        accessTokenTracker.startTracking();
//        profileTracker.startTracking();
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
//        fbLoginButton.setReadP+ermissions("Name");

//        LoginManager.getInstance()
//        fbLoginButton.setOnClickListener(this);
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
                        displayMessage(profile);
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
        // Add code to print out the key hash
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "govirtual360.numberverify",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/
    }

    private void signWithFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Log.d("inSign","inSignUp"+isLoggedIn);
        if (isLoggedIn)
            getUserDetails(accessToken);
        // Register your callback//

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
//        profileTracker.stopTracking();
        AppEventsLogger.deactivateApp(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        AppEventsLogger.activateApp(this);
        signWithFacebook();
//        displayMessage(profile);
    }
    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.btn_email_create_account) {
            createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
        } else if (i == R.id.btn_email_sign_in) {
            signIn(edtEmail.getText().toString(), edtPassword.getText().toString());
        } else if (i == R.id.btn_sign_out) {
            signOut();
        } else if (i == R.id.btn_verify_email) {
            sendEmailVerification();
        }
        else if (i==R.id.login_button)
        {
            Intent intent=new Intent(MainActivity.this, FacebookActivity.class);
            startActivityForResult(intent,2);
        }
    }

    private void createAccount(String email, String password) {
        Log.e(TAG, "createAccount:" + email);
        if (!validateForm(email, password)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "createAccount: Success!");

                            // update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.e(TAG, "createAccount: Fail!", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.e(TAG, "signIn:" + email);
        if (!validateForm(email, password)) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "signIn: Success!");

                            // update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.e(TAG, "signIn: Fail!", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        if (!task.isSuccessful()) {
                            txtStatus.setText("Authentication failed!");
                        }
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        // Disable Verify Email button
        findViewById(R.id.btn_verify_email).setEnabled(false);

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Re-enable Verify Email button
                        findViewById(R.id.btn_verify_email).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification failed!", task.getException());
                            Toast.makeText(getApplicationContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            txtStatus.setText("User Email: " + user.getEmail() + "(verified: " + user.isEmailVerified() + ")");
            txtDetail.setText("Firebase User ID: " + user.getUid());

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.layout_signed_in_buttons).setVisibility(View.VISIBLE);

            findViewById(R.id.btn_verify_email).setEnabled(!user.isEmailVerified());
        } else {
            txtStatus.setText("Signed Out");
            txtDetail.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_signed_in_buttons).setVisibility(View.GONE);
        }
    }
    // Override the onActivityResult method and pass its parameters to the callbackManager//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(data!=null)
            Log.d("data",data.toString()+"");
        Log.d("request",String.valueOf(resultCode)+"");
    }
    private void displayMessage(Profile profile){

        if(profile != null){

            Log.d("UserName",profile.getName());
            Log.d("Profile",profile.getProfilePictureUri(50,50)+"");
            txtDetail.setText(profile.getName());
            Picasso.with(this).load(profile.getProfilePictureUri(50,50))
                    .into(imageView);
        }
        else
            Log.d("Profile"," is null.");

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
//                        Intent intent = new Intent(MainActivity.this,
//                                UserProfile.class);
//                        intent.putExtra("userProfile", json_object.toString());
//                        startActivity(intent);
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

/*{
    private FirebaseAuth mAuth;
    private final String TAG="auth";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();


        mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Log.d(TAG, "onCodeSent:" + s);
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "8533802345",        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }*/
public void setProfile( JSONObject response )
{
    Log.w("setProfile",response+"");
    JSONObject response1, profile_pic_data, profile_pic_url;
    try {
//        response = new JSONObject(jsondata);
/*if (response.get("email")!=null)
        Log.w("Email",response.get("email").toString());
else
    Log.w("Email"," not exists");*/

        txtDetail.setText(response.get("name").toString());
        txtDetail.setVisibility(View.VISIBLE);
        profile_pic_data = new JSONObject(response.get("picture").toString());
        profile_pic_url = new JSONObject(profile_pic_data.getString("data"));
        Log.w("\nPic",profile_pic_url.getString("url"));
//        Picasso.with(this).load(profile_pic_url.getString("url"))
        Log.w("ImageUrl","http://graph.facebook.com/" + response.getString("id") + "/picture?type=large");
        Picasso.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(60,60))
                .into(imageView);

    } catch(Exception e){
        e.printStackTrace();
        Log.w("Error"," in loading profile");
    }
}
}
