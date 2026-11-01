package app.tatarhost.obsphone;

import android.app.*;
import android.content.*;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingService extends Service {
    public static final String ACTION_START = "app.tatarhost.obsphone.START";
    public static final String ACTION_STOP = "app.tatarhost.obsphone.STOP";
    private static final String CHANNEL = "obs_recording";
    private MediaRecorder recorder;
    private MediaProjection projection;
    private VirtualDisplay display;

    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Screen recording", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public int onStartCommand(Intent intent, int flags, int id) {
        if (intent == null) return START_NOT_STICKY;
        if (ACTION_STOP.equals(intent.getAction())) { stopCapture(); return START_NOT_STICKY; }
        if (ACTION_START.equals(intent.getAction())) {
            Notification notification = new Notification.Builder(this, CHANNEL)
                .setContentTitle("OBS Phone")
                .setContentText("Screen recording is active")
                .setSmallIcon(android.R.drawable.presence_video_online)
                .build();
            if (Build.VERSION.SDK_INT >= 29)
                startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION | android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            else startForeground(1, notification);
            try { startCapture(intent); }
            catch (Exception e) { stopCapture(); }
        }
        return START_NOT_STICKY;
    }

    private void startCapture(Intent intent) throws Exception {
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("data");
        MediaProjectionManager manager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection = manager.getMediaProjection(resultCode, data);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.Display d = getDisplay();
            d.getRealMetrics(metrics);
        } else wm.getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels & ~1;
        int height = metrics.heightPixels & ~1;
        int dpi = metrics.densityDpi;

        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "OBSPhone");
        if (!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Cannot create output folder");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File output = new File(folder, "OBS_" + stamp + ".mp4");

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(output.getAbsolutePath());
        recorder.setVideoSize(width, height);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoEncodingBitRate(8_000_000);
        recorder.setVideoFrameRate(30);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(128_000);
        recorder.setAudioSamplingRate(44100);
        recorder.prepare();
        display = projection.createVirtualDisplay("OBSPhoneCapture", width, height, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, recorder.getSurface(), null, null);
        recorder.start();
        projection.registerCallback(new MediaProjection.Callback() {
            @Override public void onStop() { stopCapture(); }
        }, null);
    }

    private void stopCapture() {
        try { if (display != null) { display.release(); display = null; } } catch (Exception ignored) {}
        try { if (recorder != null) { recorder.stop(); } } catch (Exception ignored) {}
        try { if (recorder != null) { recorder.reset(); recorder.release(); recorder = null; } } catch (Exception ignored) {}
        try { if (projection != null) { projection.stop(); projection = null; } } catch (Exception ignored) {}
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override public void onDestroy() { stopCapture(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
