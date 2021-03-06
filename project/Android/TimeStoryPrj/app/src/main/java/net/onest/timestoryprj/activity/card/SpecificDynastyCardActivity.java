package net.onest.timestoryprj.activity.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.onest.timestoryprj.R;
import net.onest.timestoryprj.adapter.card.DropdownListAdapter;
import net.onest.timestoryprj.adapter.card.SpecificDynastyCardAdapter;
import net.onest.timestoryprj.constant.Constant;
import net.onest.timestoryprj.constant.ServiceConfig;
import net.onest.timestoryprj.customview.DropdownListView;
import net.onest.timestoryprj.dialog.LoadingDialog;
import net.onest.timestoryprj.entity.card.UserCard;
import net.onest.timestoryprj.util.ToastUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpecificDynastyCardActivity extends AppCompatActivity {
    @BindView(R.id.dynasty_cards)
    GridView dyanstyCardView;
    private List<UserCard> userCards = new ArrayList<>();
    private SpecificDynastyCardAdapter cardAdapter;
    private int dynastyId;
    @BindView(R.id.card_types)
    DropdownListView typeView;
    private ArrayList<String> types;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.search_card_name)
    EditText searchCardName;
    @BindView(R.id.search_delete)
    ImageView searchDelete;
    @BindView(R.id.search_btn)
    TextView searchBtn;
    @BindView(R.id.not_exist)
    TextView notExistCards;
    private OkHttpClient client;
    private Gson gson;
    int type = 0;
    private long clickMillis = 0;
    private long clickTwiceMillis;
    private PromptDialog promptDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    Type type = new TypeToken<ArrayList<UserCard>>() {
                    }.getType();
                    userCards = gson.fromJson(result, type);
                    initDatas();
                    break;
                case 2:
                    userCards.clear();
                    String result2 = (String) msg.obj;
                    Type type2 = new TypeToken<ArrayList<UserCard>>() {
                    }.getType();
                    userCards = gson.fromJson(result2, type2);
                    notifyDataChange();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_dynasty_card);
        ButterKnife.bind(this);
        promptDialog = new PromptDialog(this);
        promptDialog.getDefaultBuilder().touchAble(false).round(3).loadingDuration(1000);
        promptDialog.showLoading("正在加载");
        client = new OkHttpClient();
        gson = new GsonBuilder()//创建GsonBuilder对象
                .serializeNulls()//允许输出Null值属性
                .create();//创建Gson对象
        initDynastyCards();
        initTypes();
        searchCardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!"".equals(searchCardName.getText().toString())) {
                    searchDelete.setVisibility(View.VISIBLE);
                } else {
                    searchDelete.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!"".equals(searchCardName.getText().toString())) {
                    searchDelete.setVisibility(View.VISIBLE);
                } else {
                    searchDelete.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initTypes() {
        if (types == null) {
            types = new ArrayList<>();
            types.add("全部卡片");
            types.add("人物卡片");
            types.add("文物卡片");
            typeView.setDatas(types);
        } else {
            typeView.setDatas(types);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchCardName.setText("");
        typeView.textView.setText(types.get(0));
        type = 0;
        getCardsByType(type);
    }

    private void initDynastyCards() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("dynastyId");
        dynastyId = Integer.parseInt(id);
        getCardsByType(type);
    }

    private void initDatas() {
        clickMillis = System.currentTimeMillis();
        if (userCards.size() == 0) {
            ToastUtil.showSickToast(getApplicationContext(), "没有此朝代的卡片，请重新选择朝代吧", 1500);
        }
        cardAdapter = new SpecificDynastyCardAdapter(getApplicationContext(), userCards);
        dyanstyCardView.setAdapter(cardAdapter);
        dyanstyCardView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                clickTwiceMillis = System.currentTimeMillis();
                if ((clickTwiceMillis - clickMillis) > 1000) {
                    Intent intent = new Intent(getApplicationContext(), SpectficCardDetailActivity.class);
                    intent.putExtra("cardId", userCards.get(position).getCardListVO().getCardId());
                    startActivity(intent);
                }
                clickMillis = clickTwiceMillis;
            }
        });
        promptDialog.dismissImmediately();
    }

    private void notifyDataChange() {
        clickMillis = System.currentTimeMillis();
        if (userCards.size() == 0) {
            ToastUtil.showSickToast(getApplicationContext(), "没有相关卡片，请重新选择吧", 1500);
        }
        cardAdapter = new SpecificDynastyCardAdapter(getApplicationContext(), userCards);
        dyanstyCardView.setAdapter(cardAdapter);
        dyanstyCardView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                clickTwiceMillis = System.currentTimeMillis();
                if ((clickTwiceMillis - clickMillis) > 1000) {
                    Intent intent = new Intent(getApplicationContext(), SpectficCardDetailActivity.class);
                    intent.putExtra("cardId", userCards.get(position).getCardListVO().getCardId());
                    startActivity(intent);
                }
                clickMillis = clickTwiceMillis;
            }
        });
    }

    @OnClick(R.id.back)
    void backToLastPage() {
        finish();
    }

    @OnClick(R.id.search_delete)
    void clearSearchContent() {
        searchCardName.setText("");
        searchDelete.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.card_types)
    void showType() {
        if (typeView.popupWindow == null) {
            showPopWindow();
        } else {
            closePopWindow();
        }
    }

    @OnClick(R.id.search_btn)
    void showSearchCards() {
        String key = searchCardName.getText().toString().trim();
        if ("".equals(key)) {
            ToastUtil.showEncourageToast(getApplicationContext(), "要输入关键字才可以搜索哦", 1500);
        } else {
            getCardsByKey(key);
        }
    }

    @OnClick(R.id.not_exist)
    void showNotExistCards() {
        Intent intent = new Intent(getApplicationContext(), NotExistUserCardActivity.class);
        intent.putExtra("dynastyId", dynastyId);
        startActivity(intent);
    }

    private void getCardsByKey(String key) {
        new Thread() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(ServiceConfig.SERVICE_ROOT + "/usercard/list/" + Constant.User.getUserId() + "/" + +dynastyId + "/" + type + "/" + key)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // TODO获取卡片内容失败
                        ToastUtil.showCryToast(getApplicationContext(), "获取朝代卡片失败了", 1500);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Message message = new Message();
                        message.what = 2;
                        message.obj = result;
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }

    private void showPopWindow() {
        // 加载popupWindow的布局文件
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) typeView.getContext().getSystemService(infServie);
        View contentView = layoutInflater.inflate(R.layout.dropdown_list_popupwindow, null, false);
        typeView.listView = contentView.findViewById(R.id.list_view);
        typeView.dropdownListAdapter = new DropdownListAdapter(typeView.getContext(), types, R.layout.dropdown_list_item);
        typeView.listView.setAdapter(typeView.dropdownListAdapter);
        typeView.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String text = typeView.cardTypes.get(position);
                typeView.textView.setText(text);
                typeView.popupWindow.dismiss();
                typeView.popupWindow = null;
                if ("全部卡片".equals(typeView.textView.getText().toString().trim())) {
                    userCards.clear();
                    type = 0;
                    String key = searchCardName.getText().toString().trim();
                    if ("".equals(key)) {
                        getCardsByType(type);
                    } else {
                        getCardsByKey(key);
                    }
                } else if ("人物卡片".equals(typeView.textView.getText().toString().trim())) {
                    userCards.clear();
                    type = 1;
                    String key = searchCardName.getText().toString().trim();
                    if ("".equals(key)) {
                        getCardsByType(type);
                    } else {
                        getCardsByKey(key);
                    }
                } else if ("文物卡片".equals(typeView.textView.getText().toString().trim())) {
                    userCards.clear();
                    type = 2;
                    String key = searchCardName.getText().toString().trim();
                    if ("".equals(key)) {
                        getCardsByType(type);
                    } else {
                        getCardsByKey(key);
                    }
                }
            }
        });
        ColorDrawable dw = new ColorDrawable(getResources().getColor(R.color.colorDropdown));
        typeView.popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        typeView.popupWindow.setBackgroundDrawable(dw);
        typeView.popupWindow.setOutsideTouchable(true);
        typeView.popupWindow.showAsDropDown(typeView);
    }

    private void getCardsByType(int type) {
        new Thread() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(ServiceConfig.SERVICE_ROOT + "/usercard/list/" + Constant.User.getUserId() + "/" + dynastyId + "/" + type)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 获取失败
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Message message = new Message();
                        if (cardAdapter == null) {
                            message.what = 1;
                        } else {
                            message.what = 2;
                        }
                        message.obj = result;
                        handler.sendMessage(message);
                    }
                });
            }
        }.start();
    }

    /**
     * 关闭下拉列表弹窗
     */
    private void closePopWindow() {
        typeView.popupWindow.dismiss();
        typeView.popupWindow = null;
    }
}
