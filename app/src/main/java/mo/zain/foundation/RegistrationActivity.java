package mo.zain.foundation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;

import mo.zain.foundation.ui.MainActivity;

public class RegistrationActivity extends AppCompatActivity {
    TextInputEditText TIETUserName,TIETEmail,TIETPassword,TIETPhone;
    String txtPassword,txtEmail,txtUserName,txtPhone;
    TextView toLoginActivity;
    Button btnSign;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initialize();
        //**************************************go back to Login activity****************************
        toLoginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
            }
        });
        //*************************************sign up button****************************************
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                txtUserName=TIETUserName.getText().toString();
                txtEmail=TIETEmail.getText().toString();
                txtPassword=TIETPassword.getText().toString();
                txtPhone=TIETPhone.getText().toString();
                //***********************
                firebaseAuth=FirebaseAuth.getInstance();
                if (txtUserName.equals(""))
                {
                    progressDialog.dismiss();
                    TIETUserName.setError("User Name is Required.");
                    return;
                }else if (txtEmail.equals(""))
                {
                    progressDialog.dismiss();
                    TIETEmail.setError("Email is Required.");
                    return;
                }else if (txtPhone.equals(""))
                {
                    progressDialog.dismiss();
                    TIETPhone.setError("Phone is Required.");
                    return;
                }else if(txtPassword.equals(""))
                {
                    progressDialog.dismiss();
                    TIETPassword.setError("Password is Required.");
                    return;
                }else if (txtPassword.length()<=6)
                {
                    progressDialog.dismiss();
                    TIETPassword.setError("Password Must be > 6 characters");
                    return;
                }else
                {
                    firebaseAuth.createUserWithEmailAndPassword(txtEmail,txtPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        FancyToast.makeText(RegistrationActivity.this,"Sign Up Failed !!",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                    }else
                                    {
                                        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                                        DatabaseReference reference;
                                        String userid=firebaseUser.getUid();
                                        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
                                        HashMap<String,String> hashMap=new HashMap<>();
                                        hashMap.put("email",txtEmail);
                                        hashMap.put("id",userid);
                                        hashMap.put("username",txtUserName);
                                        hashMap.put("phone",txtPhone);
                                        hashMap.put("imageURL","");
                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                FancyToast.makeText(RegistrationActivity.this,"Failed To store data !!",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }
    private void initialize()
    {
        //***************************************ProgressDialog**************************************
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Sign up");
        progressDialog.setMessage("Please wait we'll create anew account");
        progressDialog.setCanceledOnTouchOutside(false);
        //****************************************UI Component of Edit*******************************
        TIETUserName=findViewById(R.id.Username);
        TIETEmail=findViewById(R.id.email_login);
        TIETPassword=findViewById(R.id.password_login);
        TIETPhone=findViewById(R.id.phone_login);
        //****************************************Action Component***********************************
        toLoginActivity=findViewById(R.id.toLogin);
        btnSign=findViewById(R.id.btnSignUp);
    }
}