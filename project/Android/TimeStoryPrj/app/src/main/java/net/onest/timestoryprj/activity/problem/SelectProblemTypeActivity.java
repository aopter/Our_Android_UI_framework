package net.onest.timestoryprj.activity.problem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import net.onest.timestoryprj.R;
import net.onest.timestoryprj.constant.Constant;
import net.onest.timestoryprj.entity.Dynasty;
import net.onest.timestoryprj.entity.UserUnlockDynasty;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectProblemTypeActivity extends AppCompatActivity {

    //是否解锁当前朝代
    private boolean isUnLock = false;
    //连线
    @BindView(R.id.type_problem_lian)
    LinearLayout llProblemLian;

    //选择
    @BindView(R.id.type_problem_xuan)
    LinearLayout llProblemXuan;
    //排序
    @BindView(R.id.type_problem_pai)
    LinearLayout llProblemPai;
    //快速
    @BindView(R.id.type_problem_all)
    LinearLayout llProblemAll;

    private String dynastyId;

    @BindViews({R.id.iv_xuan,R.id.iv_lian,R.id.iv_pai,R.id.iv_all})
    ImageView[] ivGif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_problem_type);
        ButterKnife.bind(this);

        Glide.with(SelectProblemTypeActivity.this).asGif().load(R.mipmap.gif_problem_f).into(ivGif[3]);
        Glide.with(SelectProblemTypeActivity.this).asGif().load(R.mipmap.gif_problem_s).into(ivGif[1]);
        Glide.with(SelectProblemTypeActivity.this).asGif().load(R.mipmap.gif_problem_t).into(ivGif[2]);
        Glide.with(SelectProblemTypeActivity.this).asGif().load(R.mipmap.gif_problem_ff).into(ivGif[0]);




//        获得当前朝代
        Intent intent = getIntent();
        dynastyId = intent.getStringExtra("dynastyId1");
//        获得是否解锁
//        Constant.UnlockDynasty
        for(UserUnlockDynasty userUnlockDynasty:Constant.UnlockDynasty){
            userUnlockDynasty.getDynastyId().equals(dynastyId);//解锁
            isUnLock = true;
        }
//        获得解锁进度


//        准备答题

//        /problem/ready/{dynastyId}
//        三道题


    }

//   选择题目种类跳转
    @OnClick(R.id.type_problem_lian)
    public void llProblemLianOnClick(){
//        跳转
        Intent intent = new Intent();
        intent.putExtra("before","types");//从选择的界面跳转
        intent.putExtra("type","lian");
        intent.putExtra("dynastyId",dynastyId);
        intent.setClass(SelectProblemTypeActivity.this, ProblemInfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in_right,R.anim.anim_out_left);

    }
    @OnClick(R.id.type_problem_xuan)
    public void llProblemXuanOnClick(){
//        选择题

        Intent intent = new Intent();
        intent.putExtra("before","types");
        intent.putExtra("type","xuan");
        intent.putExtra("dynastyId",dynastyId);
        intent.setClass(SelectProblemTypeActivity.this, ProblemInfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in_right,R.anim.anim_out_left);
    }
    @OnClick(R.id.type_problem_pai)
    public void llProblemPaiOnClick(){
        //        排序题
        Intent intent = new Intent();
        intent.putExtra("before","types");
        intent.putExtra("type","pai");
        intent.putExtra("dynastyId",dynastyId);
        intent.setClass(SelectProblemTypeActivity.this, ProblemInfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in_right,R.anim.anim_out_left);

    }
    @OnClick(R.id.type_problem_all)
    public void llProblemAllOnClick(){
        JumpProblemInfoActoivity("all");
    }

    private void JumpProblemInfoActoivity(String type){
        Intent intent = new Intent();
        intent.putExtra("before","types");
        intent.putExtra("type",type);
        intent.putExtra("dynastyId",dynastyId);
        intent.setClass(SelectProblemTypeActivity.this, ProblemInfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in_right,R.anim.anim_out_left);
    }
}
