package org.messanger.snsproject.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.messanger.snsproject.Adapter.MainPostingRecyclerViewAdapter;
import org.messanger.snsproject.Data.MemberData;
import org.messanger.snsproject.Data.ProcessedPostData;
import org.messanger.snsproject.Listener.OnCommentListener;
import org.messanger.snsproject.Listener.OnPostListener;
import org.messanger.snsproject.R;
import org.messanger.snsproject.structure.Pair;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int INFO_REQCODE=101;
    //private Button logout;
    private Button openMenuButton;
    private TextView userName; //나의 이름
    private FloatingActionButton addBoard;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private RecyclerView recyclerView;
    private RelativeLayout loader;
    private DrawerLayout drawerLayout;
    private ImageView myProfileImage;
    private TextView myProfileName;
    private View menuView;
    private ListView menuList;
    static final String[] LIST_VAL={"마이페이지","로그아웃"};
    private String uid=null; //나의 uid
    private String profileImageURL=null; //프로필 이미지 URL

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference storage=FirebaseStorage.getInstance().getReference();

    private OnPostListener onPostListener;
    private OnCommentListener onCommentListener;

    static class SortByDate implements Comparator<ProcessedPostData> { //날짜별로 최신순으로 정렬하기 위함
        @Override
        public int compare(ProcessedPostData a, ProcessedPostData b) {
            return b.getwDate().compareTo(a.getwDate());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //가로모드 금지

        setID();
        setEvents();
        setStateListener(); //로그인 상태 이벤트

        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_expandable_list_item_1,LIST_VAL);
        menuList.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        loader.setVisibility(View.VISIBLE);

        //프로필 이미지, 이름 설정
        //Toast.makeText(MainActivity.this,profileImageURL,Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this,userName.getText().toString(),Toast.LENGTH_SHORT).show();
        Glide.with(MainActivity.this).load(profileImageURL).centerCrop().override(500).into(myProfileImage);
        myProfileName.setText(userName.getText().toString());
    }
    public void setID(){
        myProfileImage=findViewById(R.id.myProfileImage);
        myProfileName=findViewById(R.id.myProfileName);
        openMenuButton=findViewById(R.id.openMenuButton);
        addBoard=findViewById(R.id.floatingActionButton);
        userName=findViewById(R.id.userNameText);
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView=findViewById(R.id.boardList);
        loader=findViewById(R.id.viewLoader);
        drawerLayout=findViewById(R.id.drawerLayout);
        menuView=findViewById(R.id.drawer);
        menuList=findViewById(R.id.listView);
    }
    public void setEvents(){
        openMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(menuView);
            }
        });
        addBoard.setOnClickListener(new View.OnClickListener() { //게시글 작성 페이지로 이동
            @Override
            public void onClick(View v) {
                gotoActivity(WritePostActivity.class);
            }
        });
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(myProfileName.getText().toString().equals("Name")) {
                    Glide.with(MainActivity.this).load(profileImageURL).centerCrop().override(500).into(myProfileImage);
                    myProfileName.setText(userName.getText().toString());
                }

                switch(position){
                    case 0:{
                        Intent intent=new Intent(MainActivity.this,MyPageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("userID",uid);
                        startActivity(intent);
                        //마이페이지 이동
                        break;
                    }
                    case 1:{
                        //로그아웃
                        if(menuView.isShown()){
                            drawerLayout.closeDrawer(menuView);
                        }
                        firebaseAuth.signOut();
                    }
                }
            }
        });
        onPostListener=new OnPostListener() {
            @Override
            public void onDelete(final String postId,final ArrayList<String> mediaFiles) {
                db.collection("posts").document(postId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                for(String file:mediaFiles){
                                    storage.child("posts/"+postId+"/"+file)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //
                                                }
                                            });
                                }
                                Toast.makeText(MainActivity.this,"게시물이 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                getPosts();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"게시물이 삭제되지 않았습니다.",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        };
        onCommentListener=new OnCommentListener() {
            @Override
            public void onSave(String postId,String uid,String comment,ArrayList<Pair> commentList,final int pos) {
                commentList.add(new Pair(uid,comment));
                DocumentReference reference = db.collection("posts").document(postId);
                reference.update("comments",commentList)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,"댓글을 작성하였습니다.",Toast.LENGTH_SHORT).show();
                                getPosts();
                                new Handler().postDelayed(new Runnable() { //봤던 리스트로 스크롤 이동
                                    @Override
                                    public void run() {
                                        recyclerView.scrollToPosition(pos);
                                    }
                                },2000);
                            }
                        });
            }

            @Override
            public void onDelete(String postId, int idx, ArrayList<Pair> commentList,final int pos) {
                commentList.remove(idx);
                DocumentReference reference = db.collection("posts").document(postId);
                reference.update("comments",commentList)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {//post 업데이트 Success이벤트
                                Toast.makeText(MainActivity.this,"댓글을 삭제하였습니다.",Toast.LENGTH_SHORT).show();
                                getPosts();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.scrollToPosition(pos);
                                    }
                                },1000);
                            }
                        });
            }
        };
    }
    public void getPosts(){ //게시물 데이터 받아오기
        if(user!=null) {
            CollectionReference collectionReference = db.collection("posts");
            collectionReference.orderBy("postDate", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                    final ArrayList<ProcessedPostData> postBox=new ArrayList<>();
                    for (final QueryDocumentSnapshot snapshot:queryDocumentSnapshots) { //post들
                        final MemberData memberData=new MemberData();

                        DocumentReference documentReference=db.collection("users").document(snapshot.getData().get("uid").toString());
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot document) {
                                String name = document.getData().get("myname").toString(); //이름정보
                                String prof_path = document.getData().get("prof_path").toString();
                                memberData.setMyname(name);
                                memberData.setProf_path(prof_path);

                                ArrayList<Pair> pairs=new ArrayList<>();
                               for(Object key:((ArrayList)snapshot.getData().get("comments"))){ //댓글의 갯수만큼
                                   Map<String,String> map = ((HashMap)key);
                                   ArrayList<String> temp=new ArrayList<>();
                                   for(Object info : map.keySet()) { //2번 반복
                                       temp.add(info.toString());
                                   }
                                   pairs.add(new Pair(map.get(temp.get(0)),map.get(temp.get(1))));
                               }

                               postBox.add(new ProcessedPostData(memberData.getMyname(),
                                        snapshot.getData().get("title").toString(),
                                        snapshot.getData().get("contents").toString(),
                                        (ArrayList<String>)snapshot.getData().get("mediaValues"),
                                        memberData.getProf_path(),
                                        snapshot.getData().get("uid").toString(),
                                        snapshot.getId(),
                                        snapshot.getData().get("postDate").toString(),
                                        (ArrayList<String>) snapshot.getData().get("mediaFormats"),
                                        pairs));


                                if(postBox.size()==queryDocumentSnapshots.size()){
                                    //시간에 따라 재정렬
                                    Collections.sort(postBox,new SortByDate());

                                    MainPostingRecyclerViewAdapter adapter=new MainPostingRecyclerViewAdapter(MainActivity.this,postBox,user.getUid(),onPostListener,onCommentListener);
                                    recyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            });
        }
        loader.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPosts(); ///갱신
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null) //리스너를 해체합니다.
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        if(menuView.isShown()){
            drawerLayout.closeDrawer(menuView);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==INFO_REQCODE){
            if(resultCode==InitInfoActivity.FIN_CODE){
                firebaseAuth.addAuthStateListener(authStateListener);
            }
        }
    }
    public void gotoActivityForResult(Class c,int req){
        Intent intent = new Intent(MainActivity.this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent,req);
    }
    public void gotoActivity(Class c){
        Intent intent = new Intent(MainActivity.this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    public void setStateListener(){
        authStateListener=new FirebaseAuth.AuthStateListener() { //로그인 상태변화에 따른 리스너 설정
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                if( firebaseUser==null){
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                else{ //로그인 되어있는 상태
                    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

                    uid=firebaseUser.getUid();
                    DocumentReference docRef = firebaseFirestore.collection("users").document(uid); //문서가 uid인것을 찾음
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult(); //읽어들인 데이터
                                if(document!=null) {
                                    if (document.exists()) {
                                        // 회원정보가 존재한 상태
                                        userName.setText(document.getData().get("myname").toString());
                                        profileImageURL=document.getData().get("prof_path").toString();
                                        myProfileName.setText(userName.getText().toString());
                                        if(!(MainActivity.this).isDestroyed()) {
                                            Glide.with(MainActivity.this).load(profileImageURL).centerCrop().override(500).into(myProfileImage);
                                        }
                                    }
                                    else {
                                        //문서를 찾을수 없다면,(회원정보를 찾을 수 없다면,)
                                        gotoActivityForResult(InitInfoActivity.class,INFO_REQCODE);
                                    }
                                }
                            }
                            else {
                                //Do something_ 데이터 읽어들이기 실패시,
                            }
                        }
                    });

                }
            }
        };
    }
}
