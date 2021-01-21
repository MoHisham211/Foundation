package mo.zain.foundation.adapters;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mo.zain.foundation.R;
import mo.zain.foundation.UserProfile;
import mo.zain.foundation.models.ModelPost;
import mo.zain.foundation.posts.AddPostActivity;
import mo.zain.foundation.ui.HomeFragment;
import mo.zain.foundation.ui.ProfileFragment;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.ViewHolder> {
    List<ModelPost> ModelPosts;
    Context context;

    String myUid;


    public AdapterPost(List<ModelPost> modelPosts, Context context) {
        ModelPosts = modelPosts;
        this.context = context;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_style, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String Uid = ModelPosts.get(position).getUid();
        String UEmail = ModelPosts.get(position).getUEmail();
        String UName = ModelPosts.get(position).getUName();
        String UPhone = ModelPosts.get(position).getUPhone();
        String uDP = ModelPosts.get(position).getuDP();
        String pId = ModelPosts.get(position).getpId();
        String pTitle = ModelPosts.get(position).getpTitle();
        String pDescr = ModelPosts.get(position).getpDescr();
        String pImage = ModelPosts.get(position).getpImage();
        String pTimeStamp = ModelPosts.get(position).getpTime();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime = DateFormat.format("dd/MM/yyyy 'at' hh:mm aa", calendar).toString();

        //set data
        holder.uNameTV.setText(UName);
        holder.PTimeTV.setText(pTime);
        holder.PTitleTv.setText(pTitle);
        holder.PDescriptionTv.setText(pDescr);

//user

        try {
            Picasso.get().load(uDP).placeholder(R.drawable.ic_person)
                    .into(holder.uPictureIV);
        } catch (Exception ex) {

        }
        if (pImage.equals("noImage")) {
            //hide imageView
            holder.PImageIV.setVisibility(View.GONE);
        } else {
            //post
            holder.PImageIV.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage)
                        .into(holder.PImageIV);
            } catch (Exception ex) {

            }
        }


        //more
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, Uid, myUid, pId, pImage);
            }
        });
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(UPhone);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String phone = ds.child("phone").getValue().toString();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


        holder.WhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(UPhone);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String phone = "+02" + ds.child("phone").getValue().toString();
                            String url = "https://api.whatsapp.com/send?phone=" + phone;
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });


        holder.ProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Call"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(context, UserProfile.class);
                                intent.putExtra("uid", Uid);
                                context.startActivity(intent);
                                return;
                            case 1:
                                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(UPhone);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String phone = ds.child("phone").getValue().toString();
                                            Intent intent = new Intent(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse("tel:" + phone));
                                            context.startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                return;
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case 0:
                        deletePost(pId, pImage);
                        return true;
                    case 1:
                        Intent intent = new Intent(context, AddPostActivity.class);
                        intent.putExtra("key", "editPost");
                        intent.putExtra("editPostId", pId);
                        context.startActivity(intent);
                        return true;
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void deletePost(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            deleteWithoutImage(pId);
        } else {
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting..");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //to delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                FancyToast.makeText(context, "Deleted Successfully !", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting..");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                FancyToast.makeText(context, "Deleted Successfully !", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return ModelPosts.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView uPictureIV, PImageIV;
        TextView uNameTV, PTimeTV, PTitleTv, PDescriptionTv, PLikeTv;
        ImageButton moreBtn;
        LinearLayout ProfileLayout;
        Button callButton, WhatsApp;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            uPictureIV = itemView.findViewById(R.id.uPictureIV);
            PImageIV = itemView.findViewById(R.id.PImageIV);
            uNameTV = itemView.findViewById(R.id.uNameTV);
            PTimeTV = itemView.findViewById(R.id.PTimeTV);
            PTitleTv = itemView.findViewById(R.id.PTitleTv);
            PDescriptionTv = itemView.findViewById(R.id.PDescriptionTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            ProfileLayout = itemView.findViewById(R.id.Profile);
            callButton = itemView.findViewById(R.id.call);
            WhatsApp = itemView.findViewById(R.id.whatsApp);
        }
    }
}
