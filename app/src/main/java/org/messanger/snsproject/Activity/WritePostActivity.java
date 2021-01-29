package org.messanger.snsproject.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.messanger.snsproject.Data.MemberData;
import org.messanger.snsproject.Data.PostData;
import org.messanger.snsproject.Data.ProcessedPostData;
import org.messanger.snsproject.R;
import org.messanger.snsproject.structure.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WritePostActivity extends AppCompatActivity {
    public static int IMAGE_LOAD_CODE=105;
    public static int VIDEO_LOAD_CODE=106;
    private LinearLayout parent;
    private Button identify,addImage,addVideo,deleteMedia;
    private EditText title,contents;
    private String mediaPath=null; //파이어스토리지에 저장된 이미지의 경로
    private Map<ImageView,String> pathList=new HashMap<>();//내부 로컬기기의 미디어파일의 경로
    private RelativeLayout postMediaHandleLayout; //이미지 누르면 나오는 삭제버튼 레이아웃
    private RelativeLayout writePostActivityLayout; //전체 레이아웃
    private ImageView selectedImage=null; //삭제할 이미지를 클릭
    private RelativeLayout loader;
    static class SortByMediaVal implements Comparator<String> { //날짜별로 최신순으로 정렬하기 위함
        @Override
        public int compare(String a, String b) {
            String[] splitList_a=a.split("mediaVal");
            String[] splitList_af=splitList_a[1].split("\\.");
            String val_a=splitList_af[0];

            String[] splitList_b=b.split("mediaVal");
            String[] splitList_bf=splitList_b[1].split("\\.");
            String val_b=splitList_bf[0];

            return val_a.compareTo(val_b);
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        setID();
        setEvents();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }
    public void setID(){
        parent=findViewById(R.id.IVbucketsLayout);
        identify=findViewById(R.id.identifyPostButton);
        addImage=findViewById(R.id.addImageButton);
        addVideo=findViewById(R.id.addVideoButton);
        deleteMedia=findViewById(R.id.deleteMediaButton);
        title=findViewById(R.id.titleEdittext);
        contents=findViewById(R.id.contentsEdittext);
        postMediaHandleLayout=findViewById(R.id.postMediaHandleLayout);
        writePostActivityLayout=findViewById(R.id.writePostActivityLayout);
        loader=findViewById(R.id.viewLoader);
    }
    public void setEvents(){
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                writePost();
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE); //이미지만
                startActivityForResult(intent,IMAGE_LOAD_CODE);
            }
        });
        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Video.Media.CONTENT_TYPE); //비디오만
                startActivityForResult(intent,VIDEO_LOAD_CODE);
            }
        });
        writePostActivityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(postMediaHandleLayout.getVisibility()==View.VISIBLE){
                    postMediaHandleLayout.setVisibility(View.GONE);
                    selectedImage=null;
                }
            }
        });
        deleteMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImage!=null) {
                    parent.removeView(selectedImage);
                    postMediaHandleLayout.setVisibility(View.GONE);
                    pathList.remove(selectedImage);
                }
            }
        });
    }
    public void writePost(){
        final String myTitle=title.getText().toString();
        final String myContents=contents.getText().toString();
        final ArrayList<String> storagePath=new ArrayList<>(); //스토리지에 저장된 다수의 이미지경로를 저장
        final ArrayList<String> formats=new ArrayList<>();

        if(myTitle.length()>0 && myContents.length()>0) {
            FirebaseStorage storage=FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference=firebaseFirestore.collection("posts").document(); //posts 경로의 만들어진 임의의 문서id 참조

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Collection<String> values=pathList.values();
            Object[] pathValList=values.toArray();
            for(int i=0;i<pathList.size();i++){ //미디어 파일 갯수에 맞게,
                String pathVal=pathValList[i].toString();

                if(isImageFile(pathVal)){ //이미지파일이면,
                    formats.add("image");
                }
                else if(isVideoFile(pathVal)){
                    formats.add("video");
                }

                String[] extension=pathVal.split("\\.");
                final StorageReference mediaRef = storageRef.child("posts/"+documentReference.getId()+"/mediaVal"+i+"."+extension[extension.length-1]); //해당 경로로 이미지를 저장
                UploadTask uploadTask = null;

                InputStream stream = null;

                try {
                    stream = new FileInputStream(new File(pathVal)); //해당 경로로의 스트림 형성
                    uploadTask = mediaRef.putStream(stream); //기기내부의 로컬경로로 부터 저장
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { //저장하면서,
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return mediaRef.getDownloadUrl();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUri = uri; //파이어베이스 스토리지 URI
                        storagePath.add(downloadUri.toString()); //각 미디어파일의 경로(파이어베이스 스토리지 경로)를 저장한 어레이

                        if(storagePath.size()==pathList.size()){ //모두 스토리지에 업로드 되었다면,

                            //정렬필요
                            Collections.sort(storagePath,new SortByMediaVal()); //비디오가 업로드가 늦기때문에 인덱스가 섞일 수 있음.

                             PostData data = new PostData(myTitle, myContents,user.getUid(),storagePath,new Date(),formats,new ArrayList<Pair>());
                             documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() { //위에서 임의로 생성한 문서id에 set으로 최신화하여 저장함.
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                             loader.setVisibility(View.GONE);
                            Toast.makeText(WritePostActivity.this,"게시글을 성공적으로 업로드했습니다.",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
            if(pathList.size()==0){ //이미지를 게시글에 포함시키지 않았을 경우,
                PostData data = new PostData(myTitle, myContents,user.getUid(),storagePath,new Date(),formats,new ArrayList<Pair>());
                documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() { //위에서 임의로 생성한 문서id에 set으로 최신화하여 저장함.
                    @Override
                    public void onSuccess(Void aVoid) {
                        loader.setVisibility(View.GONE);
                        Toast.makeText(WritePostActivity.this,"게시글을 성공적으로 업로드했습니다.",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }
        else{
            loader.setVisibility(View.GONE);
            Toast.makeText(WritePostActivity.this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_LOAD_CODE || requestCode==VIDEO_LOAD_CODE){
            if(resultCode==Activity.RESULT_OK){
                mediaPath=getPath(data.getData());

                //동적으로 ImageView 생성
                ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(250,250);
                ImageView imageView=new ImageView(WritePostActivity.this);
                imageView.setPadding(0,0,10,0);
                imageView.setLayoutParams(layoutParams);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedImage=(ImageView)v;
                        postMediaHandleLayout.setVisibility(View.VISIBLE);
                    }
                });
                parent.addView(imageView); //생성한 이미지뷰 삽입

                Glide.with(this).load(mediaPath).override(1000).into(imageView);

                pathList.put(imageView,mediaPath);
            }
        }
    }
    public String getPath(Uri uri){ //적절한 path로 바꿔주는 코드
        String[] proj={MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader=new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        int index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }
    public static boolean isImageFile(String path){
        String type= URLConnection.guessContentTypeFromName(path);
        return (type != null && type.startsWith("image"));
    }
    public static boolean isVideoFile(String path){
        String type= URLConnection.guessContentTypeFromName(path);
        return (type != null && type.startsWith("video"));
    }
}
