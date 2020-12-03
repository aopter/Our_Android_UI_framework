package net.onest.timestoryprj.activity.problem;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.tinytongtong.tinyutils.LogUtils;
import com.tinytongtong.tinyutils.ScreenUtils;

import net.onest.timestoryprj.R;
import net.onest.timestoryprj.adapter.problem.OnStartDragListener;
import net.onest.timestoryprj.adapter.problem.OptionLianAdapter;
import net.onest.timestoryprj.constant.Constant;
import net.onest.timestoryprj.constant.ServiceConfig;
import net.onest.timestoryprj.customview.LinkLineView;
import net.onest.timestoryprj.dialog.problem.ProblemAnswerDialog;
import net.onest.timestoryprj.entity.LinkDataBean;
import net.onest.timestoryprj.entity.LinkLineBean;
import net.onest.timestoryprj.entity.Problem;
import net.onest.timestoryprj.entity.problem.OrderBean;
import net.onest.timestoryprj.entity.problem.ProblemLinkLine;
import net.onest.timestoryprj.entity.problem.ProblemSelect;
import net.onest.timestoryprj.entity.problem.ProblemgetOrder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ProblemInfoActivity extends AppCompatActivity {


    private boolean isGetAnswer = false;
    private String dynastyId;
    @BindView(R.id.link_line_view)
    LinkLineView linkLineView;
    @BindView(R.id.fl_link_line)
    FrameLayout flLinkLine;
    @BindView(R.id.tv_result)
    TextView tvResult;

    @BindView(R.id.type_xuan)
    LinearLayout llTypeXuan;
    @BindView(R.id.type_pai)
    LinearLayout llTypePai;
    @BindView(R.id.type_lian)
    LinearLayout llTypeLian;
    LinearLayout llTypeAll;

    @BindView(R.id.re_pai)
    RecyclerView rePai;
    @BindView(R.id.iv_gif_bg)
    ImageView ivGif;


    @BindView(R.id.problem_answer)
    Button btnAnswer;

    @BindView(R.id.title_lian)
    TextView tvLianTitle;

    @BindView(R.id.problem_save)
    Button btnProblemSave;

    @BindView(R.id.problem_xuan_info_title)
    TextView tv_problem_xuan_info_title;

    @BindViews({R.id.iv_optionA, R.id.iv_optionB, R.id.iv_optionC, R.id.iv_optionD})
    ImageView[] ivOptionsXuan;

    @BindViews({R.id.tv_optionA, R.id.tv_optionB, R.id.tv_optionC, R.id.tv_optionD})
    TextView[] tvOptionsXuan;

    @BindView(R.id.re_container)
    RelativeLayout reContainer;

    //   选择题选项的linear
    @BindViews({R.id.optionA, R.id.optionB, R.id.optionC, R.id.optionD})
    LinearLayout[] llOptionsXuan;

    @BindView(R.id.btn_pai_to_check)
    Button btnToCheck;

    @BindView(R.id.tv_pai_title)
    TextView tvPaiTitle;

    //   三种题目
    Problem cProblem;
    ProblemSelect problemSelect;
    ProblemgetOrder problemgetOrder;
    ProblemLinkLine problemLinkLine;
    private int cType;
    private int cIndex = 0;//当前索引
    private PromptDialog promptDialog;

    //    左右屏幕的滑动事件
    final int RIGHT = 0;
    final int LEFT = 1;
    private GestureDetector gestureDetector;

    private List<Problem> myProblems;

    private Animation animationin;
    private Animation animationout;
    private String before;


    private Handler handler = new Handler(
    ) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.arg1) {
                case 1://选择
                    tv_problem_xuan_info_title.setText(problemSelect.getTitle());
                    tvOptionsXuan[0].setText(problemSelect.getOptionA());
                    tvOptionsXuan[1].setText(problemSelect.getOptionB());
                    tvOptionsXuan[2].setText(problemSelect.getOptionC());
                    tvOptionsXuan[3].setText(problemSelect.getOptionD());
                    String url = ServiceConfig.SERVICE_ROOT + "/picture/download/";
                    LogUtils.d(url + problemSelect.getOptionApic());
                    try {
                        Glide.with(ProblemInfoActivity.this).load(new URL(url + problemSelect.getOptionApic())).into(ivOptionsXuan[0]);
                        Glide.with(ProblemInfoActivity.this).load(new URL(url + problemSelect.getOptionBpic())).into(ivOptionsXuan[1]);
                        Glide.with(ProblemInfoActivity.this).load(new URL(url + problemSelect.getOptionCpic())).into(ivOptionsXuan[2]);
                        Glide.with(ProblemInfoActivity.this).load(new URL(url + problemSelect.getOptionDpic())).into(ivOptionsXuan[3]);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    break;
                case 2://连线
//                    linkLineView = new LinkLineView(ProblemInfoActivity.this);
                    tvLianTitle.setText(problemLinkLine.getTitle());
                    List<LinkDataBean> linkDataBeans = (List<LinkDataBean>) msg.obj;
                    // 先清除掉原有绘制的线
                    Log.e("handleMessage收到: ", problemLinkLine.toString());
                    linkLineView.setData(linkDataBeans);
                    linkLineView.setOnChoiceResultListener((correct, yourAnswer) -> {
                        // 结果
                        StringBuilder sb = new StringBuilder();
                        sb.append("正确与否：");
                        sb.append(correct);
                        sb.append("\n");
                        tvResult.setText(sb.toString());
//                        显示解析
                        btnAnswer.setVisibility(View.VISIBLE);

                    });
                    break;
                case 3://排序
                    tvPaiTitle.setText(problemgetOrder.getTitle());
//                    {"李白乘舟将欲行", "不及汪伦送我情", "忽闻岸上踏歌声", "桃花潭水深千尺"};
                    int colnum = 2;
                    if (problemgetOrder.getContents().size() > 4)
                        colnum = 3;
                    if (problemgetOrder.getContents().size() > 6)
                        colnum = 4;
                    rePai.setLayoutManager(new GridLayoutManager(ProblemInfoActivity.this, colnum));
//        rv.setLayoutManager(new LinearLayoutManager(this));
                    OptionLianAdapter adapter = new OptionLianAdapter(problemgetOrder.getContents());
                    rePai.setAdapter(adapter);
                    //为RecycleView绑定触摸事件
                    ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                        @Override
                        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                            //首先回调的方法 返回int表示是否监听该方向
                            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//拖拽
                            int swipeFlags = 0;//侧滑删除
                            return makeMovementFlags(dragFlags, swipeFlags);
                        }

                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            //滑动事件
                            Log.e("getMovementFlags: ", "滑动");
                            int fromPosition = viewHolder.getAdapterPosition();
                            int toPosition = target.getAdapterPosition();

//                            Collections.swap(problemgetOrder.getContents(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                            return onMove(fromPosition, toPosition);
                        }


                        public boolean onMove(int fromPosition, int toPosition) {
                            if (fromPosition < toPosition) {
                                //从上往下拖动，每滑动一个item，都将list中的item向下交换，向上滑同理。
                                for (int i = fromPosition; i < toPosition; i++) {
                                    Collections.swap(problemgetOrder.getContents(), i, i + 1);
                                }
                            } else {
                                for (int i = fromPosition; i > toPosition; i--) {
                                    Collections.swap(problemgetOrder.getContents(), i, i - 1);
                                }
                            }
                            adapter.notifyItemMoved(fromPosition, toPosition);
                            return true;
                        }


                        @Override
                        public boolean isLongPressDragEnabled() {
                            //是否可拖拽
                            return true;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        }

                    });
                    mDragStartListener = new OnStartDragListener() {
                        @Override
                        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                            helper.startDrag(viewHolder);
                        }
                    };
                    helper.attachToRecyclerView(rePai);


                    break;
                case 4://收藏
                    btnProblemSave.setText("已收藏");
                    promptDialog.showSuccess("收藏成功");
                    break;
                case 5://取消收藏
                    btnProblemSave.setText("收藏");
                    promptDialog.showSuccess("取消收藏成功");
                    break;
            }
        }
    };

    public static OnStartDragListener mDragStartListener;
    //    okhttp客户端类
    private OkHttpClient okHttpClient;
    private Gson gson;

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();

                    if (x > 0) {
                        doResult(RIGHT);
                    } else if (x < 0) {
                        doResult(LEFT);
                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_info);
        ButterKnife.bind(this);

        promptDialog = new PromptDialog(this);
        promptDialog.getDefaultBuilder().touchAble(true).round(3).loadingDuration(3000);
