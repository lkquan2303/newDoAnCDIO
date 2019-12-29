package com.example.doantichhop_cdio_moi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DangKy extends AppCompatActivity implements View.OnClickListener {
    EditText edtdn, edmatkhau, ednhaplaimk;
    Button btdk;
    TextView tvfail;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        edtdn = findViewById(R.id.edtdn);
        edmatkhau  = findViewById(R.id.edmk);
        ednhaplaimk = findViewById(R.id.ednhaplaimk);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        btdk = findViewById(R.id.btdk);
        db = FirebaseFirestore.getInstance();
        btdk.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        progressDialog.setMessage("Đang Xử Lý, Chờ Xíu");
        progressDialog.show();

        String email = edtdn.getText().toString();
        String pass = edmatkhau.getText().toString();
        String repass = ednhaplaimk.getText().toString();
        String[] output = email.split("\\@");
        //     boolean checkdkmail = checkmail(email);

        if(email.length() < 14 || pass.length() < 6 ||  checkmail(email.trim()) == false || email.length() > 36)
        {
            tvfail.setText(" Email Hoặc Mật Khẩu Không Hợp Lệ");
            progressDialog.dismiss();
        }else if(!output[1].equals("gmail.com"))
        {
            tvfail.setText(" Email Hoặc Mật Khẩu Không Hợp Lệ");
            progressDialog.dismiss();
        }else if(isNum(output[0]) == true)
        {
            tvfail.setText(" Email Hoặc Mật Khẩu Không Hợp Lệ");
            progressDialog.dismiss();
        }
        else  if(!pass.equals(repass))
        {
            tvfail.setText(" Mật Khẩu Không Trùng Khớp");
            progressDialog.dismiss();
        }
        else {
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        createdocument();
                        Toast.makeText(DangKy.this, " Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(DangKy.this, DangNhap.class);
                        startActivity(intent);
                    }else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Log.w("Tag", "signInWithEmail:failure", task.getException());
                        tvfail.setText(" Email đã tồn tại");

                    }
                }
            });
        }

    }
    // Kiểm tra email có ký tự đặc biệt hay không
    private boolean checkmail(String mail)
    {
        // return Patterns.EMAIL_ADDRESS.matcher(mail).matches();
        String emailPattern =  "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern regex = Pattern.compile(emailPattern);
        Matcher matcher = regex.matcher(mail);
        if(matcher.find())
        {
            return true;
        }else
        {
            return false;
        }
    }

    private void createdocument()
    {
        Map<String, Object> data = new HashMap<>();
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        data.put("name", "");
        data.put("height", "");
        data.put("weight", "");
        db.collection("informations")
                .document(id).set(data);
    }
    //Kiểm tra chuỗi có chứa ký tự số không
    public static boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

