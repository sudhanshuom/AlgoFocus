package com.app.algofocus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;

public class UserLogIn extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog dialog;
    private FirebaseAuth mAuth;
    private EditText email, pass;
    private Animation shakeAnimation;
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_log_in);

        mAuth = FirebaseAuth.getInstance();
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation);

        email = findViewById(R.id.user_login_email);
        pass = findViewById(R.id.user_login_password);
        ImageButton googleSignIn = findViewById(R.id.appsignupGSignin);
        LoginButton facebookSignIn = findViewById(R.id.appsignupFSignin);
        Button btn = findViewById(R.id.user_login_signinbtn);
        TextView su = findViewById(R.id.user_login_signuptv);

        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserLogIn.this, SignUp.class));
                finish();
            }
        });

        /*
         * Action when Clicked on btn for log in with email and password.
         * */
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    String em = email.getText().toString();
                    String pas = pass.getText().toString();

                    dialog = ProgressDialog.show(UserLogIn.this, "",
                            "Please wait...", true);
                    dialog.show();

                    mAuth.signInWithEmailAndPassword(em, pas)
                            .addOnCompleteListener(UserLogIn.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("EPSignIn", "signInWithEmail:success");
                                        startActivity(new Intent(UserLogIn.this, MainActivity.class));
                                        dialog.cancel();
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("EPSignIn", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(UserLogIn.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }

                                    // ...
                                }
                            });
                }
            }
        });

        /*
         * Action when Clicked on Google btn for log in.
         * */
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(UserLogIn.this, gso);

                googleSignIn();
            }
        });

        /*
         * Action when Clicked on Facebook btn for log in.
         * */
        mCallbackManager = CallbackManager.Factory.create();
        facebookSignIn.setReadPermissions(Collections.singletonList("email"));
        facebookSignIn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("fbsign", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("fbsign", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("fbsign", "facebook:onError", error);
                // ...
            }
        });

    }

    private boolean isValid() {
        if (email.getText().toString().trim().length() == 0) {
            email.startAnimation(shakeAnimation);
            email.setError("Enter valid Email");
            return false;
        } else if (pass.getText().toString().trim().length() == 0) {
            pass.startAnimation(shakeAnimation);
            pass.setError("Enter valid password");
            return false;
        }
        return true;
    }


    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            dialog = ProgressDialog.show(UserLogIn.this, "",
                    "Please wait...", true);
            dialog.show();
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

            SharedPreferences.Editor myEdit = sharedPreferences.edit();

            assert account != null;
            myEdit.putString("email", account.getEmail());
            myEdit.putString("name", account.getDisplayName());

            myEdit.apply();
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(UserLogIn.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            //dialog.cancel();
            Log.e("GSignFailed", "signInResult:failed code=" + e.getStatusCode());
            dialog.cancel();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e("firebaseAuthWithGoogle", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("GSignin1", "signInWithCredential:success");
                            startActivity(new Intent(UserLogIn.this, MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("GSignin2", "signInWithCredential:failure", task.getException());
                            Toast.makeText(UserLogIn.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.e("fbsignhft", "handleFacebookAccessToken:" + token);
        final ProgressDialog dialog = ProgressDialog.show(UserLogIn.this, "",
                "Please wait...", true);
        dialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("fbsignhft2", "signInWithCredential:success");
                            startActivity(new Intent(UserLogIn.this, MainActivity.class));
                            LoginManager.getInstance().logOut();
                            dialog.cancel();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("fbsignhft3", "signInWithCredential:failure", task.getException());
                            Toast.makeText(UserLogIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            LoginManager.getInstance().logOut();
                            dialog.cancel();
                        }

                        // ...
                    }
                });
    }

}