//        动画
        animationin = AnimationUtils.loadAnimation(
                ProblemInfoActivity.this, R.anim.anim_in_right);

        animationout = AnimationUtils.loadAnimation(
                ProblemInfoActivity.this, R.anim.anim_in_left_problem);
//        左右触屏
        gestureDetector = new GestureDetector(ProblemInfoActivity.this, onGestureListener);
        myProblems = new ArrayList<>();
        okHttpClient = new OkHttpClient();
        gson = new Gson();
        cProblem = new Problem();

        Glide.with(this).load(R.mipmap.deng).into(ivGif);
        Intent intent = getIntent();

        String type = intent.getStringExtra("type");
//        Problem problem = (Problem) intent.getSerializableExtra("problem");
        dynastyId = intent.getStringExtra("dynastyId");
//        Log.e("朝代id: ", dynastyId);
        before = intent.getStringExtra("before");

        if (before.equals("types"))//类型页
        {
            btnAnswer.setVisibility(View.INVISIBLE);//解析
        }
        if (null != type) {
            switch (type) {
                case "xuan":
                    cType = 1;
                    llTypeLian.setVisibility(View.INVISIBLE);
                    llTypePai.setVisibility(View.INVISIBLE);
                    if (before.equals("info")) {//收藏页
                        btnProblemSave.setText("已收藏");
                        cIndex = intent.getIntExtra("position", 0);
                        problemSelect = (ProblemSelect) intent.getSerializableExtra("problem");
                        cProblem.setProblemId(problemSelect.getProblemId());
                        cProblem.setProblemDetails(problemSelect.getProblemDetails());
//                        cProblem
                        //跳转显示
                        Message message = new Message();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                        return;
                    }
//                   请求题目
                    cType = 1;
                    getProblem(1);
                    break;
                case "lian":
                    cType = 2;
                    llTypeXuan.setVisibility(View.INVISIBLE);
                    llTypePai.setVisibility(View.INVISIBLE);
                    if (before.equals("info")) {
                        btnProblemSave.setText("已收藏");
                        cIndex = intent.getIntExtra("position", 0);
                        problemLinkLine = (ProblemLinkLine) intent.getSerializableExtra("problem");
                        cProblem.setProblemId(problemLinkLine.getProblemId());
                        cProblem.setProblemDetails(problemLinkLine.getProblemDetails());

                        List<LinkDataBean> linkDataBeans = (List<LinkDataBean>) intent.getSerializableExtra("linkDataBeans");

                        //跳转显示
                        Message message = new Message();
                        message.arg1 = 2;
                        message.obj = linkDataBeans;//发送选择题
                        handler.sendMessage(message);
                        return;
                    }
                    getProblem(2);
                    break;
                case "all":
                    cType = 4;
                    getRandomProblem();
                    break;
                case "pai":
                    cType = 3;
                    llTypeXuan.setVisibility(View.INVISIBLE);
                    llTypeLian.setVisibility(View.INVISIBLE);
                    if (before.equals("info")) {
                        btnProblemSave.setText("已收藏");
                        cIndex = intent.getIntExtra("position", 0);
                        problemgetOrder = (ProblemgetOrder) intent.getSerializableExtra("problem");
                        cProblem.setProblemId(problemgetOrder.getProblemId());
                        cProblem.setProblemDetails(problemgetOrder.getProblemDetails());
                        //跳转显示
                        Message message = new Message();
                        message.arg1 = 3;
                        message.obj = problemgetOrder;//发送选择题
                        handler.sendMessage(message);
                        return;
                    }
                    getProblem(3);

                    break;
            }
        }
    }

    private void getRandomProblem() {
        //                   产生随机数
        int min = 1;
        int max = 3;
        int num = getMyRandom(min, max);
        llTypeLian.setVisibility(View.VISIBLE);
        llTypePai.setVisibility(View.VISIBLE);
        llTypeXuan.setVisibility(View.VISIBLE);

        Log.e("随机数: ", num + "");
        switch (num) {
            case 1://选择题
                llTypeLian.setVisibility(View.INVISIBLE);
                llTypePai.setVisibility(View.INVISIBLE);
                getProblem(1);
                break;
            case 2:
                llTypePai.setVisibility(View.INVISIBLE);
                llTypeXuan.setVisibility(View.INVISIBLE);
                getProblem(2);
//                            连线
                break;
            case 3://排序
                llTypeLian.setVisibility(View.INVISIBLE);
                llTypeXuan.setVisibility(View.INVISIBLE);
                getProblem(3);
                break;
        }
    }

    private int getMyRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    /**
     * 请求题目
     *
     * @param type
     */
    private void getProblem(int type) {
        btnAnswer.setVisibility(View.INVISIBLE);
        btnProblemSave.setText("收藏");
        LogUtils.d("长度suoyou", myProblems.size() + "");
        isGetAnswer = false;
        String url = ServiceConfig.SERVICE_ROOT + "/problem/replenish/" + type + "/" + dynastyId + "";
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        //构造请求类
        Request request = builder.build();
        final Call call = okHttpClient.newCall(request);
//        请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                promptDialog.showError("网络较差，请稍后");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                Problem problem = gson.fromJson(jsonData, Problem.class);
                cProblem = problem;
                String[] contents1 = problem.getProblemContent().split(Constant.DELIMITER);
                switch (type) {
                    case 1:
                        problemSelect = new ProblemSelect();
                        problemSelect.setDynastyId(dynastyId);
                        problemSelect.setProblemType(1);
                        problemSelect.setProblemId(problem.getProblemId());
                        problemSelect.setTitle(contents1[0]);
                        problemSelect.setOptionA(contents1[1]);
                        problemSelect.setOptionApic(contents1[2]);
                        problemSelect.setOptionB(contents1[3]);
                        problemSelect.setOptionBpic(contents1[4]);
                        problemSelect.setOptionC(contents1[5]);
                        problemSelect.setOptionCpic(contents1[6]);
                        problemSelect.setOptionD(contents1[7]);
                        problemSelect.setOptionDpic(contents1[8]);
                        problemSelect.setProblemKey(problem.getProblemKey());
                        problemSelect.setProblemDetails(problem.getProblemDetails());
                        myProblems.add(problemSelect);

//                        发送消息
                        Message message = new Message();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                        break;
                    case 2://连线题
                        //        初始化连线题
                        problemLinkLine = new ProblemLinkLine();
                        problemLinkLine.setDynastyId(dynastyId);
                        problemLinkLine.setProblemId(problem.getProblemId());
                        problemLinkLine.setProblemDetails(problem.getProblemDetails());
                        problemLinkLine.setProblemType(2);
                        problemLinkLine.setTitle(contents1[0]);
                        problemLinkLine.setOptionA(contents1[1]);
                        problemLinkLine.setOptionB(contents1[2]);
                        problemLinkLine.setOptionC(contents1[3]);
                        problemLinkLine.setOptionD(contents1[4]);
                        problemLinkLine.setOptionAdes(contents1[5]);
                        problemLinkLine.setOptionBdes(contents1[6]);
                        problemLinkLine.setOptionCdes(contents1[7]);
                        problemLinkLine.setOptionDdes(contents1[8]);
                        problemLinkLine.setProblemKey(problem.getProblemKey());
                        String problemKey = problem.getProblemKey();
                        String[] qNum = problemKey.split(Constant.DELIMITER);

                        List<LinkDataBean> linkDataBeans = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {
                            LinkDataBean linkDataBean = new LinkDataBean();
                            linkDataBean.setContent(contents1[i + 1]);
                            linkDataBean.setQ_num(Integer.parseInt(qNum[i]));
                            linkDataBean.setRow(i + 1);
                            linkDataBean.setCol(0);
                            linkDataBeans.add(linkDataBean);
                        }

                        for (int i = 0; i < 4; i++) {
                            LinkDataBean linkDataBean = new LinkDataBean();
                            linkDataBean.setContent(contents1[i + 5]);
                            linkDataBean.setQ_num(Integer.parseInt(qNum[i + 4]));
                            linkDataBean.setRow(i + 1);
                            linkDataBean.setCol(1);
                            linkDataBeans.add(linkDataBean);
                        }
                        myProblems.add(problemLinkLine);
                        Message msg = new Message();
                        msg.arg1 = 2;
                        msg.obj = linkDataBeans;
                        handler.sendMessage(msg);
                        break;
                    case 3:
                        problemgetOrder = new ProblemgetOrder();
                        problemgetOrder.setProblemDetails(problem.getProblemDetails());
                        problemgetOrder.setDynastyId(dynastyId);
                        problemgetOrder.setTitle(contents1[0]);
                        problemgetOrder.setProblemType(3);
                        problemgetOrder.setProblemKey(problem.getProblemKey());
                        problemgetOrder.setProblemId(problem.getProblemId());

                        String key = problem.getProblemKey();
                        List<OrderBean> orderBeans = new ArrayList<>();
                        for (int i = 0; i < key.length(); i++) {
                            //
                            OrderBean orderBean = new OrderBean();
                            orderBean.setContent(contents1[i + 1]);
                            orderBean.setOrder(Integer.parseInt(key.charAt(i) + ""));
                            orderBeans.add(orderBean);
                        }
                        problemgetOrder.setContents(orderBeans);
                        myProblems.add(problemgetOrder);
                        Message message1 = new Message();
                        message1.arg1 = 3;
                        handler.sendMessage(message1);
                        break;
                }
            }
        });
