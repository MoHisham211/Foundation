package mo.zain.foundation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;

import mo.zain.foundation.ui.MainActivity;
import mo.zain.foundation.ui.ProfileFragment;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    TextInputEditText TIFEmail,TIFPassword;
    String txtEmail,txtPassword;
    Button btnLogin;
    TextView toRegistration,Recovery;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    SignInButton MGooglebtn;

    LinearLayout linearLayout;

    Animation opps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);


        firebaseAuth=FirebaseAuth.getInstance();

        Recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        toRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtEmail=TIFEmail.getText().toString();
                txtPassword=TIFPassword.getText().toString();
                progressDialog.show();
                if (txtEmail.equals(""))
                {
                    progressDialog.dismiss();
                    TIFEmail.setError("Email is Required.");
                    return;
                }else if (txtPassword.equals(""))
                {
                    progressDialog.dismiss();
                    TIFPassword.setError("Password is Required.");
                    return;
                }else {

                    firebaseAuth.signInWithEmailAndPassword(txtEmail,txtPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                  if (task.isSuccessful())
                                  {
                                      Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                                      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                      startActivity(intent);
                                      progressDialog.dismiss();
                                      finish();
                                  }else
                                  {
                                      progressDialog.dismiss();
                                      FancyToast.makeText(LoginActivity.this,"Sign Up Failed !!",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                  }
                                }
                            });
                }
            }
        });
        MGooglebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        linearLayout=findViewById(R.id.linearLayout2);
        opps= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.text2);
        linearLayout.setAnimation(opps);

    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recovery Password");
        //Set layout linear layout
        LinearLayout linearLayout=new LinearLayout(this);
        //Views to set in dialog
        final EditText emailEt=new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(10);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        //Button
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email=emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        //Show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending Email...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FancyToast.makeText(LoginActivity.this,"Email Sent",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                            progressDialog.dismiss();
                        }else {
                            FancyToast.makeText(LoginActivity.this,"Failed...",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                FancyToast.makeText(LoginActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });
    }

    private void initialize()
    {
        TIFEmail=findViewById(R.id.email_login);
        TIFPassword=findViewById(R.id.password_login);
        toRegistration=findViewById(R.id.toReg);
        btnLogin=findViewById(R.id.login_btn);
        Recovery=findViewById(R.id.toreset);
        //******************************
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Logging in");
        progressDialog.setMessage("Please wait while we check your credentials");
        progressDialog.setCanceledOnTouchOutside(false);
        //*******************************
        MGooglebtn=findViewById(R.id.googleLoginBtn);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                DatabaseReference reference;
                                assert user != null;
                                String email=user.getEmail();
                                String userid=user.getUid();
                                reference= FirebaseDatabase.getInstance().getReference("Users");//.child(userid)
                                HashMap<String,String> hashMap=new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("id",userid);
                                hashMap.put("username","");
                                hashMap.put("phone","");
                                hashMap.put("imageURL","");
                                FirebaseDatabase database=FirebaseDatabase.getInstance();
                                reference.child(userid).setValue(hashMap);
                            }
                            Intent intent=new Intent(getApplicationContext(), MainActivity.class);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            progressDialog.dismiss();
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            FancyToast.makeText(LoginActivity.this,"Failed...",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FancyToast.makeText(LoginActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}