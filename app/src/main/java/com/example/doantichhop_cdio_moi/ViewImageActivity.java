package com.example.doantichhop_cdio_moi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    private FirebaseDatabase db;
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar progressBar;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef,mRef;
    private List<Upload> mUploads;
    private ValueEventListener mDBlistener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        mRecyclerView=findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar=findViewById(R.id.progress_circular);

        mUploads=new ArrayList<>();

        mAdapter=new ImageAdapter(ViewImageActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(ViewImageActivity.this);

        mStorage = FirebaseStorage.getInstance();

//        mDatabaseRef=FirebaseDatabase.getInstance().getReference("uploads");
        db = FirebaseDatabase.getInstance();
        mDatabaseRef = db.getReference("uploads");
        mRef = db.getReference("uploads");
        mDBlistener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

 //               mUploads.clear();

                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Upload upload=postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewImageActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {

        Upload selectItem = mUploads.get(position);
        String uri_image = selectItem.getImgUrl();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", uri_image);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        Upload selectItem = mUploads.get(position);
        final String selectKey = selectItem.getKey();
        StorageReference imageRef =  mStorage.getReferenceFromUrl(selectItem.getImgUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectKey).removeValue();
                Toast.makeText(ViewImageActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ViewImageActivity.this, "Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mDatabaseRef.removeEventListener(mDBlistener);
//    }
}
