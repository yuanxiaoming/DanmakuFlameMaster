package com.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_entrance);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_enter_glsurfaceview:
                Intent intent = new Intent();
                intent.setClass(this, BiliDanmaActivity.class);
                intent.putExtra(BiliDanmaActivity.TYPE, BiliDanmaActivity.TYPE_DANMAKU_GL_VIEW);
                startActivity(intent);
                break;
            case R.id.btn_enter_view:
                intent = new Intent();
                intent.setClass(this, BiliDanmaActivity.class);
                intent.putExtra(BiliDanmaActivity.TYPE, BiliDanmaActivity.TYPE_DANMAKU_VIEW);
                startActivity(intent);
                break;
            case R.id.btn_surface_view:
                intent = new Intent();
                intent.setClass(this, BiliDanmaActivity.class);
                intent.putExtra(BiliDanmaActivity.TYPE,
                        BiliDanmaActivity.TYPE_DANMAKU_SURFACE_VIEW);
                startActivity(intent);
                break;
            case R.id.btn_texture_view:
                intent = new Intent();
                intent.setClass(this, BiliDanmaActivity.class);
                intent.putExtra(BiliDanmaActivity.TYPE,
                        BiliDanmaActivity.TYPE_DANMAKU_TEXTURE_VIEW);
                startActivity(intent);
                break;
        }
    }
}
