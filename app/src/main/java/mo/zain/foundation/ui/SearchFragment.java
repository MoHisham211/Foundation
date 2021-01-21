package mo.zain.foundation.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import mo.zain.foundation.R;
import mo.zain.foundation.adapters.AdapterPost;
import mo.zain.foundation.models.ModelPost;
import mo.zain.foundation.posts.AddPostActivity;

public class SearchFragment extends Fragment {


    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    ProgressBar progressBar;

    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_search, container, false);


        progressBar=view.findViewById(R.id.pb);
        firebaseAuth= FirebaseAuth.getInstance();
        searchView=view.findViewById(R.id.searchView);

        recyclerView=view.findViewById(R.id.postsRecycleView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);





        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                if (!TextUtils.isEmpty(text))
                {
                    searchPosts(text);
                }else
                {
                    loadPosts();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String text) {
                if (!TextUtils.isEmpty(text))
                {
                    searchPosts(text);
                }else
                {
                    loadPosts();
                }
                return false;
            }
        });


        postList=new ArrayList<>();
        loadPosts();

        return view;







    }

    private void loadPosts() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    adapterPost=new AdapterPost(postList,getActivity());

                    recyclerView.setAdapter(adapterPost);

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FancyToast.makeText(getContext(),""+error.getMessage(), FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //private void search

    private void searchPosts(String searchKey)
    {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost modelPost=ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle().toLowerCase().contains(searchKey.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchKey.toLowerCase())){
                        postList.add(modelPost);
                    }
                    //postList.add(modelPost);

                    adapterPost=new AdapterPost(postList,getActivity());

                    recyclerView.setAdapter(adapterPost);

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FancyToast.makeText(getContext(),""+error.getMessage(), FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}