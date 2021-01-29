package org.messanger.snsproject.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.messanger.snsproject.Data.ProcessedPostData;
import org.messanger.snsproject.Listener.OnCommentListener;
import org.messanger.snsproject.Listener.OnPostListener;
import org.messanger.snsproject.R;
import org.messanger.snsproject.structure.Pair;

import java.util.ArrayList;
import java.util.Map;

public class MainPostingRecyclerViewAdapter extends RecyclerView.Adapter<MainPostingRecyclerViewAdapter.ItemHolder>{
    private ArrayList<ProcessedPostData> arrayList;
    private Activity activity;
    private String userId,name=null;
    private OnPostListener onPostListener;
    private OnCommentListener onCommentListener;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    public MainPostingRecyclerViewAdapter(Activity activity, ArrayList<ProcessedPostData> list, String uid, OnPostListener onPostListener, OnCommentListener onCommentListener){
        this.activity=activity;
        arrayList=list;
        userId=uid; //로그인한 유저의 uid
        this.onPostListener=onPostListener;
        this.onCommentListener=onCommentListener;

        DocumentReference documentReference=db.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                Map<String,Object> map = document.getData();
                if(map!=null)
                    name = map.get("myname").toString()+" : "; //이름정보
            }
        });
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //하나하나 ItemHolder를 생성(View)
        ItemHolder itemHolder= new ItemHolder(activity,(RelativeLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.board_items,parent,false),arrayList.get(viewType),userId);

        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, final int position) {//데이터 바인딩은 여기서 해야한다.
        RelativeLayout parentLayout=holder.parent;
        LinearLayout postedMediaLayout,commentsLayout;
        ImageView writerProfile,menu; //작성자 프로필 사진
        TextView writerName,postedContents,postedTitle; //작성자 이름, 작성된 내용
        Button commentsSave;
        final EditText commentsWrite;
        ArrayList<String> formats=arrayList.get(position).getFormats();
        final ArrayList<Pair> commentList=arrayList.get(position).getComments();

        writerProfile=parentLayout.findViewById(R.id.writerProfileImage);
        menu=parentLayout.findViewById(R.id.menuImageView);
        writerName=parentLayout.findViewById(R.id.writerName);
        postedTitle=parentLayout.findViewById(R.id.postedTitle);
        postedContents=parentLayout.findViewById(R.id.postedContents);
        postedMediaLayout=parentLayout.findViewById(R.id.postedMediaLayout);
        commentsLayout=parentLayout.findViewById(R.id.commentsLayout);
        commentsSave=parentLayout.findViewById(R.id.commentsSaveButton);
        commentsWrite=parentLayout.findViewById(R.id.commentsWriteEditText);

        writerName.setText(arrayList.get(position).getName()); //작성자 이름
        postedContents.setText(arrayList.get(position).getContents()); //텍스트 콘텐츠
        postedTitle.setText(arrayList.get(position).getTitle()); //제목
        Glide.with(holder.parent).load(arrayList.get(position).getWriterProfImage()).centerCrop().override(500).into(writerProfile); //프로필 사진

        commentsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(commentsWrite.getText().toString().length()<=0) {
                    Toast.makeText(activity, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String comment=name+commentsWrite.getText().toString()+"\n";
                onCommentListener.onSave(arrayList.get(position).getPostId(),userId,comment,commentList,position);
                commentsWrite.setText("");

            }
        });

        if(menu.getVisibility()!=View.GONE){
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(v,position);
                }
            });
        }

        ArrayList<String> mediaList=arrayList.get(position).getMediaBox(); //이미지 또는 비디오의 파일 갯수

        for (int i = 0; i < mediaList.size(); i++) {
            String content = mediaList.get(i);
            if(postedMediaLayout.getChildAt(i)==null){
                Toast.makeText(activity,"오류",Toast.LENGTH_SHORT).show();
            }
            else {
                if(formats.get(i).equals("image"))
                    Glide.with(activity).load(content).override(1000).thumbnail(0.1f).into((ImageView) postedMediaLayout.getChildAt(i));
                else{
                    final PlayerView playerView=(PlayerView) postedMediaLayout.getChildAt(i);
                    SimpleExoPlayer player =holder.player;
                    playerView.setPlayer(player);

                    MediaItem mediaItem = MediaItem.fromUri(content);
                    player.setMediaItem(mediaItem);
                    player.prepare();
                }
            }
        }

        for(int i=0;i<commentList.size();i++){
            final int idx=i;
            String uid = commentList.get(i).getUid();
            String comment=commentList.get(i).getComment();
            if(uid.equals(userId)){
                commentsLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                        builder.setTitle("댓글 삭제 알림").setMessage("해당 댓글을 삭제 하시겠습니까?");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                onCommentListener.onDelete(arrayList.get(position).getPostId(),idx,commentList,position);
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                return;
                            }
                        });
                        builder.create().show();
                    }
                });
            }
            ((TextView)commentsLayout.getChildAt(idx)).setText(comment);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) { //viewType를 제대로 반환하기 위한 오버라이딩 코드
        return position;
    }

    public void showPopup(View v,final int position){
        PopupMenu popup=new PopupMenu(activity,v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.remove:{
                        ArrayList<String> mediaList=arrayList.get(position).getMediaBox();
                        ArrayList<String> mediaFiles=new ArrayList<>();
                        for(int i=0;i<mediaList.size();i++){
                            String[] splitList=mediaList.get(i).split("\\?"); //'?'단위로 스플릿
                            String[] splitList2=splitList[0].split("%2F");
                            mediaFiles.add(splitList2[splitList2.length-1]);
                        }
                        onPostListener.onDelete(arrayList.get(position).getPostId(),mediaFiles);
                    return true;
                    }
                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater=popup.getMenuInflater();
        inflater.inflate(R.menu.post_menu,popup.getMenu());
        popup.show();
    }

    static class ItemHolder extends RecyclerView.ViewHolder{ //레이아웃의 동적 생성은 여기서 진행하자. (뷰를 만들때만)
        RelativeLayout parent;
        LinearLayout postedMediaLayout,commentsLayout;
        ImageView menu;
        SimpleExoPlayer player;

        ItemHolder(Activity activity,@NonNull RelativeLayout itemView,ProcessedPostData postData,String uid) {
            super(itemView);
            parent=itemView.findViewById(R.id.postingParent);
            postedMediaLayout=itemView.findViewById(R.id.postedMediaLayout);
            commentsLayout=itemView.findViewById(R.id.commentsLayout);

            menu=itemView.findViewById(R.id.menuImageView);
            player= new SimpleExoPlayer.Builder(activity).build();

            ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            ArrayList<String> mediaList=postData.getMediaBox();
            ArrayList<String> formats=postData.getFormats();
            ArrayList<Pair> commentsList=postData.getComments();

            if(!postData.getMyUid().equals(uid)){ //게시물의 uid와 로그인한 유저의 uid가 다르다면,
                menu.setVisibility(View.GONE);
            }

            if(postedMediaLayout.getChildCount()==0) {
                for (int i = 0; i < mediaList.size(); i++) {
                    if(formats.get(i).equals("image")) { //이미지파일 이라면,
                        ImageView imageView = new ImageView(activity);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY); //꽉차게 이미지 삽입
                        imageView.setPadding(0, 0, 0, 15);
                        postedMediaLayout.addView(imageView);
                    }
                    else{ //비디오 파일이라면,
                        LayoutInflater layoutInflater=activity.getLayoutInflater();
                        final PlayerView playerView=(PlayerView)layoutInflater.inflate(R.layout.view_contents_player,parent,false);
                        postedMediaLayout.addView(playerView);
                        player.addVideoListener(new VideoListener() { //사이즈 설정
                            @Override
                            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                                playerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
                            }
                        });
                    }
                }
            }

            if(commentsLayout.getChildCount()==0){
                for(int i=0;i<commentsList.size();i++){
                    TextView textView=new TextView(activity);
                    textView.setLayoutParams(layoutParams);
                    textView.setTextSize(15);
                    textView.setPadding(5,5,5,5);
                    commentsLayout.addView(textView);
                }
            }
        }
    }
}
