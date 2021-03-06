package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SymbolTable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.GroupAdapter;
import com.example.myapplication.Model.AddGroup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;

public class Add_Reciever extends AppCompatActivity {

public String uid;
public String Group_Name,Email,Pass;
RecyclerView recyclerView;
ProgressDialog progressDialog;
ArrayList<String> Group_List;
GroupAdapter groupAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__reciever);
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");
        Email=intent.getStringExtra("email");
        Pass=intent.getStringExtra("pass");
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        Group_List=new ArrayList<>();
        progressDialog =new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialogue);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        retrieveData();
    }
    public void new_group_btn(View view) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_box, null);
        final EditText GroupName = (EditText) dialogView.findViewById(R.id.edit_txt);
        Button Cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button Save = (Button) dialogView.findViewById(R.id.btn_save);
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(GroupName.getText()))
                {
                    Toast.makeText(Add_Reciever.this,"Please enter the name of group",Toast.LENGTH_SHORT).show();
                }
                else
                {
                   Group_Name=GroupName.getText().toString();
                   MakeGroup();
                }
                dialogBuilder.dismiss();

            }
        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
    }
    public void MakeGroup()
    {
        DatabaseReference firebaseDatabase= FirebaseDatabase.getInstance().getReference("Groups").child(uid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("group_name",Group_Name);
        firebaseDatabase.push().setValue(hashMap);
        FirebaseDatabase firebaseDatabase1=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase1.getReference().child("Groups").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Group_List.clear();
                    HashMap<String,Object> hashMap=(HashMap<String, Object>)dataSnapshot.getValue();
                    for(String key:hashMap.keySet()) {
                        Object data = hashMap.get(key);
                        HashMap<String, Object> userData = (HashMap<String, Object>) data;
                        String group_name=(String)userData.get("group_name");
                        Group_List.add(group_name);
                    }
                    groupAdapter.notifyItemRangeChanged(0,Group_List.size());
                    groupAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void retrieveData()
    {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference().child("Groups").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    HashMap<String,Object> hashMap=(HashMap<String, Object>)dataSnapshot.getValue();
                    for(String key:hashMap.keySet()) {
                        Object data = hashMap.get(key);
                        HashMap<String, Object> userData = (HashMap<String, Object>) data;
                        String group_name=(String)userData.get("group_name");
                        Group_List.add(group_name);
                        initRecyclerView();
                    }
                    TextView textView=findViewById(R.id.text_view);
                    textView.setVisibility(View.GONE);
                }
                else
                {
                    TextView textView=findViewById(R.id.text_view);
                    textView.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void initRecyclerView()
    {
        groupAdapter =new GroupAdapter(Add_Reciever.this,Group_List);
        recyclerView.setAdapter(groupAdapter);
        progressDialog.dismiss();
        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String group=Group_List.get(position);
                Intent intent=new Intent(Add_Reciever.this,FormGroup.class);
                intent.putExtra("uid",uid);
                intent.putExtra("group_name",group);
                startActivity(intent);
            }

            @Override
            public void removeItem(final int position) {
                final int pos=position;
                final String gname=Group_List.get(position);
                final AlertDialog dialogBuilder = new AlertDialog.Builder(Add_Reciever.this).create();
                LayoutInflater inflater = Add_Reciever.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.delete_group, null);
                Button No = (Button) dialogView.findViewById(R.id.btn_cancel);
                Button Yes = (Button) dialogView.findViewById(R.id.btn_save);
                dialogBuilder.setView(dialogView);
                dialogBuilder.show();
                Yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogBuilder.dismiss();
                        Group_List.remove(pos);
                        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                        final DatabaseReference databaseReference=firebaseDatabase.getReference().child("Groups").child(uid);
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    HashMap<String,Object> hashMap=(HashMap<String, Object>)dataSnapshot.getValue();
                                    for(String key:hashMap.keySet()) {
                                        Object data = hashMap.get(key);
                                        HashMap<String, Object> userData = (HashMap<String, Object>) data;
                                        String group_name=(String)userData.get("group_name");
                                        if(group_name.equals(gname))
                                        {
                                            databaseReference.child(key).removeValue();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        groupAdapter.notifyItemRemoved(pos);
                    }
                });
                No.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogBuilder.dismiss();
                    }
                });
            }

        });
    }

    public void compose(View view) {
        Intent intent=new Intent(Add_Reciever.this,ComposeEmail.class);
        intent.putExtra("uid",uid);
        intent.putExtra("email",Email);
        intent.putExtra("pass",Pass);
        startActivity(intent);
    }
}