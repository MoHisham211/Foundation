package mo.zain.foundation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import mo.zain.foundation.adapters.AdapterPost;
import mo.zain.foundation.models.ModelPost;
import mo.zain.foundation.ui.SplashScreen;

public class UserProfile extends AppCompatActivity {

    FirebaseAuth firebaseAuth;


    RecyclerView postsRecycleView;

    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;

    TextView UserName,Email,Phone;
    ImageView avatarIV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        postsRecycleView=findViewById(R.id.recycleView_posts);

        firebaseAuth=FirebaseAuth.getInstance();



        UserName=findViewById(R.id.name);
        Email=findViewById(R.id.email);
        avatarIV=findViewById(R.id.app_bar_image);
        Phone=findViewById(R.id.phone);

        //get uid of clicked user to retrieve this posts
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");

        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    String name=ds.child("username").getValue().toString();
                    String email=ds.child("email").getValue().toString();
                    String image=ds.child("imageURL").getValue().toString();
                    String phone=ds.child("phone").getValue().toString();


                    UserName.setText(name);
                    Email.setText(email);
                    Phone.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatarIV);

                    }catch (Exception e)
                    {
                        //Picasso.get().load(R.mipmap.ic_launcher).into(avatarIV);
                        //avatarIV.setImageResource(R.drawable.icon);
                        //Glide.with(getContext()).load(user.getImageURL()).into(circularImageView);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postList=new ArrayList<>();



        checkUserStatus();
        loadHisPosts();

    }

    private void loadHisPosts() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        //show newest posts first,for this load from last;
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set to recycle
        postsRecycleView.setLayoutManager(linearLayoutManager);

        //postsList
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        //query to load
        Query query=ref.orderByChild("uid").equalTo(uid);

        //get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost MyPosts=ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(MyPosts);

                    //adapter
                    adapterPost=new AdapterPost(postList,getApplicationContext());

                    //set this adapter to recycle
                    postsRecycleView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FancyToast.makeText(getApplicationContext(),""+error.getMessage(),FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show();
            }
        });

    }

    private void checkUserStatus()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null)
        {

        }else {
            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
            finish();
        }
    }
}