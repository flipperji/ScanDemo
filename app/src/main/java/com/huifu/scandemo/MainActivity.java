package com.huifu.scandemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huifu.hlmscan.Scanner;
import com.huifu.hlmscan.code.BarcodeFormat;
import com.huifu.hlmscan.code.BarcodeReader;
import com.huifu.hlmscan.code.CodeResult;
import com.huifu.hlmscan.util.BitmapUtil;
import com.huifu.hlmscan.view.ScanActivityDelegate;
import com.huifu.hlmscan.view.ScanView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTvResult;
    private static final int CODE_SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvResult = findViewById(R.id.tvResult);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openScan();
            }
        });
    }

    private void openScan() {

        Resources resources = getResources();
        List<Integer> scanColors = Arrays.asList(resources.getColor(R.color.scan_side), resources.getColor(R.color.scan_partial), resources.getColor(R.color.scan_middle));
        Scanner.with(this)
                .setMaskColor(resources.getColor(R.color.mask_color))
                .setBorderColor(resources.getColor(R.color.box_line))
                .setBorderSize(dp2px(this, 200))
                .setCornerColor(resources.getColor(R.color.corner))
                .setScanLineColors(scanColors)
                .setScanMode(ScanView.SCAN_MODE_MIX)
                //                .setBarcodeFormat(BarcodeFormat.EAN_13)
                .setTitle("我的扫一扫")
                .showAlbum(true)
                .setScanNoticeText("扫描二维码")
                .setFlashLightOnText("打开闪光灯")
                .setFlashLightOffText("关闭闪光灯")
                //                .setFlashLightInvisible()
                .setFlashLightOnDrawable(R.drawable.ic_highlight_blue_open_24dp)
                .setFlashLightOffDrawable(R.drawable.ic_highlight_white_close_24dp)
                .continuousScan()
                .setOnClickAlbumDelegate(new ScanActivityDelegate.OnClickAlbumDelegate() {
                    @Override
                    public void onClickAlbum(Activity activity) {
                        Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        activity.startActivityForResult(albumIntent, CODE_SELECT_IMAGE);
                    }

                    @Override
                    public void onSelectData(int requestCode, Intent data) {
                        if (requestCode == CODE_SELECT_IMAGE) {
                            decodeImage(data);
                        }
                    }
                })
                .setOnScanResultDelegate(new ScanActivityDelegate.OnScanDelegate() {
                    @Override
                    public void onScanResult(@NonNull final String result, @NonNull BarcodeFormat format) {
                        // 如果有回调，则必然有值,因为要避免AndroidX和support包的差异，所以没有默认的注解
                        //                        Intent intent = new Intent(MainActivity.this, DelegateActivity.class);
                        //                        intent.putExtra("result", result);
                        //                        startActivity(intent);

                        final String showContent = "format: " + format.name() + "  code: " + result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, showContent, Toast.LENGTH_SHORT).show();
                                mTvResult.setText("扫码结果： " + result);
                            }
                        });
                    }
                })
                .start();
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    private void decodeImage(Intent intent) {
        Uri selectImageUri = intent.getData();
        if (selectImageUri == null) {
            return;
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        // 适当压缩图片
        Bitmap bitmap = BitmapUtil.getDecodeAbleBitmap(picturePath);
        if (bitmap == null) {
            return;
        }

        CodeResult result = BarcodeReader.getInstance().read(bitmap);
        if (result == null) {
            Log.e("Scan >>> ", "no code");
            return;
        } else {
            Log.e("Scan >>> ", result.getText());
        }
        mTvResult.setText("扫码结果： " + result.getText());
    }
}
