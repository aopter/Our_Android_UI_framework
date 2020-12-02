package net.onest.timestoryprj.activity.user;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import net.onest.timestoryprj.R;
import net.onest.timestoryprj.constant.Constant;
import net.onest.timestoryprj.constant.ServiceConfig;
import net.onest.timestoryprj.entity.UserDetails;
import net.onest.timestoryprj.util.AudioUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SettingActivity extends AppCompatActivity {

    private Button btnPerson;
    private Button btnVoice;
    private Button btnRule;
    private Button btnProblem;
    private Button btnExitLogin;
    private LinearLayout rightLayout;
    private ImageView ivVoice;
    private ImageView ivHeader;
    private AudioUtil audioUtil;
    private Gson gson;
    private String userInfo;
    private String picturePath = "";//相册路径
    private Bitmap bitmapHeader;//从相册选择的图片
    private File file;
    private PromptDialog promptDialog;
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    private Bitmap bitmap;//从相册选择的图片
    private Handler handler = new Handler() {

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //创建对象
        promptDialog = new PromptDialog(this);
        //设置自定义属性
        promptDialog.getDefaultBuilder().touchAble(true).round(3).loadingDuration(3000);

        findViews();
        setListener();
        gson = new Gson();

        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(ServiceConfig.SERVICE_ROOT + "/rule");
                    URLConnection connection = url.openConnection();
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in, "utf-8"));
                    String rule = reader.readLine();
                    Log.e("规则", rule);
                    Constant.Rule.setRuleInfo(rule);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void setListener() {
        MyListener myListener = new MyListener();
        btnPerson.setOnClickListener(myListener);
        btnVoice.setOnClickListener(myListener);
        btnRule.setOnClickListener(myListener);
        btnProblem.setOnClickListener(myListener);
        btnExitLogin.setOnClickListener(myListener);
    }

    private void findViews() {
        btnPerson = findViewById(R.id.btn_person);
        btnVoice = findViewById(R.id.btn_voice_set);
        btnRule = findViewById(R.id.btn_rule);
        btnProblem = findViewById(R.id.btn_problem);
        btnExitLogin = findViewById(R.id.btn_exit_login);
        rightLayout = findViewById(R.id.rightlinear);
        audioUtil = AudioUtil.getInstance(getApplicationContext());
    }

    class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_person://个人资料
                    rightLayout.removeAllViews();
                    setPersonAttr();
                    break;
                case R.id.btn_voice_set://音量设置
                    rightLayout.removeAllViews();
                    setVoiceAttr();
                    break;
                case R.id.btn_rule://查看规则
                    rightLayout.removeAllViews();
                    setRuleAttr();
                    break;
                case R.id.btn_problem://反馈问题
                    rightLayout.removeAllViews();
                    setProblemAttr();
                    break;
                case R.id.btn_exit_login://退出登录
                    rightLayout.removeAllViews();
                    setExitLoginAttr();
                    break;
            }
        }
    }

    /**
     * 退出登录布局
     */
    private void setExitLoginAttr() {
        promptDialog.showWarnAlert("您确定退出登录吗？", new PromptButton("取消", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton button) {
                promptDialog.dismiss();
            }
        }), new PromptButton("确定", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton button) {
                Constant.User = null;
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }));

    }

    /**
     * 反馈问题布局
     */
    private void setProblemAttr() {
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(params);
        //问题编辑框
        EditText etProblem = new EditText(getApplicationContext());
        LinearLayout.LayoutParams etParam = new LinearLayout.LayoutParams(900, 500);
        etParam.topMargin = 150;
        etParam.gravity = Gravity.CENTER_HORIZONTAL;
        etProblem.setText("说说你的问题吧……");
        etProblem.setTextSize(20);
        etProblem.setBackgroundResource(R.drawable.edit_style);
        etProblem.setLayoutParams(etParam);
        linearLayout.addView(etProblem);
        //提交
        Button btnSub = new Button(getApplicationContext());
        LinearLayout.LayoutParams btnParam = new LinearLayout.LayoutParams(280, 160);
        btnSub.setText("提交");
        btnSub.setTextSize(20);
        btnSub.setBackgroundResource(R.color.ourDynastyRed);
        btnParam.gravity = Gravity.RIGHT;
        btnParam.rightMargin = 30;
        btnParam.topMargin = 120;
        btnSub.setLayoutParams(btnParam);
        linearLayout.addView(btnSub);
        rightLayout.addView(linearLayout);
        //提交按钮的监听器
        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etProblem.getText().toString().trim();
                if (null != content && !content.equals("")) {
                    Toast.makeText(getApplicationContext(), "您的问题已反馈成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "请填写您的问题", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 音量设置布局
     */
    private void setVoiceAttr() {
        //音量大小
        TextView tvTitle = new TextView(getApplicationContext());
        LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParam.topMargin = 100;
        tvParam.leftMargin = 50;
        tvTitle.setText("音量大小");
        tvTitle.setTextSize(25);
        tvTitle.setLayoutParams(tvParam);
        rightLayout.addView(tvTitle);
        //声音
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.topMargin = 50;
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(params);
        ivVoice = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams ivParam = new LinearLayout.LayoutParams(150, 150);
        ivParam.leftMargin = 20;
        ivVoice.setBackgroundResource(R.mipmap.voice);
        ivVoice.setLayoutParams(ivParam);
        linearLayout.addView(ivVoice);
        rightLayout.addView(linearLayout);

        SeekBar seekBar = new SeekBar(getApplicationContext());
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        par.topMargin = 40;
        seekBar.setLayoutParams(par);
        seekBar.setMax(100);
        int mediaMax = audioUtil.getMediaMaxVolume();//最大音量
        int media = audioUtil.getMediaVolume();//当前音量
        DecimalFormat df = new DecimalFormat("0.00");
        String stringVolume = df.format((float) media / mediaMax);
        double volume = Double.parseDouble(stringVolume);
        int progress = (int) (volume * 100);
        if (progress == 0) {
            ivVoice.setBackgroundResource(R.mipmap.novoice);
        }
        seekBar.setProgress(progress);
        linearLayout.addView(seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e("进度", seekBar.getProgress() + "");
                int progress = seekBar.getProgress();
                int mediaMax = audioUtil.getMediaMaxVolume();
                Log.e("多媒体最大", mediaMax + "");
                int media = audioUtil.getMediaVolume();
                Log.e("多媒体当前", media + "");
                DecimalFormat df = new DecimalFormat("0.00");
                String stringVolume = df.format((float) progress / 100 * 15);
                double volume = Double.parseDouble(stringVolume);
                Log.e("double volume", volume + "");
                int volume1 = 0;
                if (volume > 1) {
                    volume1 = (int) Math.round(volume);
                    ivVoice.setBackgroundResource(R.mipmap.voice);
                } else if (volume > 0) {
                    volume1 = (int) Math.ceil(volume);
                    ivVoice.setBackgroundResource(R.mipmap.voice);
                } else {
                    volume1 = 0;
                    ivVoice.setBackgroundResource(R.mipmap.novoice);
                }
                Log.e("int volume", volume1 + "");
                audioUtil.setMediaVolume(volume1);
            }
        });

    }

    /**
     * 查看规则布局
     */
    private void setRuleAttr() {
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(params);

        TextView tvTitle = new TextView(getApplicationContext());
        LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParam.leftMargin = 50;
        tvParam.topMargin = 60;
        tvTitle.setText("时光序平台积分规则:");
        tvTitle.setTextSize(25);
        tvTitle.setLayoutParams(tvParam);
        linearLayout.addView(tvTitle);

        TextView tvRule = new TextView(getApplicationContext());
        tvRule.setText(Constant.Rule.getRuleInfo());
        tvRule.setTextSize(20);
        LinearLayout.LayoutParams ruleParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ruleParam.leftMargin = 100;
        ruleParam.topMargin = 50;
        tvRule.setLayoutParams(ruleParam);
        linearLayout.addView(tvRule);
        rightLayout.addView(linearLayout);

    }

    /**
     * 设置个人资料的布局
     */
    private void setPersonAttr() {
        LinearLayout linearLayout1 = new LinearLayout(getApplicationContext());
        //width,height
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout1.setLayoutParams(params);
        //子TextView的属性
        LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(350, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParam.leftMargin = 30;
        tvParam.topMargin = 30;
        LinearLayout.LayoutParams etParam = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT);
        etParam.leftMargin = 20;
        etParam.topMargin = 30;
        TextView tvPhoto = new TextView(getApplicationContext());
        tvPhoto.setText("头像:");
        tvPhoto.setTextSize(20);
        tvPhoto.setLayoutParams(tvParam);
        linearLayout1.addView(tvPhoto);
        //头像
        ivHeader = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams ivParam = new LinearLayout.LayoutParams(200, 200);
        ivParam.leftMargin = 20;
        ivParam.topMargin = 20;
        Glide.with(this)
                .load(R.mipmap.man)
                .circleCrop()
                .into(ivHeader);
        ivHeader.setLayoutParams(ivParam);
        linearLayout1.addView(ivHeader);
        rightLayout.addView(linearLayout1);
        //昵称
        LinearLayout linearLayout2 = new LinearLayout(getApplicationContext());
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout2.setLayoutParams(params);
        TextView tvNiName = new TextView(getApplicationContext());
        tvNiName.setText("昵称：");
        tvNiName.setTextSize(20);
        tvNiName.setLayoutParams(tvParam);
        linearLayout2.addView(tvNiName);
        EditText etNiName = new EditText(getApplicationContext());
        etNiName.setText("小美");
        etNiName.setTextSize(20);
        etNiName.setLayoutParams(etParam);
        linearLayout2.addView(etNiName);
        rightLayout.addView(linearLayout2);
        //个性签名
        LinearLayout linearLayout3 = new LinearLayout(getApplicationContext());
        linearLayout3.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout3.setLayoutParams(params);
        TextView tvSignature = new TextView(getApplicationContext());
        tvSignature.setText("个性签名：");
        tvSignature.setTextSize(20);
        tvSignature.setLayoutParams(tvParam);
        linearLayout3.addView(tvSignature);
        EditText etSignature = new EditText(getApplicationContext());

        etSignature.setText("第五美女");
        etSignature.setTextSize(20);
        etSignature.setLayoutParams(etParam);
        linearLayout3.addView(etSignature);
        rightLayout.addView(linearLayout3);
        //性别
        LinearLayout linearLayout4 = new LinearLayout(getApplicationContext());
        linearLayout4.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout4.setLayoutParams(params);
        TextView tvSex = new TextView(getApplicationContext());
        tvSex.setText("性别：");
        tvSex.setTextSize(20);
        tvSex.setLayoutParams(tvParam);
        linearLayout4.addView(tvSex);
        EditText etSex = new EditText(getApplicationContext());
        etSex.setText("女");
        etSex.setTextSize(20);
        etSex.setLayoutParams(etParam);
        linearLayout4.addView(etSex);
        rightLayout.addView(linearLayout4);
        //手机号
        LinearLayout linearLayout5 = new LinearLayout(getApplicationContext());
        linearLayout5.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout5.setLayoutParams(params);
        TextView tvPhone = new TextView(getApplicationContext());
        tvPhone.setText("手机号：");
        tvPhone.setTextSize(20);
        tvPhone.setLayoutParams(tvParam);
        linearLayout5.addView(tvPhone);
        EditText etPhone = new EditText(getApplicationContext());
        etPhone.setText("123456");
        etPhone.setTextSize(20);
        etPhone.setLayoutParams(etParam);
        linearLayout5.addView(etPhone);
        rightLayout.addView(linearLayout5);

        //提交
        Button btnSub = new Button(getApplicationContext());
        btnSub.setText("保存");
        btnSub.setTextSize(20);
        btnSub.setBackgroundResource(R.color.ourDynastyRed);
        LinearLayout.LayoutParams btnParam = new LinearLayout.LayoutParams(280, 130);
        btnParam.gravity = Gravity.RIGHT;
        btnParam.topMargin = 20;
        btnParam.rightMargin = 20;
        btnSub.setLayoutParams(btnParam);
        rightLayout.addView(btnSub);

        //修改图片
        ivHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PromptButton cancle = new PromptButton("取消", null);
                cancle.setTextColor(Color.parseColor("#0076ff"));
                promptDialog.showAlertSheet("", true, cancle, new PromptButton("从相册选择", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent, 1);
                    }
                }), new PromptButton("拍照", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"temp.jpg")));
                        startActivityForResult(intent, 2);

                    }
                }));

            }
        });

        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String niName = etNiName.getText().toString().trim();
                String signature = etSignature.getText().toString().trim();
                String sex = etSex.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                Log.e("提交", niName + signature + sex + phone);
                //判断非空
                if (null != niName && null != signature && null != sex && null != phone) {
//                    int userId = Constant.User.getUserId();
                    UserDetails userDetails = new UserDetails();
                    userDetails.setUserId(1);
                    userDetails.setUserNickname(niName);
                    userDetails.setUserNumber(phone);
                    userDetails.setUserSex(sex);
                    userDetails.setUserSignature(signature);
                    userInfo = gson.toJson(userDetails);
                    Log.e("userInfo", userInfo);
                    //用户详情传给服务器
//                    upToServer();
                    //上传头像
                    upHeaderToServer();

                } else {
                    Toast.makeText(getApplicationContext(), "请您完善用户信息后提交", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    /**
     * 创建保存图片的文件
     *
     * @return
     */
    private File createImageFile() {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        return tempFile;
    }
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes

    /**
     * 上传头像
     */
    private void upHeaderToServer() {
        Log.e("文件名称", file.getName());
        Log.e("文件路径", file.getAbsolutePath());
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);//通过表单上传
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);//上传的文件以及类型
        requestBody.addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("userId", 1 + "");
        Request request = new Request.Builder()
                .url(ServiceConfig.SERVICE_ROOT + "/picture/upload")
                .post(requestBody.build())
                .build();

        client.newBuilder().build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("no", "失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("ok", "成功");
            }
        });
    }


    /**
     * 向服务器上传用户更改后的信息
     */
    private void upToServer() {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(ServiceConfig.SERVICE_ROOT + "/userdetails/modify");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(userInfo.getBytes());
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    String info = reader.readLine();
                    JSONObject object = new JSONObject(info);
                    boolean flag = object.getBoolean("result");
                    if (flag) {
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            picturePath = loadImagePath(data);
            Glide.with(getApplicationContext())
                    .load(picturePath)
                    .circleCrop()
                    .into(ivHeader);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
<<<<<<< Updated upstream
            bitmapHeader = BitmapFactory.decodeFile(picturePath,options);

=======
            bitmapHeader = BitmapFactory.decodeFile(picturePath, options);
>>>>>>> Stashed changes
            convertBitmapToFile(bitmapHeader);
        } else if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            File picture = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
            Bundle extras = data.getExtras();
<<<<<<< Updated upstream
            if (extras != null){
                    Bitmap photo = extras.getParcelable("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG,75,stream);
                    Glide.with(getApplicationContext())
                            .load(photo)
                            .circleCrop()
                            .into(ivHeader);
                    convertBitmapToFile(photo);
            }
        }
    }

    private File convertBitmapToFile(Bitmap bitmap) {
        try {
            file = new File(SettingActivity.this.getCacheDir(),"userHeader");
=======
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
//                ivHeader.setImageBitmap(photo);
                Glide.with(getApplicationContext())
                        .load(photo)
                        .circleCrop()
                        .into(ivHeader);
                convertBitmapToFile(photo);
            }

//            ivHeader.setImageBitmap(bitmap);
            convertBitmapToFile(bitmap);

        }
    }


    private File convertBitmapToFile(Bitmap bitmap) {
        try {
            file = new File(SettingActivity.this.getCacheDir(), "userHeader");
            file = new File(SettingActivity.this.getCacheDir(), "portrait");
>>>>>>> Stashed changes
            file.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String loadImagePath(Intent data) {
        //获取返回的数据，这里是android自定义的uri地址
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Log.e("filePathColumn的值", filePathColumn[0]);
        //获取选择照片的数据视图
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        //从数据视图中获取已选择图片的路径
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        Log.e("columnIndex的值", columnIndex + "");
        String path = cursor.getString(columnIndex);
        Log.e("path的值", path);
        cursor.close();
        return path;
    }
}