//
    }


    //    选择题点击事件
    @OnClick({R.id.problem_save, R.id.problem_answer,
            R.id.optionA, R.id.optionB, R.id.optionC, R.id.optionD, R.id.btn_pai_to_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.problem_save:
                Log.e("当先题目: ", cProblem.toString());
                String s = btnProblemSave.getText().toString();
                if (s.equals("收藏")) {//取消收藏
//                    /userproblem/collect/{userId}/{dynastyId}/{problemId}
                    String urlSaveProblem = ServiceConfig.SERVICE_ROOT + "/userproblem/collect/" +
                            1 + "/" + dynastyId + "/" + cProblem.getProblemId() + "";
                    Log.e("urlSaveProblem: ", urlSaveProblem);
                    Request.Builder builder = new Request.Builder();
                    builder.url(urlSaveProblem);
                    //构造请求类
                    Request request = builder.build();
                    final Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("onFailure: ", "收藏失败");
                            //稍后再试
                            promptDialog.showError("网络较差，请稍后");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String data = response.body().string();
                            Message message = new Message();
                            message.arg1 = 4;
                            handler.sendMessage(message);
                        }
                    });
                } else {
//                    /userproblem/uncollect/{userId}/{dynastyId}/{problemId}
                    String urlSaveProblem = ServiceConfig.SERVICE_ROOT + "/userproblem/uncollect/" +
                            1 + "/" + dynastyId + "/" + cProblem.getProblemId() + "";
                    Request.Builder builder = new Request.Builder();
                    builder.url(urlSaveProblem);
                    //构造请求类
                    Request request = builder.build();
                    final Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("onFailure: ", "取消收藏失败");
                            //稍后再试
                            //
                            promptDialog.showError("网络较差，请稍后");

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
//
                            Message message = new Message();
                            message.arg1 = 5;
                            handler.sendMessage(message);
                        }
                    });


                }
                break;
            case R.id.problem_answer:
                getProblemAnswer();
                break;
            case R.id.optionA:
