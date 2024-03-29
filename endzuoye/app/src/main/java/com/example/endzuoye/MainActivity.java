package com.example.endzuoye;
//登录页面


import android.content.DialogInterface;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.endzuoye.bean.UserInfo;
import com.example.endzuoye.database.UserDBHelper;
import com.example.endzuoye.util.DateUtil;
import com.example.endzuoye.util.ViewUtil;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , OnCheckedChangeListener{

    private RadioGroup rg_login; // 声明一个单选组对象
    private RadioButton rb_password; // 声明一个单选按钮对象
    private RadioButton rb_verifycode; // 声明一个单选按钮对象
    private EditText et_phone; // 声明一个编辑框对象
    private TextView tv_password; // 声明一个文本视图对象
    private EditText et_password; // 声明一个编辑框对象
    private Button btn_forget; // 声明一个忘记密码按钮控件对象
    private Switch sw_ios; // 声明一个开关按钮对象
    private TextView tv_ios_result; // 声明一个文本视图对象
    private Button btn_login; // 声明一个登录按钮控件对象
    private Button btn_logon;//声明一个注册按钮控件对象

    private int mRequestCode = 0; // 跳转页面时的请求代码
    private int mType = 2; // 用户类型
    private boolean bRemember = false; // 是否记住密码
    private String mPassword = "111111"; // 默认密码
    private String mVerifyCode; // 验证码

    private SharedPreferences mShared; // 声明一个共享参数对象
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象

    // 初始化用户类型的下拉框


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rg_login = findViewById(R.id.rg_login);
        rb_password = findViewById(R.id.rb_password);
        rb_verifycode = findViewById(R.id.rb_verifycode);
        et_phone = findViewById(R.id.et_phone);
        tv_password = findViewById(R.id.tv_password);
        et_password = findViewById(R.id.et_password);
        btn_forget = findViewById(R.id.btn_forget);
        btn_login = findViewById(R.id.btn_login);
        btn_logon = findViewById(R.id.btn_logon);
        sw_ios = findViewById(R.id.sw_ios);
        tv_ios_result = findViewById(R.id.tv_ios_result);


        // 给rg_login设置单选监听器
        rg_login.setOnCheckedChangeListener(new RadioListener());
        // 给et_phone添加文本变更监听器
        et_phone.addTextChangedListener(new HideTextWatcher(et_phone));
        // 给et_password添加文本变更监听器
        et_password.addTextChangedListener(new HideTextWatcher(et_password));
        //忘记密码监听
        btn_forget.setOnClickListener(this);
        //登录
        btn_login.setOnClickListener(this);
        //注册
        btn_logon.setOnClickListener(this);

        // 给开关按钮设置选择监听器，一旦用户点击它，就触发监听器的onCheckedChanged方法
        sw_ios.setOnCheckedChangeListener(this);
        initTypeSpinner();

        //从share_login.xml中获取共享参数对象

        mShared = getSharedPreferences("share_login", MODE_PRIVATE);
        // 获取共享参数中保存的手机号码
        String phone = mShared.getString("phone", "");
        // 获取共享参数中保存的密码
        String password = mShared.getString("password", "");
        et_phone.setText(phone); // 给手机号码编辑框填写上次保存的手机号
        et_password.setText(password); // 给密码编辑框填写上次保存的密码
    }

    @Override
    public void onClick(View v) {
        String phone = et_phone.getText().toString();
        if (v.getId() == R.id.btn_forget) { // 点击了“忘记密码”按钮
            if (phone.length() < 11) { // 手机号码不足11位
                Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rb_password.isChecked()) { // 选择了密码方式校验，此时要跳到找回密码页面
                Intent intent = new Intent(this, LoginForgetActivity.class);
                // 携带手机号码跳转到找回密码页面
                intent.putExtra("phone", phone);
                startActivityForResult(intent, mRequestCode);
            } else if (rb_verifycode.isChecked()) { // 选择了验证码方式校验，此时要生成六位随机数字验证码
                // 生成六位随机数字的验证码,结果用0填充
                mVerifyCode = String.format("%06d", (int) ((Math.random() * 9 + 1) * 100000));
                // 弹出提醒对话框，提示用户六位验证码数字
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请记住验证码");
                builder.setMessage("手机号" + phone + "，本次验证码是" + mVerifyCode + "，请输入验证码");
                builder.setPositiveButton("好的", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        } else if (v.getId() == R.id.btn_login) { // 点击了“登录”按钮
            if (phone.length() < 11) { // 手机号码不足11位
                Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rb_password.isChecked()) { // 密码方式校验
                // 根据手机号码到数据库中查询用户记录
                UserInfo info = mHelper.queryByPhone(et_phone.getText().toString());
                if (info != null) {
                    // 输入的密码和数据库储存的比较
                    if (!et_password.getText().toString().equals(info.pwd)) {
                        Toast.makeText(this, "请输入正确的密码", Toast.LENGTH_SHORT).show();
                    } else { // 密码校验通过
                        loginSuccess(); // 提示用户登录成功
                    }
                } else {
                    //输入的密码跟mPassword比较
                    if (!et_password.getText().toString().equals(mPassword)) {
                        Toast.makeText(this, "请输入正确的密码", Toast.LENGTH_SHORT).show();
                    } else { // 密码校验通过
                        loginSuccess(); // 提示用户登录成功
                    }
                }
            } else if (rb_verifycode.isChecked()) { // 验证码方式校验
                if (!et_password.getText().toString().equals(mVerifyCode)) {
                    Toast.makeText(this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
                } else { // 验证码校验通过
                    loginSuccess(); // 提示用户登录成功
                }
            }
        }else if(v.getId() == R.id.btn_logon){//如果点击注册按钮
            Intent intent = new Intent(this, Logon.class);
            startActivityForResult(intent, mRequestCode);
        }
    }

    // 从修改密码页面返回登录页面，要清空密码的输入框
    @Override
    protected void onRestart() {
        et_password.setText("");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获得用户数据库帮助器的一个实例
        mHelper = UserDBHelper.getInstance(this, 2);
        // 恢复页面，则打开数据库连接
        mHelper.openWriteLink();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停页面，则关闭数据库连接
        mHelper.closeLink();
    }

    // 忘记密码修改后，从后一个页面携带参数返回当前页面时触发
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mRequestCode && data != null) {
            // 用户密码已改为新密码，故更新密码变量
            mPassword = data.getStringExtra("new_password");
        }
    }

    // 校验通过，登录成功
    private void loginSuccess() {
        // 如果勾选了“记住密码”
        if (bRemember) {
            //把手机号码和密码都保存到共享参数中
            SharedPreferences.Editor editor = mShared.edit(); // 获得编辑器的对象
            editor.putString("phone", et_phone.getText().toString()); // 添加名叫phone的手机号码
            editor.putString("password", et_password.getText().toString()); // 添加名叫password的密码
            editor.commit(); // 提交编辑器中的修改
        }

        Intent intent = new Intent(this, ShoppingChannelActivity.class);
        startActivity(intent);

        /*
        String desc = String.format("您的手机号码是%s，类型是%s。恭喜你通过登录验证，点击“确定”按钮返回上个页面",
                et_phone.getText().toString(), typeArray[mType]);
        // 弹出提醒对话框，提示用户登录成功
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("登录成功");
        builder.setMessage(desc);
        builder.setPositiveButton("确定返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("我再看看", null);
        AlertDialog alert = builder.create();
        alert.show();
        */
    }

    private String[] typeArray = {"个人用户", "公司用户","徐汇江18660014"};
    private void initTypeSpinner() {
        // 声明一个下拉列表的数组适配器
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
                R.layout.item_select, typeArray);
        // 设置数组适配器的布局样式
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        // 从布局文件中获取名叫sp_type的下拉框
        Spinner sp_type = findViewById(R.id.sp_type);
        // 设置下拉框的标题
        sp_type.setPrompt("请选择用户类型");
        // 设置下拉框的数组适配器
        sp_type.setAdapter(typeAdapter);
        // 设置下拉框默认显示第几项
        sp_type.setSelection(mType);
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        sp_type.setOnItemSelectedListener(new TypeSelectedListener());
    }


    @Override//开关
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        refreshResult(buttonView);
    }

    // 刷新Switch按钮的开关状态说明
    private void refreshResult(CompoundButton buttonView) {                            //真     假
        String result = String.format("Switch按钮的状态是%s", (buttonView.isChecked()) ? "开" : "关");

        if (buttonView.getId() == R.id.sw_ios) {

            if(buttonView.isChecked()){
                tv_ios_result.setText("已记住密码");
                bRemember=true;
            }else{
                tv_ios_result.setText("xzcxzcxzcxzc");
                bRemember=false;
            }
        }
    }

    // 定义用户类型的选择监听器
    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mType = arg2;
        }
        // 未选择时的处理方法，通常无需关注
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // 定义登录方式的单选监听器
    private class RadioListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_password) { // 选择了密码登录
                tv_password.setText("登录密码：");
                et_password.setHint("请输入密码");
                btn_forget.setText("忘记密码");
                //ck_remember.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rb_verifycode) { // 选择了验证码登录
                tv_password.setText("　验证码：");
                et_password.setHint("请输入验证码");
                btn_forget.setText("获取验证码");
                //ck_remember.setVisibility(View.INVISIBLE);
            }
        }
    }
    // 定义编辑框的文本变化监听器
    private class HideTextWatcher implements TextWatcher {
        private EditText mView;
        private int mMaxLength;
        private CharSequence mStr;
        HideTextWatcher(EditText v) {
            super();
            mView = v;
            mMaxLength = ViewUtil.getMaxLength(v);
        }
        // 在编辑框的输入文本变化前触发
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        // 在编辑框的输入文本变化时触发
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mStr = s;
        }

        // 在编辑框的输入文本变化后触发
        public void afterTextChanged(Editable s) {
            if (mStr == null || mStr.length() == 0)
                return;
            // 手机号码输入达到11位，或者密码/验证码输入达到6位，都关闭输入法软键盘
            if ((mStr.length() == 11 && mMaxLength == 11) ||
                    (mStr.length() == 8 && mMaxLength == 8)) {
                ViewUtil.hideOneInputMethod(MainActivity.this, mView);
            }
        }
    }
}