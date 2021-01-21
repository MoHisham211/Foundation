package mo.zain.foundation.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Toast;

import com.fxn.BubbleTabBar;

import mo.zain.foundation.ExitDialog;
import mo.zain.foundation.R;

public class MainActivity extends AppCompatActivity {

    BubbleTabBar bubbleTabBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bubbleTabBar=findViewById(R.id.bubbleTabBar);
        FragmentTransaction fragment=getSupportFragmentManager().beginTransaction();
        fragment.replace(R.id.contaner,new HomeFragment()).commit();
        bubbleTabBar.addBubbleListener(i -> {
            switch (i)
            {
                case R.id.home:
                    FragmentTransaction fragmentTransactionHome=getSupportFragmentManager().beginTransaction();
                    fragmentTransactionHome.replace(R.id.contaner,new HomeFragment()).commit();
                    return;
                case R.id.search:
                    FragmentTransaction fragmentTransactionSearch=getSupportFragmentManager().beginTransaction();
                    fragmentTransactionSearch.replace(R.id.contaner,new SearchFragment()).commit();
                    return;
                case R.id.person:
                    FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.contaner,new ProfileFragment()).commit();
                    return;

            }
        });

    }

    @Override
    public void onBackPressed() {
        ExitDialog exitDialog=new ExitDialog();
        exitDialog.show(getSupportFragmentManager(),"");
        exitDialog.setCancelable(false);
    }
}