//                判断正误的方法
                checkUserXuanAnswer(problemSelect.getOptionA());
                break;
            case R.id.optionB:
                checkUserXuanAnswer(problemSelect.getOptionB());
                break;
            case R.id.optionC:
                checkUserXuanAnswer(problemSelect.getOptionC());

                break;
            case R.id.optionD:
                checkUserXuanAnswer(problemSelect.getOptionD());
                break;
            case R.id.btn_pai_to_check:
                if (isGetAnswer) {
                    return;
                }
//                检查题目
                List<OrderBean> orderBeans = problemgetOrder.getContents();
                isGetAnswer = true;
                for (int i = 0; i < orderBeans.size(); i++) {

//                    检查
                    int postion = i + 1;
                    if (orderBeans.get(i).getOrder() != postion) {
                        //错误
                        promptDialog.showInfo("很遗憾，回答错误~");
                        //Toast.makeText(getApplicationContext(), "回答错误", Toast.LENGTH_SHORT).show();
                        btnAnswer.setVisibility(View.VISIBLE);
                        return;
                    }
                }
                promptDialog.showInfo("恭喜你，回答正确喽！");
                // Toast.makeText(getApplicationContext(), "回答正确", Toast.LENGTH_SHORT).show();
                btnAnswer.setVisibility(View.VISIBLE);
                break;
        }
    }

    //    检查正误
    private void checkUserXuanAnswer(String option) {
        if (isGetAnswer) {
            return;
        }
        if (problemSelect.getProblemKey().equals(option) && !isGetAnswer) {
            isGetAnswer = true;
//          正确 加积分加经验
            //Toast.makeText(getApplicationContext(), "回答正确", Toast.LENGTH_SHORT).show();
            promptDialog.showInfo("恭喜你，回答正确喽！");

            //          /problem/answer/{userId}/{dynastyId}/{problemId}/{result}
            //显示查看解析
            btnAnswer.setVisibility(View.VISIBLE);
        } else {
            isGetAnswer = true;
            promptDialog.showInfo("很遗憾，回答错误~");

            //Toast.makeText(getApplicationContext(), "回答错误", Toast.LENGTH_SHORT).show();
            btnAnswer.setVisibility(View.VISIBLE);
        }
    }

