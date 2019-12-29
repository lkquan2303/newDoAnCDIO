package com.example.doantichhop_cdio_moi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class TimKiem extends AppCompatActivity {
    String[] items;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem);
        listView = (ListView) findViewById(R.id.listview);
        editText = (EditText) findViewById(R.id.edsearch);
        initList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(TimKiem.this, listItems.get(i), Toast.LENGTH_SHORT).show();
            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.toString().equals("")){
//                    // reset listview
//                    initList();
//                } else {
//                    // perform search
//                    searchItem(s.toString());
//                }
                TimKiem.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent  = new Intent(TimKiem.this, MainActivity.class);
                intent.putExtra("name_image", listItems.get(position));
                startActivity(intent);
            }
        });
    }

//    public void searchItem(String textToSearch){
//        for(String item:items){
//            if(!item.contains(textToSearch)){
//                listItems.remove(item);
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }

    public void initList() {
        items = new String[]{"cho","gau", "hoa", "meo", "tegiac", "tho", "voi", "chim"};
        listItems = new ArrayList<>(Arrays.asList(items));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
    }
}

