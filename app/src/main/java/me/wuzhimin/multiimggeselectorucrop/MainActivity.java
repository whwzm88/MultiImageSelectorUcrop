package me.wuzhimin.multiimggeselectorucrop;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity {
    private List<String> imgs = new ArrayList<>();
    private static final int SELECT_PHOTO_DATA = 1;
    private ImageResultAdapter adapter;
    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiImageSelector.create()
                        .showCamera(true) // show camera or not. true by default
                        //编辑时只能选择一张图,添加时最多选9张
                        .count(9 - imgs.size()) // max select image size, 9 by default. used width #.multi()
                        .multi()// single mode
                        .origin(new ArrayList<String>()) // original select data set, used width #.multi()
                        .ucrop(false)
                        .cropShow(true)
                        .maxSide(2000)
                        .minRatio("16/9")
                        .maxRatio("3/4")
                        .dpi(70)
                        .start(MainActivity.this, SELECT_PHOTO_DATA);
            }
        });
        ListView lv_list = findViewById(R.id.lv_list);
        adapter = new ImageResultAdapter(this, imgs);
        lv_list.setAdapter(adapter);
        verifyStoragePermissions(this);
    }

    private void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case SELECT_PHOTO_DATA:
                //返回的数据
                List<String> mSelected = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                Log.e("TAG","添加"+mSelected.size()+"张图");
                if (mSelected != null && mSelected.size() > 0) {
                    imgs.addAll(mSelected);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