//    选择题点击事件
//@OnClick({R.id.optionA,R.id.optionB,R.id.optionC,R.id.optionD})


    /**
     * 显示答案弹窗
     */
    private void getProblemAnswer() {
        ProblemAnswerDialog dialog = new ProblemAnswerDialog(this);
        dialog.setDetail(cProblem.getProblemDetails());
        dialog.show();
    }


    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }


    //手势滑动
    private void doResult(int action) {
        LogUtils.d("长度", myProblems.size() + "");
        switch (action) {
            case LEFT:
                cIndex += 1;
                if (before.equals("info")) {
                    return;
                }
                if (myProblems.size() > cIndex) {//下一道
                    getBeforeOrNextProblem();//显示
                    animationin.setDuration(600);
                    reContainer.startAnimation(animationin);
                    return;
                } else {
                    LogUtils.d("获取题目");
                    LogUtils.d(cType + "aaa");
                    if (cType == 4) {//随机
                        getRandomProblem();
                    } else {
                        getProblem(cProblem.getProblemType());
                    }
                }
                animationin.setDuration(600);
                reContainer.startAnimation(animationin);
                break;
            case RIGHT:
                cIndex -= 1;
                if (before.equals("info")) {
                    return;
                }
                if (cIndex < 0) {//上一道
//                    提示不能滑动
                    cIndex = 0;
                    Toast.makeText(getApplicationContext(), "不能再滑啦", Toast.LENGTH_SHORT).show();
                    return;
                } else {
//                    显示
                    getBeforeOrNextProblem();//显示上一道题目
                }
                animationout.setDuration(600);
                reContainer.startAnimation(animationout);
                break;
        }
    }

    private void getBeforeOrNextProblem() {
        LogUtils.d("类型", "");
        Problem problem = myProblems.get(cIndex);
        switch (problem.getProblemType()) {
            case 1://选择
                if (cType == 4) {
                    llTypeLian.setVisibility(View.INVISIBLE);
                    llTypePai.setVisibility(View.INVISIBLE);
                    llTypeXuan.setVisibility(View.VISIBLE);
                }
                problemSelect = (ProblemSelect) problem;
                Message message = new Message();
                message.arg1 = 1;
                handler.sendMessage(message);
                break;
            case 2:
                if (cType == 4) {
                    llTypeLian.setVisibility(View.VISIBLE);
                    llTypePai.setVisibility(View.INVISIBLE);
                    llTypeXuan.setVisibility(View.INVISIBLE);
                } else {
                    llTypeLian.setVisibility(View.INVISIBLE);
                    llTypeLian.setVisibility(View.VISIBLE);
                }

                problemLinkLine = (ProblemLinkLine) problem;
                String[] qNum = problemLinkLine.getProblemKey().split(Constant.DELIMITER);

                List<LinkDataBean> linkDataBeans = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    LinkDataBean linkDataBean = new LinkDataBean();
                    switch (i) {
                        case 0:
                            linkDataBean.setContent(problemLinkLine.getOptionA());
                            break;
                        case 1:
                            linkDataBean.setContent(problemLinkLine.getOptionB());
                            break;
                        case 2:
                            linkDataBean.setContent(problemLinkLine.getOptionC());
                            break;
                        case 3:
                            linkDataBean.setContent(problemLinkLine.getOptionD());
                            break;
                    }
                    linkDataBean.setQ_num(Integer.parseInt(qNum[i]));
                    linkDataBean.setRow(i + 1);
                    linkDataBean.setCol(0);
                    linkDataBeans.add(linkDataBean);
                }

                for (int i = 0; i < 4; i++) {
                    LinkDataBean linkDataBean = new LinkDataBean();
                    switch (i) {
                        case 0:
                            linkDataBean.setContent(problemLinkLine.getOptionAdes());
                            break;
                        case 1:
                            linkDataBean.setContent(problemLinkLine.getOptionBdes());
                            break;
                        case 2:
                            linkDataBean.setContent(problemLinkLine.getOptionCdes());
                            break;
                        case 3:
                            linkDataBean.setContent(problemLinkLine.getOptionDdes());
                            break;
                    }
                    linkDataBean.setQ_num(Integer.parseInt(qNum[i + 4]));
                    linkDataBean.setRow(i + 1);
                    linkDataBean.setCol(1);
                    linkDataBeans.add(linkDataBean);
                }

                Message message2 = new Message();
                message2.arg1 = 2;
                message2.obj = linkDataBeans;
                handler.sendMessage(message2);
                break;
            case 3:
                if (cType == 4) {
                    llTypeLian.setVisibility(View.INVISIBLE);
                    llTypePai.setVisibility(View.VISIBLE);
                    llTypeXuan.setVisibility(View.INVISIBLE);
                }
                problemgetOrder = (ProblemgetOrder) problem;
                Message message1 = new Message();
                message1.arg1 = 3;
                handler.sendMessage(message1);
                break;
        }
    }


