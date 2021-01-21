package mo.zain.foundation.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mo.zain.foundation.R;
import mo.zain.foundation.adapters.AdapterPost;
import mo.zain.foundation.models.ModelPost;

import static android.app.Activity.RESULT_OK;


public class   ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    TextView UserName,Email,Phone;
    FloatingActionButton edit;
    ProgressDialog pd;
    ImageView avatarIV;
    private static final int IMAGE_REQUEST=1;

    private Uri imageUri;
    private StorageTask uploadTask;

    RecyclerView postsRecycleView;

    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        UserName=view.findViewById(R.id.name);
        Email=view.findViewById(R.id.email);
        Phone=view.findViewById(R.id.phone);
        edit=view.findViewById(R.id.editing);
        pd=new ProgressDialog(getActivity());
        avatarIV=view.findViewById(R.id.app_bar_image);
        postsRecycleView=view.findViewById(R.id.recycleView_posts);


        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();

        databaseReference=firebaseDatabase.getReference("Users");
//        error
        storageReference= FirebaseStorage.getInstance().getReference();






        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    String name=ds.child("username").getValue().toString();
                    String email=ds.child("email").getValue().toString();
                    String image=ds.child("imageURL").getValue().toString();
                    String phoneNum=ds.child("phone").getValue().toString();

                    UserName.setText(name);
                    Email.setText(email);
                    Phone.setText(phoneNum);

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

        //*****************Editing******************
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditeProfileDialog();
            }
        });

        //------------------------
        postList=new ArrayList<>();
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        //show newest posts first,for this load from last;
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set to recycle
        postsRecycleView.setLayoutManager(linearLayoutManager);

        //postsList
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

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
                    adapterPost=new AdapterPost(postList,getContext());

                    //set this adapter to recycle
                    postsRecycleView.setAdapter(adapterPost);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FancyToast.makeText(getContext(),""+error.getMessage(), FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });

    }

    //****************************************
    private void showEditeProfileDialog() {
        String options[]={"Edit Profile Picture","Edit Name","Edit Phone","LogOut"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose any operation");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //
                if (i==0)
                {
                    pd.setMessage("Update Profile Picture");
                    //ProfileOrCoverPhoto="imageURL";
                    openImage();

                }else if(i==1)
                {
                    pd.setMessage("Update User Name");
                    showNamePhoneUpdateDialog("username");
                }else if(i==2)
                {
                    pd.setMessage("Update Phone Number");
                    showNamePhoneUpdateDialog("phone");
                }else if (i==3)
                {
                    firebaseAuth.signOut();
                    FancyToast.makeText(getContext(),"Goodbye !!",FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
                    startActivity(new Intent(getContext(),SplashScreen.class));

                }
            }
        });
        builder.create().show();
    }

    //*************************************
    private void checkUserStatus()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null)
        {
            uid=user.getUid();
        }else {
            startActivity(new Intent(getContext(),SplashScreen.class));
            getActivity().finish();
        }
    }
    //*************************************
    private void showNamePhoneUpdateDialog(final String Key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+Key);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+Key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value=editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result=new HashMap<>();
                    result.put(Key,value);
                    //user.getUid()
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    FancyToast.makeText(getContext(),"Updated...",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            FancyToast.makeText(getContext(),""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                        }
                    });
                }else
                {
                    FancyToast.makeText(getContext(),"Enter "+Key,FancyToast.LENGTH_LONG,FancyToast.INFO,false).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }
    //***************************
    private void openImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    //*****************************
    private void uploadImage()
    {
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setTitle("Uploading");
        pd.setMessage("Wait for uploading the profile image");
        pd.show();
        if (imageUri!=null)
        {
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+
                    "."+getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Uri dwonlloadUri= (Uri)task.getResult();
                        String mUri=dwonlloadUri.toString();
                        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageURL",mUri);
                        databaseReference.updateChildren(map);
                        pd.dismiss();
                    }else {
                        FancyToast.makeText(getContext(),"Failed!",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FancyToast.makeText(getContext(),""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                    pd.dismiss();
                }
            });
        }else
        {
            FancyToast.makeText(getContext(),"No Image Selected !",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
        }
    }
    //**************************
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST&& resultCode== RESULT_OK
                && data!=null && data.getData()!=null)
        {
            imageUri=data.getData();
            if (uploadTask!=null&& uploadTask.isInProgress())
            {
                FancyToast.makeText(getContext(),"Upload in progress",FancyToast.LENGTH_LONG,FancyToast.INFO,false).show();
            }else {
                uploadImage();
            }
        }
    }
}