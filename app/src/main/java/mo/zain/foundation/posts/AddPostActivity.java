package mo.zain.foundation.posts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import mo.zain.foundation.R;
import mo.zain.foundation.ui.SplashScreen;

public class AddPostActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    EditText pDescriptionEt, pTitleEt;
    Button pUploadBtn;
    ImageView imageView;
    private Uri imageUri;

    String name, email, uid, dp,phone;

    ProgressDialog pd;


    private static final int IMAGE_REQUEST = 1;


    //info of post to be edit
    String editTitle,editDescription,editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        setTitle("");

        firebaseAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);
        pDescriptionEt = findViewById(R.id.pDescriptionEt);
        pTitleEt = findViewById(R.id.pTitleEt);
        pUploadBtn = findViewById(R.id.pUploadBtn);
        imageView = findViewById(R.id.pImageIv);


        checkUserStatus();

        //get data
        Intent intent=getIntent();
        String isUpdateKey=""+intent.getStringExtra("key");
        String edit=""+intent.getStringExtra("editPostId");

        if (isUpdateKey.equals("editPost"))
        {
            pUploadBtn.setText("Update");
            loadPostData(edit);

        }else {
            pUploadBtn.setText("Upload");
        }

        //get information in post
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    name = dataSnapshot.child("username").getValue().toString();
                    email = dataSnapshot.child("email").getValue().toString();
                    dp = dataSnapshot.child("imageURL").getValue().toString();
                    phone=dataSnapshot.child("phone").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });
        pUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data from xml
                String titleTx = pTitleEt.getText().toString().trim();
                String descriptionTx = pDescriptionEt.getText().toString().trim();
                if (titleTx.isEmpty()) {
                    pTitleEt.setError("Enter Post Title..");
                    return;
                }
                if (descriptionTx.isEmpty()) {
                    pDescriptionEt.setError("Enter Post Description");
                    return;
                }
                if (isUpdateKey.equals("editPost"))
                {
                    beginUpdate(titleTx,descriptionTx,edit);
                }else {
                    uploadData(titleTx, descriptionTx);

                }


            }
        });

    }

    private void beginUpdate(String titleTx, String descriptionTx, String edit) {
        pd.setMessage("Updating Post...");
        pd.show();

        if (!editImage.equals("noImage")){
            //with image
            updateWasWithImage(titleTx,descriptionTx,edit);
        }else if (imageView.getDrawable()!=null){
            updateWithNowImage(titleTx,descriptionTx,edit);
        }else {
            //without image
            updateWithoutImage(titleTx,descriptionTx,edit);
        }
    }

    private void updateWithoutImage(String titleTx, String descriptionTx, String edit) {

        HashMap<String, Object> hashMap=new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("UName", name);
        hashMap.put("UEmail", email);
        hashMap.put("uDP", dp);
        hashMap.put("pTitle", titleTx);
        hashMap.put("pDescr", descriptionTx);
        hashMap.put("pImage", "noImage");
        hashMap.put("UPhone",phone);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(edit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        FancyToast.makeText(AddPostActivity.this,"Updated...",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });
    }

    private void updateWasWithImage(String titleTx, String descriptionTx, String edit) {
        StorageReference mPicRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String timeStamp= String.valueOf(System.currentTimeMillis());
                String filePathAndName="Posts/" + "Post_" +timeStamp;
                Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                byte[] data=baos.toByteArray();

                StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //image uploaded get its url
                                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful())
                                {
                                   String downloadUri=uriTask.getResult().toString();
                                   if (uriTask.isSuccessful())
                                   {
                                      //uri is recived upload to fire base
                                      HashMap<String, Object> hashMap=new HashMap<>();

                                       hashMap.put("uid", uid);
                                       hashMap.put("UName", name);
                                       hashMap.put("UEmail", email);
                                       hashMap.put("uDP", dp);
                                       hashMap.put("pTitle", titleTx);
                                       hashMap.put("pDescr", descriptionTx);
                                       hashMap.put("pImage", downloadUri);
                                       hashMap.put("UPhone",phone);
                                       DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                       ref.child(edit)
                                               .updateChildren(hashMap)
                                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {
                                                       pd.dismiss();
                                                       FancyToast.makeText(AddPostActivity.this,"Updated...",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                                                   }
                                               }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               pd.dismiss();
                                               FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                           }
                                       });
                                   }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //image not updated
                        pd.dismiss();
                        FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });
    }

    private void updateWithNowImage(String titleTx, String descriptionTx, String edit) {
        String timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/" + "Post_" +timeStamp;
        Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();

        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful())
                        {
                            String downloadUri=uriTask.getResult().toString().trim();
                            if (uriTask.isSuccessful())
                            {
                                //uri is recived upload to fire base
                                HashMap<String, Object> hashMap=new HashMap<>();

                                hashMap.put("uid", uid);
                                hashMap.put("UName", name);
                                hashMap.put("UEmail", email);
                                hashMap.put("uDP", dp);
                                hashMap.put("pTitle", titleTx);
                                hashMap.put("pDescr", descriptionTx);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("UPhone",phone);

                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(edit)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                FancyToast.makeText(AddPostActivity.this,"Updated...",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //image not updated
                pd.dismiss();
                FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });


    }
    private void loadPostData(String edit) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query fquery=reference.orderByChild("pId").equalTo(edit);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    editTitle=""+ds.child("pTitle").getValue();
                    editDescription=""+ds.child("pDescr").getValue();
                    editImage=""+ds.child("pImage").getValue();

                    pTitleEt.setText(editTitle);
                    pDescriptionEt.setText(editDescription);
                    if (!editImage.equals("noImage"))
                    {
                        try {
                            Picasso.get().load(editImage).into(imageView);
                        }catch (Exception ex)
                        {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String titleTx, String descriptionTx) {
        pd.setMessage("Publishing Post...");
        pd.show();
        //for post image post id post publish time
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "Post_" + timestamp;
        if (imageView.getDrawable()!=null) {
            Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data=baos.toByteArray();


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("UName", name);
                                hashMap.put("UEmail", email);
                                hashMap.put("uDP", dp);
                                hashMap.put("pId", timestamp);
                                hashMap.put("pTitle", titleTx);
                                hashMap.put("pDescr", descriptionTx);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timestamp);
                                hashMap.put("UPhone",phone);
                                //Path to store post data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                FancyToast.makeText(AddPostActivity.this,"Post Published..",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                                                pTitleEt.setText("");
                                                pDescriptionEt.setText("");
                                                imageView.setImageURI(null);
                                                imageUri = null;

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed adding post in database
                                        pd.dismiss();
                                        FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            pd.dismiss();
                            FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                        }
                    });
        } else {

            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("UName", name);
            hashMap.put("UEmail", email);
            hashMap.put("uDP", dp);
            hashMap.put("pId", timestamp);
            hashMap.put("pTitle", titleTx);
            hashMap.put("pDescr", descriptionTx);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timestamp);
            hashMap.put("UPhone",phone);
            //Path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published..", Toast.LENGTH_SHORT).show();
                            pTitleEt.setText("");
                            pDescriptionEt.setText("");
                            imageView.setImageURI(null);
                            imageUri = null;
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post in database
                    pd.dismiss();
                    FancyToast.makeText(AddPostActivity.this,""+e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();

                }
            });
        }
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        imageUri = data.getData();
        imageView.setImageURI(imageUri);
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void checkUserStatus() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            email = firebaseUser.getEmail();
            uid = firebaseUser.getUid();
            //name=firebaseUser

        } else {
            startActivity(new Intent(this, SplashScreen.class));
            finish();
        }
    }

}