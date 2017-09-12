package io.tehtotalpwnage.keyclubinterface;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import io.tehtotalpwnage.keyclubinterface.R;

public class BarcodeListActivity extends AppCompatActivity {

    private ArrayList<String> mMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_list);
        mMembers = getIntent().getStringArrayListExtra("members");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem_barcode_list, R.id.textView4, mMembers);
        ListView listView = (ListView) findViewById(R.id.listview_barcode_list);
        listView.setAdapter(adapter);
    }
}
