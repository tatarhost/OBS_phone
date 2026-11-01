package app.tatarhost.obsphone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;

public class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST = 701;
    private static final int AUDIO_REQUEST = 702;
    private TextView status;
    private Button recordButton;
    private boolean recording = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(12, 13, 19));
        getWindow().setNavigationBarColor(Color.rgb(12, 13, 19));
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 703);
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(20));
        root.setBackgroundColor(Color.rgb(12,13,19));

        TextView brand = new TextView(this);
        brand.setText("OBS  /  PHONE");
        brand.setTextColor(Color.rgb(160,135,255));
        brand.setTextSize(13);
        brand.setLetterSpacing(.14f);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView title = new TextView(this);
        title.setText("Studio");
        title.setTextColor(Color.WHITE);
        title.setTextSize(34);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(10);
        root.addView(title, titleLp);

        TextView sub = new TextView(this);
        sub.setText("Mobile screen recording studio");
        sub.setTextColor(Color.rgb(150,153,170));
        sub.setTextSize(14);
        root.addView(sub);

        TextView preview = new TextView(this);
        preview.setText("LIVE PREVIEW\n\nScreen capture preview appears while recording");
        preview.setGravity(Gravity.CENTER);
        preview.setTextColor(Color.rgb(180,181,197));
        preview.setTextSize(14);
        preview.setBackgroundColor(Color.rgb(27,29,40));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 0, 1);
        p.topMargin = dp(26); p.bottomMargin = dp(20);
        root.addView(preview, p);

        status = new TextView(this);
        status.setText("●  READY TO RECORD");
        status.setTextColor(Color.rgb(120,220,170));
        status.setTextSize(12);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(status);

        recordButton = new Button(this);
        recordButton.setText("Start recording");
        recordButton.setAllCaps(false);
        recordButton.setTextSize(16);
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(-1, dp(58));
        b.topMargin = dp(12);
        root.addView(recordButton, b);
        recordButton.setOnClickListener(v -> {
            if (recording) stopRecording(); else requestCapture();
        });

        TextView note = new TextView(this);
        note.setText("Capture starts only after Android permission confirmation. Files are saved in Movies/OBSPhone.");
        note.setTextColor(Color.rgb(120,123,140));
        note.setTextSize(12);
        LinearLayout.LayoutParams n = new LinearLayout.LayoutParams(-1, -2);
        n.topMargin = dp(14);
        root.addView(note, n);
        setContentView(root);
    }

    private void requestCapture() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST);
            return;
        }
        MediaProjectionManager manager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(), CAPTURE_REQUEST);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == CAPTURE_REQUEST && result == RESULT_OK && data != null) {
            Intent service = new Intent(this, RecordingService.class);
            service.setAction(RecordingService.ACTION_START);
            service.putExtra("resultCode", result);
            service.putExtra("data", data);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
            recording = true;
            recordButton.setText("Stop recording");
            status.setText("●  RECORDING");
            status.setTextColor(Color.rgb(255,105,120));
        } else if (request == CAPTURE_REQUEST) {
            status.setText("Capture permission was not granted");
        }
    }

    private void stopRecording() {
        Intent service = new Intent(this, RecordingService.class);
        service.setAction(RecordingService.ACTION_STOP);
        startService(service);
        recording = false;
        recordButton.setText("Start recording");
        status.setText("●  READY — recording saved");
        status.setTextColor(Color.rgb(120,220,170));
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
}