//    private void getBeforeOrNextProblem(Problem problem) {
//        LogUtils.d("类型", problem.getProblemType() + "");
//        switch (problem.getProblemType()) {
//            case 1://选择
//                LogUtils.d("收到的选择");
//                if (null == problemSelect) {
//                    problemSelect = new ProblemSelect();
//                }
//                problemSelect = (ProblemSelect) problem;
//                LogUtils.d("收到的选择", problemSelect.toString());
//
//                Message message = new Message();
//                message.arg1 = 1;
//                handler.sendMessage(message);
//                break;
//            case 2:
//                problemLinkLine = (ProblemLinkLine) problem;
//                Message message2 = new Message();
//                message2.arg1 = 2;
//                handler.sendMessage(message2);
//                break;
//            case 3:
//                problemgetOrder = (ProblemgetOrder) problem;
//                LogUtils.d("排序接收到的", problemgetOrder.toString());
//                Message message1 = new Message();
//                message1.arg1 = 3;
//                handler.sendMessage(message1);
//                break;
//        }
//    }


    //    private void doResult(int action) {
//        switch (action) {
//            case LEFT:
//                LogUtils.d("before",before);
//                cIndex += 1;
//                switch (before) {
//                    case "types":
//                        if (myProblems.size() > cIndex) {
//                            Problem problem = myProblems.get(cIndex);
//                            getBeforeOrNextProblem(problem);//显示
//                            return;
//                        } else {
//                            LogUtils.d("获取题目");
//                            LogUtils.d(cType + "aaa");
//                            if (cType == 4) {
//                                getRandomProblem();
//                            } else {
//                                getProblem(cProblem.getProblemType());
//                            }
//                        }
//                        animationin.setDuration(600);
//                        reContainer.startAnimation(animationin);
//                        break;
//                    case "info":
//                        LogUtils.d(Constant.userProblems.size()+"长度");
//                        LogUtils.d(cIndex+"index");
//                        if (Constant.userProblems.size() > cIndex) {
//                            Problem problem = Constant.userProblems.get(cIndex);
//                            LogUtils.d(problem.toString());
//                            llTypeLian.setVisibility(View.INVISIBLE);
//                            llTypePai.setVisibility(View.INVISIBLE);
//                            llTypeXuan.setVisibility(View.INVISIBLE);
//                            switch (problem.getProblemType()){//显示题目 下一道
//                                case 1:
//                                    llTypeXuan.setVisibility(View.VISIBLE);
//                                    problemSelect = (ProblemSelect) problem;
//                                    Message message = new Message();
//                                    message.arg1 = 1;
//                                    handler.sendMessage(message);
//                                    break;
//                                case 2:
//                                    llTypeLian.setVisibility(View.VISIBLE);
//                                    LogUtils.d("zhuanhuanqian:",problem.toString());
//                                    problemLinkLine = (ProblemLinkLine) problem;
//                                    LogUtils.d("zhuanhuan后:",problem.toString());
//
//                                    String problemKey = problemLinkLine.getProblemKey();
//                                    String[] qNum = problemKey.split(Constant.DELIMITER);
//                                    List<LinkDataBean> linkDataBeans = new ArrayList<>();
//                                    for (int i = 0; i < 4; i++) {
//                                        LinkDataBean linkDataBean = new LinkDataBean();
//                                        switch (i){
//                                            case 0:
//                                                linkDataBean.setContent(problemLinkLine.getOptionA());
//                                                break;
//                                            case 1:
//                                                linkDataBean.setContent(problemLinkLine.getOptionB());
//
//                                                break;
//                                            case 2:
//                                                linkDataBean.setContent(problemLinkLine.getOptionC());
//
//                                                break;
//                                            case 3:
//                                                linkDataBean.setContent(problemLinkLine.getOptionD());
//
//                                                break;
//                                        }
//                                        linkDataBean.setQ_num(Integer.parseInt(qNum[i]));
//                                        linkDataBean.setRow(i + 1);
//                                        linkDataBean.setCol(0);
//                                        linkDataBeans.add(linkDataBean);
//                                    }
//
//                                    for (int i = 0; i < 4; i++) {
//                                        LinkDataBean linkDataBean = new LinkDataBean();
//                                        switch (i){
//                                            case 0:
//                                                linkDataBean.setContent(problemLinkLine.getOptionAdes());
//                                                break;
//                                            case 1:
//                                                linkDataBean.setContent(problemLinkLine.getOptionBdes());
//
//                                                break;
//                                            case 2:
//                                                linkDataBean.setContent(problemLinkLine.getOptionCdes());
//
//                                                break;
//                                            case 3:
//                                                linkDataBean.setContent(problemLinkLine.getOptionDdes());
//
//                                                break;
//                                        }
//                                        linkDataBean.setQ_num(Integer.parseInt(qNum[i + 4]));
//                                        linkDataBean.setRow(i + 1);
//                                        linkDataBean.setCol(1);
//                                        linkDataBeans.add(linkDataBean);
//                                    }
//                                    LogUtils.d("zhuanhuan后:",problemLinkLine.toString());
//                                    Message message2 = new Message();
//                                    message2.arg1 = 2;
//                                    message2.obj = linkDataBeans;
//                                    handler.sendMessage(message2);
//                                    break;
//                                case 3:
//                                    llTypePai.setVisibility(View.VISIBLE);
//                                    problemgetOrder = new ProblemgetOrder();
//                                    problemgetOrder = (ProblemgetOrder) problem;
//                                    Message message1 = new Message();
//                                    message1.arg1 = 3;
//                                    handler.sendMessage(message1);
//                                    break;
//                            }
//                            animationin.setDuration(600);
//                            reContainer.startAnimation(animationin);
//                            return;
//                        } else {
//                            LogUtils.d("获取题目");
//                            LogUtils.d(cType + "aaa");
//                            //获取收藏的题目
//                        }
//                        break;
//                }
//                break;
//
//            case RIGHT://shang一道题目
//                cIndex -= 1;
//                switch (before) {
//                    case "types":
//                        if (cIndex < 0) {
////                    提示不能滑动
//                            Toast.makeText(getApplicationContext(), "不能再滑啦", Toast.LENGTH_SHORT).show();
//                            return;
//                        } else {
////                    显示
//                            Problem problem = myProblems.get(cIndex);
//                            getBeforeOrNextProblem(problem);
//                            animationout.setDuration(600);
//                            reContainer.startAnimation(animationout);
//                        }
//                        break;
//                    case "info":
//                        if (cIndex < 0) {
////                    提示不能滑动
//                            Toast.makeText(getApplicationContext(), "不能再滑啦", Toast.LENGTH_SHORT).show();
//                            return;
//                        } else {
////                    显示
//                            llTypeLian.setVisibility(View.INVISIBLE);
//                            llTypePai.setVisibility(View.INVISIBLE);
//                            llTypeXuan.setVisibility(View.INVISIBLE);
//                            Problem problem = Constant.userProblems.get(cIndex);
//                            switch (problem.getProblemType()){
//                                case 1:
//                                    llTypeXuan.setVisibility(View.VISIBLE);
//                                    problemSelect = (ProblemSelect) problem;
//                                    Message message = new Message();
//                                    message.arg1 = 1;
//                                    handler.sendMessage(message);
//                                    break;
//                                case 2:
//                                    llTypeLian.setVisibility(View.VISIBLE);
//                                    problemLinkLine = (ProblemLinkLine) problem;
//                                    String problemKey = problemLinkLine.getProblemKey();
//                                    String[] qNum = problemKey.split(Constant.DELIMITER);
//                                    List<LinkDataBean> linkDataBeans = new ArrayList<>();
//                                    for (int i = 0; i < 4; i++) {
//                                        LinkDataBean linkDataBean = new LinkDataBean();
//                                        switch (i){
//                                            case 0:
//                                                linkDataBean.setContent(problemLinkLine.getOptionA());
//                                                break;
//                                            case 1:
//                                                linkDataBean.setContent(problemLinkLine.getOptionB());
//
//                                                break;
//                                            case 2:
//                                                linkDataBean.setContent(problemLinkLine.getOptionC());
//
//                                                break;
//                                            case 3:
//                                                linkDataBean.setContent(problemLinkLine.getOptionD());
//
//                                                break;
//                                        }
//                                        linkDataBean.setQ_num(Integer.parseInt(qNum[i]));
//                                        linkDataBean.setRow(i + 1);
//                                        linkDataBean.setCol(0);
//                                        linkDataBeans.add(linkDataBean);
//                                    }
//
//                                    for (int i = 0; i < 4; i++) {
//                                        LinkDataBean linkDataBean = new LinkDataBean();
//                                        switch (i){
//                                            case 0:
//                                                linkDataBean.setContent(problemLinkLine.getOptionAdes());
//                                                break;
//                                            case 1:
//                                                linkDataBean.setContent(problemLinkLine.getOptionBdes());
//
//                                                break;
//                                            case 2:
//                                                linkDataBean.setContent(problemLinkLine.getOptionCdes());
//
//                                                break;
//                                            case 3:
//                                                linkDataBean.setContent(problemLinkLine.getOptionDdes());
//                                                break;
//                                        }
//                                        linkDataBean.setQ_num(Integer.parseInt(qNum[i + 4]));
//                                        linkDataBean.setRow(i + 1);
//                                        linkDataBean.setCol(1);
//                                        linkDataBeans.add(linkDataBean);
//                                    }
//                                    Message message2 = new Message();
//                                    message2.arg1 = 2;
//                                    message2.obj = linkDataBeans;
//                                    handler.sendMessage(message2);
//                                    break;
//                                case 3:
//                                    llTypePai.setVisibility(View.VISIBLE);
//                                    problemgetOrder = (ProblemgetOrder) problem;
//                                    Message message1 = new Message();
//                                    message1.arg1 = 3;
//                                    handler.sendMessage(message1);
//                                    break;
//                            }
//                            getBeforeOrNextProblem(problem);
//                            animationout.setDuration(600);
//                            reContainer.startAnimation(animationout);
//                        }
//                        break;
//                }
//                break;
//        }
//    }


}

