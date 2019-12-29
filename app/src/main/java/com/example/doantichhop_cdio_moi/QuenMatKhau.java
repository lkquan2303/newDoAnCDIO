package com.example.doantichhop_cdio_moi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuenMatKhau extends AppCompatActivity implements View.OnClickListener {
    EditText edquenmk;
    Button btkhoiphuc;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    TextView tvshowthanhcong,tvshowthatbai;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);
        edquenmk = findViewById(R.id.edquenmk);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = new ProgressBar(this);
        btkhoiphuc = findViewById(R.id.btkhoiphuc);
        btkhoiphuc.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        String q = edquenmk.getText().toString();
        String[] output =  q.split("\\@");
        int id = view.getId();
        switch (id)
        {
            case R.id.btkhoiphuc:
                String email = edquenmk.getText().toString();
                boolean checkmail = kiemtraemail(email);
                if(email.equals("")){
                    tvshowthatbai.setText("Email không hợp lệ");
                }else
                if(isNum(output[1])){
                    tvshowthatbai.setText("Email không hợp lệ");
                }
                else if(checkmail && output[1].equals("gmail.com"))
                {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                tvshowthatbai.setText("");
                                tvshowthanhcong.setText("Vui lòng kiểm tra email và làm theo hướng dẫn");
                            }else { tvshowthatbai.setText("Email chưa được đăng ký");}
                        }
                    });
                }else {
                    tvshowthatbai.setText("Email không hợp lệ");
                }
                break;
        }
    }
    private boolean kiemtraemail(String email)
    {
        String emailPattern =  "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern regex = Pattern.compile(emailPattern);
        Matcher matcher = regex.matcher(email);
        if(matcher.find())
        {
            return true;
        }else
        {
            return false;
        }
    }
    public static boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

