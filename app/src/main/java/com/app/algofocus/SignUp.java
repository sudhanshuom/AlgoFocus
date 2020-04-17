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
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Collections;

public class SignUp extends AppCompatActivity {

    EditText email, name, pass, pass2;
    private Animation shakeAnimation;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 10;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog dialog;
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation);

        email = findViewById(R.id.Appsignupemail);
        name = findViewById(R.id.Appsignupfn);
        pass = findViewById(R.id.Appsignuppass1);
        pass2 = findViewById(R.id.Appsignuppass2);
        Button allogin = findViewById(R.id.allogin);
        Button signUp = findViewById(R.id.Appsignupsignupbtn);
        ImageButton googleSignIn = findViewById(R.id.appsignupGSignin);
        LoginButton facebookSignIn = findViewById(R.id.appsignupFSignin);

        allogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*
         * Action when Clicked on btn for sign up with email and pass.
         * */
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    final String nam = name.getText().toString().trim();
                    final String emai = email.getText().toString().trim();
                    final String pas = pass.getText().toString().trim();

                    final ProgressDialog dialog = ProgressDialog.show(SignUp.this, "",
                            "Please wait...", true);
                    dialog.show();

                    mAuth.createUserWithEmailAndPassword(emai, pas)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("EmailPass", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(nam).build();

                                        assert user != null;
                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.e("usd", "User profile updated.");
                                                            dialog.dismiss();
                                                            startActivity(new Intent(SignUp.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                });

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("EmailPass", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(SignUp.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
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

                mGoogleSignInClient = GoogleSignIn.getClient(SignUp.this, gso);
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
        if (name.getText().toString().trim().length() == 0) {
            name.startAnimation(shakeAnimation);
            name.setError("Enter name");
            return false;
        } else if (email.getText().toString().trim().length() == 0) {
            email.startAnimation(shakeAnimation);
            email.setError("Enter valid Email");
            return false;
        } else if (pass.getText().toString().trim().length() == 0) {
            pass.startAnimation(shakeAnimation);
            pass.setError("Enter valid password");
            return false;
        } else if (pass2.getText().toString().trim().length() == 0) {
            pass2.startAnimation(shakeAnimation);
            pass2.setError("Enter password");
            return false;
        } else if (!pass.getText().toString().equals(pass2.getText().toString())) {
            pass.startAnimation(shakeAnimation);
            pass.setError("Password doesn't match");
            pass2.startAnimation(shakeAnimation);
            pass2.setError("Password doesn't match");
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
            dialog = ProgressDialog.show(SignUp.this, "",
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
            Toast.makeText(SignUp.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            //dialog.cancel();
            Log.e("GSignFailed", "signInResult:failed code=" + e.getStatusCode());
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
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("GSignin2", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.e("fbsignhft", "handleFacebookAccessToken:" + token);
        final ProgressDialog dialog = ProgressDialog.show(SignUp.this, "",
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
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            LoginManager.getInstance().logOut();
                            dialog.cancel();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("fbsignhft3", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            LoginManager.getInstance().logOut();
                            dialog.cancel();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignUp.this, UserLogIn.class));
    }
}