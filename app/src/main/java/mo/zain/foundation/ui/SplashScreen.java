package mo.zain.foundation.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import mo.zain.foundation.LoginActivity;
import mo.zain.foundation.R;
import mo.zain.foundation.RegistrationActivity;

public class SplashScreen extends AppCompatActivity {

    CircularImageView circularImageView;
    TextView textView,textView2,textView3,log;
    Animation photo,welcome,hellomsg,btnA,v,vv,or,logn;
    Button btn;
    View v1,v2;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        circularImageView=findViewById(R.id.circularImageView);
        photo= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.text1);
        circularImageView.setAnimation(photo);

        textView=findViewById(R.id.textView);
        welcome=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.text2);
        welcome.setStartOffset(2550);
        textView.setAnimation(welcome);

        textView2=findViewById(R.id.textView2);
        hellomsg=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.text3);
        hellomsg.setStartOffset(3550);
        textView2.setAnimation(hellomsg);

        btn=findViewById(R.id.button);
        btnA=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.btn);
        btnA.setStartOffset(4550);
        btn.setAnimation(btnA);

        v1=findViewById(R.id.v);
        v=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.v1);
        v.setStartOffset(4550);
        v1.setAnimation(v);

        textView3=findViewById(R.id.textView3);
        or=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.v1);
        or.setStartOffset(4550);
        textView3.setAnimation(or);

        v2=findViewById(R.id.vv);
        vv=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.v1);
        vv.setStartOffset(4550);
        v2.setAnimation(vv);

        log=findViewById(R.id.textView4);
        logn=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.v1);
        logn.setStartOffset(4550);
        log.setAnimation(logn);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseUser!=null)
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}