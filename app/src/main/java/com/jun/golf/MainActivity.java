package com.jun.golf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.os.Environment.DIRECTORY_DCIM;
import static com.jun.golf.SafeDetect.direction_center_x_offset;
import static com.jun.golf.SafeDetect.get_safe_zone_detect;
import static com.jun.golf.SafeDetect.hit_predict;

import com.google.common.util.concurrent.ListenableFuture;
import com.umeng.commonsdk.debug.D;

public class MainActivity extends AppCompatActivity {

    //????????? ??????????????? ??????
    public static int YOLOV5S = 1;
    public static int YOLOV4_TINY = 2;
    public static int MOBILENETV2_YOLOV3_NANO = 3;
    public static int SIMPLE_POSE = 4;
    public static int YOLACT = 5;
    public static int ENET = 6;
    public static int FACE_LANDMARK = 7;
    public static int DBFACE = 8;
    public static int MOBILENETV2_FCN = 9;
    public static int MOBILENETV3_SEG = 10;
    public static int YOLOV5_CUSTOM_LAYER = 11;
    public static int NANODET = 12;
    public static int YOLO_FASTEST_XL = 13;
    public static int LANE_LSTR = 14;
    public static int YOLOX = 14;
    //????????? ??????????????? ??????

    public static boolean CONFIG_MULTI_DETECT = true;
    public static boolean controller_click_count = false;   //????????????
    public static boolean video_click_count = false;
    public static String app_version="1.0";
    public static String app_V = "???????????? V"+app_version;
    public static int pic_count = 0;                        //????????????
    public static boolean ultra_fast_mode = true;           //?????? true??? ????????? ????????? ????????????
    public static int USE_MODEL = LANE_LSTR;                //?????? ???????????? ????????? ???????????????. LANE??? ?????? ????????? LANE_LSTR??? ????????????

    public static boolean USE_GPU = false;  //?????? true??? ????????? ?????? ????????? ????????? ???????????? ??????????????? ???????????????
    public static int num = 0;

    public static Sensor mTempSensor =null;                 //????????????
    public static float device_temperature = 25;            //????????????

    private static final int REQUEST_CAMERA = 1;            //????????????
    private static final int REQUEST_PICK_IMAGE = 2;        //????????????
    private static final int REQUEST_PICK_VIDEO = 3;        //????????????
    private static String[] PERMISSIONS_CAMERA = {          //????????? ?????? ??????
            Manifest.permission.CAMERA
    };

    private ImageView resultImageView;                      //????????????
    private ResultView mResultView;                         //????????????
    private ImageView iv_detect_input;                      //????????????
    private ImageView iv_lane_input;                        //????????????

    private TextView tvInfo;                                //????????????
    private LinearLayout container;                         //????????????
    private Button btnPhoto;                                //"??????????????????" ??????
    private Button btnController;                           //"????????????" ?????? (????????? ???????????? ???????????? ?????? ????????? ?????? ??????)
    private Button btnSetting;                              //"??????" ??????
    private Button btnCamera;                               //????????????
    private Button btnVideo;                                //????????????
    private Button menubar_clear;
    private Button camera_chage;

    public static Boolean check_video_line = false;
    public static Boolean check_video_object = false;
    private Button test_video_pic;

    public static Boolean check_photo = false;
    public static Boolean check_frame = false;

    //state
    private TextView appName;                               //Appname
    private TextView fpsTextview;                           //fps textview id
    private TextView elec_value_Textview;                   //????????? textview id
    private TextView ele_value;                             //???????????? RAM ???????
    private TextView time;                                  //?????? ?????? text view
    private TextView date;                                  //?????? textview id
    private TextView image_size;                            //image size textview id
    private TextView steering_angle_text;                   //steering angle textview id

    //test
    private Button btn_direction;                           //???????????? ???????
    private SeekBar speed_seek;                             //????????? ??????

    //swtich
    private static boolean object_check = false;             //Object switch??? ?????? bool ???
    private static boolean line_check = false;               //Line switch??? ?????? bool ???
    private Switch object_switch;                           //Object switch ??????
    private Switch line_swtich;                             //Line switch ??????

    //Serial
    private Button cart_stop;                               //"????????????" ??????
    public static Boolean serial_check = false;             //????????????
    public static boolean serial_btn_check = false;         //????????????
    private TextView serial_text;                           //????????????
    private TextView crash_text;                            //????????????
    private TextView cart_speed;                            //????????????

    //safe_Object
    public static boolean detect_danger_person = false;     //????????????
    public static float[] person_y1 = new float[50];        //????????????
    public static int person_box_count = 0;
    public static float safe_line_1 = 0.0f;

    //Rec_VIDEO
    private Button Rec_video_btn;
    public static boolean rec_video_click_count = false;


    //private ListView serial_list;
    private  boolean param_toggle = false;
    //    public static double threshold = 0.3, nms_threshold = 0.7;
    public static double lane_nms_threshold = 0.7;
    public static double threshold = 0.1, nms_threshold = 0.20;

    public static double front_detect=0.5;
    public static float DetectWidth = 1.05f;
    public static float CityDetectHeight = 0.5f;
    public static float LaneDetectHeight = 0.5f;

    //Distance
    public static List<Float> distance_list = new ArrayList<Float>();
    public static List<String> label_list = new ArrayList<String>();
    public static float crash_pre_time;
    public static String crash_pre_time_str_value;
    public static String near_label;
    public static float cart_speed_value;

    //cart_speed
    public static double RPM_speed;
    public static String angle_speed;
    public static Button test_start_btn;
    public static boolean test_btn_check = false;
    public static boolean test_value_check = false;
    public static String time_value;

    public static int txt_count_value = 0;
    private TextureView viewFinder;
    protected float videoSpeed = 1.0f;
    protected long videoCurFrameLoc = 0;
    public static int VIDEO_SPEED_MAX = 20 + 1;
    public static int VIDEO_SPEED_MIN = 1;
    public static int frame_count = 0;

    public static int all_pixel_x;
    public static int all_pixel_y;

    private int current_rotation_degree = 0;
    //????????? ?????? ??????
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.FRONT;
    private boolean camera_check = true;
    public static String path_address;

    private AtomicBoolean detectCamera = new AtomicBoolean(false);
    private AtomicBoolean detectVideo = new AtomicBoolean(false);
    private AtomicBoolean detectPhoto = new AtomicBoolean(false);
    private AtomicBoolean detectYolov4 = new AtomicBoolean(false);

    private long startTime = 0;
    private long endTime = 0;
    private int width;
    private int height;

    public double total_fps = 0;
    public int fps_count = 0;
    public double avg_fps = 0;

    public static int test_vlaue_person = 0;

    public static float recent_fps =0;
    ImageProxy image;
    protected Bitmap mutableBitmap;
    protected Bitmap resizedBitmap;
    protected Bitmap bitmapsrc;

    private long last_press_back_time =0 ;

    ExecutorService detectService = Executors.newSingleThreadExecutor();

    FFmpegMediaMetadataRetriever mmr;

    public static boolean view_setting_lines = false;
    public static int USE_DEBUG_PHOTO_ID = 0;
    public static int OBJECT_DETECION = 0;
    public static int LINE_DETECTION = 0;
    public static boolean USE_FAST_EXP = false;

    public SoundPool mSoundPool;
    public int alarm_voiceId;
    public int alarm_voiceId1;
    public int alarm_voiceId2;
    public int alarm_voiceId3;
    public int highway_voiceId;
    public int cityroad_voiceId;
    public int outskirts_voiceId;
    public int direction_off_voiceId;
    public int direction_on_voiceId;
    public int person_focus_off_voiceId;
    public int person_focus_on_voiceId;
    public static int front_car_start_voiceId;
    public static int mute_2min_voiceId;
    public static int mute_voiceId;
    public static int sound_on_voiceId;
    public static int welcome_voiceId;
    public static int rest_2hour_voiceId;
    public static int toohot_voiceId;
    public static int alarm_wait_time = 20;

    public static long mute_end=0;
    public static int mute_func_idx=0;

    public SafeDetect msafeDetect = new SafeDetect();

    public static String detect_msg = "";
    public static Context mcontext;
    public static Activity mactivity;
    public static long advanced_func_key = 0;
    public static long key_create_time = 0;

    //kjk1020
    public static int input_size_idx = 1 ;

    //0 ?????? ???1 ?????????2????????????????????? 3????????????????????? 4,????????????????????????
    public static int alarm_mode = 1;
    public static int last_alarm_mode = alarm_mode;
    public static String openid = "";
    public static String deviceid = "";
    //0 ????????? ???1 ?????????2????????? 3?????????
    public static int road_type = 1;
    //0 ?????? ???1 ?????????2??????????????? 3???????????????
    public static int auto_adjust_detect_area = 0;
    public static int person_detect_focus = 0;
    public static boolean far_enhanced_detect = true;
    public static  int USE_YOLOV4_DETECT = 0;
    public static  float carmera_height = 1.2f;
    public static  float distance_fix = 1.f;
    public static  float vertical_distance_rate = 1.f;
    public static long sys_start_time = 0;
    public static long reset_notice_start_time = 0;
    public static long toohot_alarm_time =0;
    public static Box[] detect_full_result = null;
    public static Box[] detect_far_result = null;
    public static Box[] lane_result = null;

    private VideoCaptureConfig videoCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        mactivity = this;
        setContentView(R.layout.activity_main);
        sys_start_time = SystemClock.elapsedRealtime(); //?????????
        reset_notice_start_time = sys_start_time;
//        System.out.println(Build.MODEL);
        initLayout();
        init_param();
        initModel();
        initViewID();
        initViewListener();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//?????? ???????????? ??????
        initSoundPool();
        checkPermission();
        RPM_speed = 1.0;
        cart_speed.setText("1.0");

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
            startTime = new Date().getTime();

        }else{
            startCamera();
        }


        try{
            SensorManager mSmanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            //List<Sensor> allSensors = mSmanager.getSensorList(Sensor.TYPE_ALL);
            mTempSensor =   mSmanager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            // ????????? ????????? ???????????? if ???
            SensorEventListener mSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getStringType().toUpperCase().indexOf("TEMP") > 0) {
                        device_temperature = event.values[0];
                        //Log.e("temperature: ", String.valueOf(device_temperature));
                        //mSmanager.unregisterListener(mSensorEventListener, mTempSensor);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

            if (mTempSensor != null) {
                mSmanager.registerListener(mSensorEventListener, mTempSensor
                        , SensorManager.SENSOR_DELAY_GAME);
            }
        }catch (Exception e) {

        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(4000);
//                    play_sound(welcome_voiceId);
//                } catch (Exception e) {
//                }
//            }}).start();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
    }//onCretae

    protected void checkCamera(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    mactivity,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
        }else {
            startCamera();
        }

    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // ??????????????? ????????? ????????? ???????????????
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "?????? ????????? ????????? ?????? ??????/?????? ??????", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);  //????????? ????????? ??????????????? ?????? ??????

            } else {
                //Toast.makeText(this, "?????? ???????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void initSoundPool(){

        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //??????????????????????????????,
            builder.setMaxStreams(10);
            //AudioAttributes??????????????????????????????????????????
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //?????????????????????????????????
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            //????????????AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();

        } else {
            /**
             * ??????????????????int maxStreams???SoundPool???????????????????????????
             * ??????????????????int streamType???AudioManager???????????????????????????
             *??????????????????int srcQuality????????????????????????????????? ????????????????????? ??????0??????????????????
             */
            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        alarm_voiceId = mSoundPool.load(this, R.raw.alarm, 1);
        alarm_voiceId1 = mSoundPool.load(this, R.raw.alarm1, 1);
        alarm_voiceId2 = mSoundPool.load(this, R.raw.alarm2, 1);
        alarm_voiceId3 = mSoundPool.load(this, R.raw.alarm3, 1);

        highway_voiceId = mSoundPool.load(this, R.raw.highway, 1);
        cityroad_voiceId = mSoundPool.load(this, R.raw.cityroad, 1);
        outskirts_voiceId = mSoundPool.load(this, R.raw.outskirts, 1);
        direction_off_voiceId = mSoundPool.load(this, R.raw.direction_adjust_off, 1);
        direction_on_voiceId = mSoundPool.load(this, R.raw.direction_adjust_on, 1);
        person_focus_off_voiceId = mSoundPool.load(this, R.raw.person_focus_off, 1);
        person_focus_on_voiceId = mSoundPool.load(this, R.raw.person_focus_on, 1);
        front_car_start_voiceId = mSoundPool.load(this, R.raw.front_car_start, 1);
        mute_2min_voiceId = mSoundPool.load(this, R.raw.mute2min, 1);
        mute_voiceId = mSoundPool.load(this, R.raw.mute, 1);
        sound_on_voiceId = mSoundPool.load(this, R.raw.sound_on, 1);
        welcome_voiceId = mSoundPool.load(this, R.raw.welcome, 1);
        rest_2hour_voiceId = mSoundPool.load(this, R.raw.rest_at_2hour, 1);
        toohot_voiceId = mSoundPool.load(this, R.raw.rest_at_2hour, 1);
    }

    public void play_alarm(float rate){
        mSoundPool.play(alarm_voiceId2, 1, 1, 1, 0, 1);
    }
    public void play_alarm1(float rate){
        mSoundPool.play(alarm_voiceId1, 1, 1, 1, 0, 1);
    }
    public void play_alarm3(){
        mSoundPool.play(alarm_voiceId3, 1, 1, 1, 0, 1.5f);
    }
    public void stop_alarm(){
        mSoundPool.stop(alarm_voiceId2);
    }
    public void stop_alarm3(){
        mSoundPool.stop(alarm_voiceId3);
    }
    public void play_sound(int resid){
        mSoundPool.play(resid, 1, 1, 1, 0, 1f);
    }

    protected void initViewListener() {
        if (USE_MODEL != YOLOV5S  && USE_MODEL != DBFACE && USE_MODEL != NANODET && USE_MODEL != YOLOV5_CUSTOM_LAYER) {
        } else if (USE_MODEL == YOLOV5S) {
            threshold = 0.3f;
            nms_threshold = 0.7f;
        } else if (USE_MODEL == DBFACE || USE_MODEL == NANODET) {
            threshold = 0.4f;
            nms_threshold = 0.6f;
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            threshold = 0.1f;
            nms_threshold = 0.65f;
        }

        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                line_swtich.setChecked(false);
                object_switch.setChecked(true);
                check_video_object =true;
                check_photo = true;
            }
        });

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USE_DEBUG_PHOTO_ID ++;
                if(USE_DEBUG_PHOTO_ID>51){
                    USE_DEBUG_PHOTO_ID = 0;
                }else if(USE_DEBUG_PHOTO_ID ==51){
                    check_video_line = false;
                }
            }
        });

        test_video_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_video_line == true){
                    LINE_DETECTION ++;
                    if(LINE_DETECTION>274){
                        LINE_DETECTION = 0;
                    }else if(LINE_DETECTION ==273){
                        toast_msg("Line ?????? ????????? ??????");
                        check_video_line = false;

                    }
                }else if(check_video_object ==true){
                    OBJECT_DETECION ++;
                    if(OBJECT_DETECION>342){
                        OBJECT_DETECION = 0;
                    }else if(OBJECT_DETECION ==341){
                        toast_msg("Object ?????? ????????? ??????");
                        check_video_object = false;
                    }
                }
            }
        });
        //btn_Video
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(video_click_count == false){
//                    btnVideo.setText("????????? ????????? ??????");
//                    video_click_count = true;
//                    doSelectMovie();
//                }//controller_click CHECK
//                else {
//                    detectVideo.set(false);
//                    checkCamera();
//                    btnVideo.setText("????????? ????????? ??????");
//                    video_click_count = false;
//                }
                line_swtich.setChecked(true);
                object_switch.setChecked(false);
                check_video_line = true;
                check_photo = true;
            }
        });

        //cart_stop
        cart_stop.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(serial_btn_check == false) {
                    send_serial("+0.000", "+0.000");
                    cart_stop.setBackgroundColor(getColor(R.color.red));
                    cart_stop.setText("Lock??????");
                    serial_btn_check = true;
                }else if(serial_btn_check == true){
                    serial_btn_check = false;
                    cart_stop.setBackgroundColor(getColor(R.color.colorRecieveText));
                    cart_stop.setText("????????????");
                }
            }
        });

        //test_start_btn
        test_start_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (test_btn_check == false){
                    String ss = String.valueOf(RPM_speed);

//                    send_serial("+1.200","+0.000");
                    test_start_btn.setBackgroundColor(getColor(R.color.colorStatusText));
                    test_start_btn.setText("????????? ??????");
                    test_btn_check = true;

                    test_value_check = true;
                }else if(test_btn_check == true){
                    test_btn_check = false;
                    test_value_check= false;
                    txt_write("----------------");
                    test_start_btn.setBackgroundColor(getColor(R.color.colorRecieveText));
                    test_start_btn.setText("????????? ??????");
                }
            }
        });

        //controller_click_event
        btnController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(controller_click_count == false){
                    toast_msg("???????????? ?????? ?????????");
                    btnController.setText("???????????? ?????? ??????");
                    controller_click_count = true;
                    initControlerlayout();
                }//controller_click CHECK
                else {
                    toast_msg("???????????? ?????? ????????????");
                    btnController.setText("???????????? ?????? ??????");
                    controller_click_count = false;
                    initLayout();
                }
            }
        });

        //Rec_video
        Rec_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rec_video_click_count == false){
                    Rec_video_btn.setText("????????? ??????");
                    rec_video_click_count = true;
                }else if(rec_video_click_count == true){
                    Rec_video_btn.setText("????????? ?????? ??????");
                    rec_video_click_count = false;
                }
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCamera();
                toast_msg( "????????? ????????? ????????? ???????????? ????????? ?????? ?????? ?????????");
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectVideo.set(false);
                view_setting_lines = !view_setting_lines;
                far_enhanced_detect = true;
                view_setting();
                param_toggle = false;
                checkCamera();
                check_video_object = false;
                check_video_line = false;
                line_swtich.setChecked(false);
                object_switch.setChecked(false);
                check_photo=false;
            }
        });

        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detectPhoto.get()) {
                    detectPhoto.set(false);
                    startCamera();
                }
            }
        });

        menubar_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_setting_lines = !view_setting_lines;
                far_enhanced_detect = true;
                USE_DEBUG_PHOTO_ID=0;
                OBJECT_DETECION =0;
                LINE_DETECTION=0;
                viewFinder.setVisibility(View.GONE);
                tvInfo.setVisibility(View.GONE);
                mResultView.setVisibility(View.GONE);
                btnPhoto.setVisibility(View.GONE);
                Rec_video_btn.setVisibility(View.GONE);
                btn_direction.setVisibility(View.GONE);
                btnVideo.setVisibility(View.GONE);
                btnController.setVisibility(View.GONE);
                btnCamera.setVisibility(View.GONE);
                camera_chage.setVisibility(View.GONE);
                menubar_clear.setVisibility(View.GONE);
            }
        });
        //????????? ?????? ??????
        camera_chage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera_check == true){
                    lensFacing = CameraX.LensFacing.BACK;
                    camera_check = false;
                    startCamera();
                    camera_chage.setText("????????? ?????? ?????? ??????");
                }
                else{
                    lensFacing = CameraX.LensFacing.FRONT;
                    camera_check = true;
                    startCamera();
                    camera_chage.setText("????????? ?????? ?????? ??????");

                }
            }
        });

        speed_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RPM_speed=Math.round(Float.valueOf(progress))/100.0;
                cart_speed.setText(String.valueOf(RPM_speed));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if (USE_MODEL == YOLOV4_TINY || USE_MODEL == LANE_LSTR) {
            btnPhoto.setVisibility(View.GONE);
            btnVideo.setVisibility(View.GONE);
            btnController.setVisibility(View.GONE);
            Rec_video_btn.setVisibility(View.GONE);
            btn_direction.setVisibility(View.GONE);
            camera_chage.setVisibility(View.GONE);
            menubar_clear.setVisibility(View.GONE);
        }
        btnSetting.setVisibility(View.VISIBLE);

        object_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    object_check = false;
                }else if(isChecked){
                    object_check = true;
                }
            }
        });

        line_swtich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    line_check = false;
                }else if(isChecked){
                    line_check = true;
                }
            }
        });
    }

    //Serial
    protected void send_serial(String str1, String str2){
        ((TerminalFragment)TerminalFragment.mContext).send(str1+" "+str2);
    }

    public void serial_check(Boolean check){
        serial_check = check;
    }

    public void serial_read_1(String value){
//        serial_velo.setText(value);
    }

    public void serial_read_2(String value){
//        serial_angle.setText(value);
    }

    protected void initViewID() {
        resultImageView = findViewById(R.id.imageView);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        iv_detect_input= findViewById(R.id.detect_input);

        iv_detect_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_detect_input.setVisibility(View.GONE);
            }
        });

        iv_detect_input.setLongClickable(true);
        iv_detect_input.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                far_enhanced_detect = false;
                if(!far_enhanced_detect){
                    toast_msg("?????? ????????? ????????? ??????????????? ????????? ???????????? ?????? ????????????");
                }

                return true;
            }
        });
        if(advanced_func_key!=0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iv_detect_input.setVisibility(View.VISIBLE);
                }
            });
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iv_detect_input.setVisibility(View.GONE);
                }
            });
        }

        iv_lane_input= findViewById(R.id.lane_input);
        tvInfo = findViewById(R.id.tv_info);
        btnCamera= findViewById(R.id.camera_btn);
        btnPhoto = findViewById(R.id.btn_photo);
        btnVideo = findViewById(R.id.btn_video);
        btnController= findViewById(R.id.btn_controller);

        btnSetting = findViewById(R.id.btn_setting);
        viewFinder = findViewById(R.id.view_finder);

        appName = findViewById(R.id.app_name_value);
        date = findViewById(R.id.app_date_value);
        image_size = findViewById(R.id.image_size_value);
        steering_angle_text = findViewById(R.id.steering_angle);
        fpsTextview = findViewById(R.id.fps_value);
        elec_value_Textview = findViewById(R.id.elec_value);
        ele_value = findViewById(R.id.ele_value);
        cart_stop = findViewById(R.id.cart_stop);

        serial_text = findViewById(R.id.text_value);
        crash_text = findViewById(R.id.crash_value);
        cart_speed = findViewById(R.id.cart_speed_value);

        object_switch = findViewById(R.id.object_switch);
        line_swtich = findViewById(R.id.line_switch);

        speed_seek = findViewById(R.id.cart_speed_seek);

        Rec_video_btn = findViewById(R.id.rec_video);

        btn_direction = findViewById(R.id.btn_direction);
        test_start_btn = findViewById(R.id.test_start);

        menubar_clear = findViewById(R.id.menubar_clear);
        camera_chage = findViewById(R.id.btn_camera);
        test_video_pic = findViewById(R.id.test_video_pic);

    }

    //serial_Fn

    protected void initLayout(){
        Display display = getWindowManager().getDefaultDisplay();
        Point pixel_size = new Point();
        display.getRealSize(pixel_size);

        all_pixel_x = pixel_size.x;
        all_pixel_y = pixel_size.y;

        //camera_view_size_init
        ImageView camera_imgview = (ImageView)findViewById(R.id.imageView);
        camera_imgview.getLayoutParams().width = all_pixel_x/4 * 3;

        //Tablelayout_size_init
        TableLayout tableLayout = (TableLayout)findViewById(R.id.information_table);
        tableLayout.getLayoutParams().width = all_pixel_x/4;
        tableLayout.getLayoutParams().height = all_pixel_y;
        tableLayout.setVisibility(View.VISIBLE);

        //controller_layout
        LinearLayout controller_layout = (LinearLayout)findViewById(R.id.layout_controller);
        controller_layout.setVisibility(View.GONE);
    }//android xml(layout) init

    protected void initControlerlayout(){
        TableLayout tableLayout = (TableLayout)findViewById(R.id.information_table);
        //tableLayout.setVisibility(View.GONE);
        tableLayout.getLayoutParams().width = all_pixel_x/2;
        tableLayout.getLayoutParams().height = all_pixel_y/2;

        //visible_controller_layout
        LinearLayout controller_layout = (LinearLayout)findViewById(R.id.layout_controller);
        controller_layout.setVisibility(View.VISIBLE);
        controller_layout.getLayoutParams().width = all_pixel_x/2;
        controller_layout.getLayoutParams().height = all_pixel_y/2;

    }//android xml(Controlerlayout) init

    protected void initModel() {
        NcnnYolox.loadModel(getAssets(),0, 1);
        LSTR.init(getAssets(), 0, false);
    }

    public static void toast_msg(String msg){
        mactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mcontext,msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startCamera() {
        CameraX.unbindAll();
        DetectAnalyzer detectAnalyzer = new DetectAnalyzer();
        CameraX.bindToLifecycle((LifecycleOwner) this, gainAnalyzer(detectAnalyzer));
    }

    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        //????????? ?????? ??????
        analysisConfigBuilder.setLensFacing(lensFacing);
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        int[][] input_size_a = new int[][] { new int[] {640 ,480},new int[] {1280 ,720},
                new int[] {1280 ,960},new int[] {1440 ,1080},new int[] {1920 ,1440},new int[] {2560 ,1920}};
        //{960,720}
        //analysisConfigBuilder.setTargetResolution(new Size(1280, 720));
        analysisConfigBuilder.setTargetResolution(new Size(input_size_a[input_size_idx][0], input_size_a[input_size_idx][1]));
        analysisConfigBuilder.setTargetAspectRatio(new Rational(input_size_a[input_size_idx][0], input_size_a[input_size_idx][1]));

        //kjk1020
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }

    public static void save_img(Bitmap bitmap) {
        String ex_storage = Environment.getExternalStorageDirectory()+"/DCIM/test";
        String foler_name = "/";
        pic_count = pic_count+1;
        String str_pic_count = String.valueOf(pic_count);
        String file_name = str_pic_count + ".jpg";
        toast_msg(file_name);
        String string_path = ex_storage+foler_name;
        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }

    }
    private Bitmap imageToBitmap(ImageProxy image) {
        byte[] nv21 = imagetToNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private byte[] imagetToNV21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ImageProxy.PlaneProxy y = planes[0];
        ImageProxy.PlaneProxy u = planes[1];
        ImageProxy.PlaneProxy v = planes[2];
        ByteBuffer yBuffer = y.getBuffer();
        ByteBuffer uBuffer = u.getBuffer();
        ByteBuffer vBuffer = v.getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] nv21 = new byte[ySize + uSize + vSize];
        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    private class DetectAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            detectOnModel(image, rotationDegrees);
        }
    }


    private void detectOnModel(ImageProxy image, final int rotationDegrees) {
        if (detectCamera.get() || detectPhoto.get()) {
            return;
        }
        frame_count ++;
        detectCamera.set(true);

        bitmapsrc = imageToBitmap(image);  // ????????????
        //resizedBitmap = Bitmap.createScaledBitmap(bitmapsrc,  viewFinder.getWidth(), viewFinder.getHeight()+1, true);

        if (detectService == null) {
            detectCamera.set(false);
            return;
        }
        try {
            if(USE_DEBUG_PHOTO_ID>0){
                String filename = "0.jpg";
                if(USE_DEBUG_PHOTO_ID>0){
                    filename = +(USE_DEBUG_PHOTO_ID) + ".jpg";
                }

                InputStream open = getAssets().open(filename);
                bitmapsrc = BitmapFactory.decodeStream(open);
            }

            if(OBJECT_DETECION>0){
                String object_filename = "0.jpg";
                if(OBJECT_DETECION>0){
                    object_filename = "obj_image/"+(OBJECT_DETECION) + ".jpg";
                }
                InputStream open = getAssets().open(object_filename);
                bitmapsrc = BitmapFactory.decodeStream(open);
            }

            if(LINE_DETECTION>0){
                String line_filename = "0.jpg";
                if(LINE_DETECTION>0){
                    line_filename = "300ms/"+(LINE_DETECTION) + ".jpg"; //??????
                }
                InputStream open = getAssets().open(line_filename);
                bitmapsrc = BitmapFactory.decodeStream(open);
            }
        }catch (Exception e){}

        detectService.execute(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                current_rotation_degree = rotationDegrees;
                width = bitmapsrc.getWidth();
                height = bitmapsrc.getHeight();

                //Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmapsrc,  0,2,width/2+160, height-2,null, false); **check
                Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmapsrc,  0,2,width, height-2,null, false);
                if(ultra_fast_mode) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //kjk1020
                            //?????? ??????
                            detectAndDraw(resizedBitmap1, bitmapsrc, frame_count);
                            showResultOnUI();
                        }
                    }).start();
                }else{
                    //kjk1020
                    //detectAndDraw(resizedBitmap1, lane_Bitmap, frame_count);
                    detectAndDraw(resizedBitmap1, bitmapsrc, frame_count);
                    showResultOnUI();
                }
//                if(rotationDegrees == 90){
//                    matrix.postRotate(90);
//                    width = bitmapsrc.getWidth();
//                    height = bitmapsrc.getHeight();
//                    Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, width, height, matrix, true);
//                    //Log.d("detectOnModel","rotationDegrees:"+rotationDegrees+" width:"+width+ " after width:"+bitmap.getWidth()+ " height:"+bitmap.getHeight());
//                    detectAndDraw(bitmap,null,frame_count);
//                    showResultOnUI();
//                    toast_msg("rotation 90");
//                }else {
//
//                    width = bitmapsrc.getWidth();
//                    height = bitmapsrc.getHeight();
//
//                    //Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmapsrc,  0,2,width/2+160, height-2,null, false); **check
//                    Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmapsrc,  0,2,width, height-2,null, false);
//                    if(ultra_fast_mode) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //kjk1020
//                                //?????? ??????
//                                detectAndDraw(resizedBitmap1, bitmapsrc, frame_count);
//                                showResultOnUI();
//                            }
//                        }).start();
//                    }else{
//                        //kjk1020
//                        //detectAndDraw(resizedBitmap1, lane_Bitmap, frame_count);
//                        detectAndDraw(resizedBitmap1, bitmapsrc, frame_count);
//                        showResultOnUI();
//                    }
//
//                }
            }
        });
    }

    protected void showResultOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultImageView.setImageBitmap(mutableBitmap);
                endTime = System.currentTimeMillis();
                long dur = endTime - startTime;
                startTime = endTime;
                float fps = (float) (1000.0 / dur);
                if(recent_fps <0.1){
                    recent_fps = fps;
                }else {
                    recent_fps = 0.95f * recent_fps + 0.05f * fps;
                }
                total_fps = (total_fps == 0) ? fps : (total_fps + fps);
                fps_count++;

                String modelName = getModelName();
                DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA);
                DateFormat df8 = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.KOREA);
                String date2 = df2.format(new Date());
                String time4 = df8.format(new Date());
                BatteryManager manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int battery_current =manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW); //BATTERY_PROPERTY_CURRENT_NOW
                int battery_persent = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if(Math.abs(battery_current)>4000){
                    battery_current = battery_current / 1000;
                }
//                String outmsg=String.format(Locale.CHINESE,
//                        // ?????????%.1f ???
//                        "???????????? ????????????\nV%s\n%s\n?????????: %dx%d\n?????? ??????: \n%.3f s\nFPS: %.3f\n????????????%d %%\n?????????%d mA\n?????? ?????? ?????????%.1f m/s\n?????????%.1f m\n\n",
//                        app_version,date2+"\n"+time4, width,height, dur / 1000.0, recent_fps,battery_persent,battery_current,near_object_speed,near_object_distance);
//                //(float) total_fps / fps_count
//                outmsg += detect_msg;
//                tvInfo.setText(outmsg);
                String img_size_width = String.valueOf(width);
                String img_size_height = String.valueOf(height);
                if(check_video_line == true || check_video_object ==true){
                    test_video_pic.performClick();
                }

                date.setText(date2+" "+time4);
                appName.setText(app_V);
                image_size.setText(img_size_width+" X "+img_size_height);
                fpsTextview.setText(""+recent_fps);
                elec_value_Textview.setText(battery_persent+" %");
                ele_value.setText(battery_current+" mA");
                crash_text.setText("( " + near_label +" )"+"   " +crash_pre_time + "???");
                Runtime.getRuntime().gc();
                long total_ram = Runtime.getRuntime().totalMemory();
                long use_ram = Runtime.getRuntime().freeMemory();
                double d_total_ram = (double) total_ram;
                double d_use_ram = (double) use_ram;
                String ram_memory = String.format("%.0f",d_use_ram / d_total_ram * 100);
                serial_text.setText(ram_memory + " %");
                txt_write(time4+"--"+time_value);
//                System.out.println(getDate(82233213123L, "dd/MM/yyyy hh:mm:ss.SSS"));
            }
        });
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    protected Bitmap drawBoxRects(Bitmap mutableBitmap, Box[] results,int lane_offset,int frame_count) {

        Canvas canvas ;
        try {
            canvas = new Canvas(mutableBitmap);
        }catch (Exception e){
            return resizedBitmap;
        }

        Distance distance = new Distance(canvas,results,lane_offset);
        Object_Distance object_distance = new Object_Distance();
        SafeDetect.safe_region_param sp = get_safe_zone_detect(mutableBitmap.getWidth(),mutableBitmap.getHeight());
        msafeDetect.safeDetect(canvas,results,this,sp,distance,lane_offset,frame_count);

        if (!view_setting_lines &&(  results == null || results.length <= 0)) {
            return mutableBitmap;
        }
        //?????? ????????? ??????
        float canvas_width = canvas.getWidth();
        float canvas_height = canvas.getHeight();


        float lane_scaleX = canvas_width /800;
        float virtual_height = canvas_width * 288 / 800;
        float lane_scaleY = 2.5f;


        float startY = (canvas_height - virtual_height) /2;
        //System.out.println("startY " +startY);

        float horizon_line_Y = startY + virtual_height*(121)/288;
        float horizon_line_Y_second = horizon_line_Y +45;

        final Paint boxPaint = new Paint();
        final Paint boxPaint1 = new Paint();
        final Paint boxPaint2 = new Paint();

        //????????? ????????? ????????? ?????????
        if(view_setting_lines){
            boxPaint.setAlpha(200);
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 1200.0f);
            boxPaint.setColor(Color.argb(255,20,255,10));
            boxPaint1.setAlpha(200);
            boxPaint1.setStyle(Paint.Style.STROKE);
            boxPaint1.setStrokeWidth(4 * mutableBitmap.getWidth() / 1200.0f);
            boxPaint1.setColor(Color.argb(255,217,65,197));
            boxPaint2.setAlpha(200);
            boxPaint2.setStyle(Paint.Style.STROKE);
            boxPaint2.setStrokeWidth(4 * mutableBitmap.getWidth() / 1200.0f);
            boxPaint2.setColor(Color.argb(255,242,150,97));

            //???????????????
//            canvas.drawLine(0 ,horizon_line_Y ,bitmapsrc.getWidth() , horizon_line_Y,boxPaint2);
//            canvas.drawLine(0 ,horizon_line_Y_second ,bitmapsrc.getWidth() , horizon_line_Y_second,boxPaint);
//            //horizon_line_y = 322.2

            //canvas.drawLine(0 ,horizon_line_Y*2 ,bitmapsrc.getWidth() , horizon_line_Y*2,boxPaint);
            //canvas.drawRect(new RectF( 0 ,horizon_line_Y ,bitmapsrc.getWidth() , horizon_line_Y), boxPaint);
//            canvas.drawRect(new RectF( (float)bitmapsrc.getWidth()/2 ,0 ,(float)bitmapsrc.getWidth()/2 ,
//                    (float)bitmapsrc.getHeight()+1), boxPaint);
        }

        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2 * mutableBitmap.getWidth() / 800.0f);
        boxPaint.setTextSize(18 * mutableBitmap.getWidth() / 800.0f);

        //Object_detection_line,         //jun_drive_line
//        canvas.drawLine(canvas_width/2, canvas_height,0,0,boxPaint);

        object_distance.Distance(horizon_line_Y,horizon_line_Y_second,sp.safe_region_y[13],sp.safe_region_y[5],canvas_height);
        //20211223_?????????
        int left_x1=-1;      //Lookahead Distance ?????? ?????? x??????
        int left_x2=-1;      //?????? ?????? ?????? x??????
        int right_x1=-1;     //Lookahead Distance ????????? ?????? x??????
        int right_x2=-1;     //?????? ????????? ?????? x??????
        int x_offset = 0;
        int y_offset = 0;
        double angle_to_mid_radian = 0;
        int angle_to_mid_deg = 0;
        int steering_angle = 0;
        String text = "";
        cart_speed_value = (float) RPM_speed;


        for (Box box : results) {
            if (USE_MODEL == MOBILENETV2_YOLOV3_NANO) {
                if (box.getScore() < 0.15f) {
                    // ????????? ????????? ?????????, ???????????? ?????? ????????? ?????? ?????????
                    continue;
                }
                // ???????????? ??????
                box.x0 = box.x0 < 0 ? box.x0 / 9 : box.x0;
                box.y0 = box.y0 < 0 ? box.y0 / 9 : box.y0;
            }
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            RectF rect = box.getRect();
            if (box.label <1000){
                int label = box.label;
                if(label>28 && !( label==56 || label==57 ) ){
                    continue;
                }

                //?????? ??????
                //distance_value : type --> float
                canvas.drawText(box.getLabel() + String.format(Locale.KOREA, " %d%%\n%2.1fm", (int)(box.getScore()*100),
                        object_distance.getDistance(box,results)),
//                        distance.getDistance(box,results,sp,canvas,lane_offset)),
                        box.x0 + 3, box.y0 + 30 * canvas_width / 1000.0f, boxPaint);
                rect = box.getRect();
                boxPaint.setStyle(Paint.Style.STROKE);
                //canvas.drawRect(new RectF(rect.left*scaleX ,rect.top*scaleY ,rect.right*scaleX ,
                //       rect.bottom*scaleY ), boxPaint);
                canvas.drawRect(rect, boxPaint);
                //????????? ??????

                if (object_distance.getDistance(box,results) != 0.0){
                    distance_list.add(object_distance.getDistance(box,results));
                    label_list.add(object_distance.getDistanceLabel(box));
                }
                safe_line_1 = sp.safe_region_y[13];
                //???????????????_??????
                if(box.getLabel() == "person"){
                    test_vlaue_person = test_vlaue_person+1;
                    person_y1[person_box_count]=box.y1;
                    person_box_count++;
                }
                canvas.drawText("( " + near_label +" )"+"   " +crash_pre_time + "???",10,60,boxPaint);        //kjk ???????????? ???????????? ?????? ?????????

            }else{
                if(box.score<=0){
                    continue;
                }

                rect = box.getRect();

                rect.left *= lane_scaleX;
                rect.top *= lane_scaleY;
//                rect.top *= lane_scaleY;
//                rect.top += startY;
                switch (box.label) {
                    case 1000:
                        boxPaint.setColor(Color.argb(255, 255, 255, 255));
                        break;
                    case 1001:
                        boxPaint.setColor(Color.argb(255, 255, 100, 100));
                        break;
                    //20211223_?????????
                    case 1002:
                        boxPaint.setColor(Color.argb(255, 20, 255, 10));
                        left_x1 = (int) rect.left;
                        break;
                    case 1003:
                        boxPaint.setColor(Color.argb(255, 10, 100, 255));
                        right_x1 = (int) rect.left;
                        break;
                    case 1004:
                        boxPaint.setColor(Color.argb(255, 100, 100, 255));
                        left_x2 = (int) rect.left;
                        break;
                    case 1005:
                        boxPaint.setColor(Color.argb(255, 100, 100, 255));
                        right_x2 = (int) rect.left;
                        break;
                    //20211223_?????????
                }
                canvas.drawCircle(rect.left,rect.top,3.5f * mutableBitmap.getWidth() / 800.0f, boxPaint);
            }
        }

        //canvas.drawText("( " + near_label +" )"+"   " +crash_pre_time + "???",10,60,boxPaint);

        boxPaint.setColor(Color.argb(255, 255, 0, 0));
        if (left_x1 != -1 && right_x1 != -1) {
            x_offset = (left_x1 + right_x1) / 2 - (int) (canvas_width / 2);
        }
        else if (left_x1 != -1)
            x_offset = (left_x1-left_x2);
        else if (right_x1 != -1)
            x_offset = (right_x1-right_x2);
        else
            x_offset = 0;
        y_offset = (int)canvas_height/2 + 48;
        angle_to_mid_radian = Math.atan2((double)x_offset, (double)y_offset);
        angle_to_mid_deg = (int)(angle_to_mid_radian * 180.0 / Math.PI);
        steering_angle = angle_to_mid_deg + 90;
        //text = "Steering angle : ".concat(Integer.toString(angle_to_mid_deg));
        steering_angle_text.setText("".concat(Double.toString(angle_to_mid_radian))); //??????
//        text = "Steering angle : ".concat(Double.toString(angle_to_mid_radian));
//        canvas.drawText(text,50, 100, boxPaint);
        canvas.drawLine(canvas_width/2, canvas_height,(int)((canvas_width/2) - canvas_height/2/Math.tan((float)steering_angle/180.0 * Math.PI)),y_offset,boxPaint);
        //20211223_?????????
        //??????????????? ????????? ????????? ?????? ????????????
        if (serial_check == true){
            if(angle_to_mid_radian >0)
                angle_speed = String.format("-%.3f",Math.abs(angle_to_mid_radian));
            else
                angle_speed = String.format("+%.3f",Math.abs(angle_to_mid_radian));
            String RPM_speed_str = String.format("+%.3f",RPM_speed);
            if(serial_btn_check == false && detect_danger_person == false) {
//                send_serial(RPM_speed_str, angle_speed); ////????????????

            }
        }

        return mutableBitmap;
    }

    //?????? ?????? ??? ?????? ?????? ?????? ????????? ??????
//    protected void set_input_image(Bitmap image,Bitmap image2){
//        if(view_setting_lines){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    //iv_lane_input.setImageBitmap(image2);
//                }
//            });
//        }
//
//    }

    protected Bitmap detectAndDraw(Bitmap image,Bitmap image2,int frame_count) {
        Box[] result = null;
        Box[] result1 = null;
        Box[] result2= null;

        KeyPoint[] keyPoints = null;
        YolactMask[] yolactMasks = null;
        FaceKeyPoint[] faceKeyPoints = null;
        float[] enetMasks = null;
        if (check_photo == true){
             if (object_check == true) {
                result = NcnnYolox.detect(image, threshold, nms_threshold);
//          System.out.println("?????? ??????");
            } else if (object_check == false) {
                //System.out.println("Object OFF");
            }
        }else{
            if((!ultra_fast_mode || ultra_fast_mode && ((road_type !=3 && frame_count %3 == 0) ||(road_type ==3 && frame_count%2 == 0)))) {
                if (object_check == true) {
                    result = NcnnYolox.detect(image, threshold, nms_threshold);
//          System.out.println("?????? ??????");
                } else if (object_check == false) {
                    //System.out.println("Object OFF");
                }
            }
        }

        if (check_photo == true){
            if (line_check == true) {
                result2 = LSTR.detect(image2, threshold, lane_nms_threshold);
                lane_result = result2;
            } else if (line_check == false) {
                result2 = null;
                lane_result = null;
                //System.out.println("?????? ?????? ??????");
            }
        }else{
            if ( (!ultra_fast_mode || ultra_fast_mode && frame_count % 3 == 2) ){
                if (line_check == true) {
                    result2 = LSTR.detect(image2, threshold, lane_nms_threshold);
                    check_photo = false;
                    lane_result = result2;
                } else if (line_check == false) {
                    result2 = null;
                    lane_result = null;
                    //System.out.println("?????? ?????? ??????");
                }
            }
        }
        if (line_check == false && object_check == false){
//            mutableBitmap = bitmapsrc;
            mutableBitmap = image;
        }
//        detectYolov4.set(false);
        //detect_full_result = result;
            //more far
//            if(advanced_func_key!=0 && far_enhanced_detect &&
//                    ( !ultra_fast_mode || ultra_fast_mode &&((road_type !=3 /*&& frame_count %3 == 1*/ ) || (road_type ==3 && frame_count %2 == 1)))
//            ) {
//                float cut_scale = 8.f;
//                if (image.getWidth() <= 640){
//                    cut_scale = 6.0f;
//                }
//                int small_box_half_width = (int)((float)image.getWidth() /cut_scale);
//                int startX = (int)((float)image.getWidth() /2 - small_box_half_width) + (int)direction_center_x_offset;
//                int startY = (int)((float)image.getHeight()/2 - small_box_half_width);
//                // < 320 to scale with filter
//                boolean filter = small_box_half_width *2 < 320;
//                Bitmap image1 = Bitmap.createBitmap(image, startX,startY,small_box_half_width*2,small_box_half_width*2,null,filter);
//                if (USE_YOLOV4_DETECT == 0){
//                    result1 = NcnnYolox.detect(image1, threshold, nms_threshold);
//                    //result = NanoDet.detect(image, threshold, nms_threshold);
//                }else if (USE_YOLOV4_DETECT <= 3) {
//                    result1 = YOLOv4.detect(image1, threshold, nms_threshold);
//                } else if (USE_YOLOV4_DETECT == 4){
//                    result1 = YOLOv5.detect(image1, threshold, nms_threshold);
//                    //result = NanoDet.detect(image, threshold, nms_threshold);
//                }
//                detectYolov4.set(false);
//                if(result1!=null) {
//                    for (int i = 0; i < result1.length; i++) {
//                        result1[i].x0 += startX;
//                        result1[i].y0 += startY;
//                        result1[i].x1 += startX;
//                        result1[i].y1 += startY;
//                    }
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        iv_detect_input.setImageBitmap(image1);
//                    }
//                });
//                detect_far_result = result1;
//            }else{
//                result1 = detect_far_result;
//            }
//
//            if(road_type == 3) {
//                lane_result = null;
//                result2 = null;
//            }else {
//
//                if ( (!ultra_fast_mode || ultra_fast_mode && frame_count % 3 == 2)
//                ) {
////                    if(line_check == true){
////                        result2 = LSTR.detect(image2, threshold, lane_nms_threshold);
////                        lane_result = result2;
////                    }else if (line_check == false){
////                        result2=null;
////                        lane_result = null;
////                        System.out.println("?????? ?????? ??????");
////                    }
//
//                } else {
//                    if(line_check == true) {
//                        result2 = lane_result;
//                    }else{
//                        result2=null;
//                        lane_result = null;
//                    }
//
//                }
//            }

        if (!view_setting_lines && result == null  && result1 == null && result2==null && keyPoints == null && yolactMasks == null && enetMasks == null && faceKeyPoints == null) {
            detectCamera.set(false);
            return image;
        }
//        if (USE_MODEL == YOLOV5S || USE_MODEL == YOLOV4_TINY || USE_MODEL == MOBILENETV2_YOLOV3_NANO
//                || USE_MODEL == YOLOV5_CUSTOM_LAYER || USE_MODEL == NANODET || USE_MODEL == YOLO_FASTEST_XL) {
//            mutableBitmap = drawBoxRects(image, result,0,frame_count);
//
//        }
//        else if (USE_MODEL == SIMPLE_POSE) {
//            mutableBitmap = drawPersonPose(image, keyPoints);
//        }
        else if (USE_MODEL == LANE_LSTR) {
            int all_count =0;
            if(result!=null){
                all_count += result.length;
            }
            if(result1!=null){
                all_count += result1.length;
            }
            if(result2!=null){
                all_count += result2.length;
            }

            Box[] result_all = new Box[all_count];
            int result_all_i = 0;
            if(result!=null) {
                for (int i = 0; i < result.length; i++) {
                    result_all[result_all_i] = result[i];
                    result_all_i ++;
                }
            }

            if(result1!=null) {
                for (int i = 0; i < result1.length; i++) {
                    result_all[result_all_i] = result1[i];
                    result_all_i ++;
                }
            }
            int lane_offset = result_all_i;
            if(result2!=null) {
                for (int i = 0; i < result2.length; i++) {
                    result_all[result_all_i] = result2[i];
                    result_all_i ++;
                }
            }
            detectCamera.set(false);

            for (int i=0; i<test_vlaue_person; i++){
                if(safe_line_1<person_y1[i]){
                    detect_danger_person = true;
                    break;
                }else{
                    detect_danger_person = false;
                }
            }
            if (detect_danger_person == true){
                //cart_stop
//                send_serial("+0.000", "+0.000");
//                System.out.println("?????? ?????? ????????????");
            }

            if(test_vlaue_person ==0 || detect_danger_person == false){
                detect_danger_person = false;
            }
            for (int i=0; i<test_vlaue_person; i++){
                person_y1[i] = 0.0f;
            }

            test_vlaue_person = 0;
            person_box_count = 0;

            mutableBitmap = drawBoxRects(image, result_all,lane_offset,frame_count);
            crash_prediction_time();

            if (crash_pre_time != 0.0 && crash_pre_time < 3.6){
//                send_serial("+0.000", "+0.000");
//                save_img(mutableBitmap);
//                System.out.println("?????? ?????? ?????? ?????? ?????????");
            }

        }
        return mutableBitmap;
    }

    //Write_txt
    public void txt_write(String value){
        txt_count_value++;
        String file_path = Environment.getExternalStorageDirectory()+"/Documents/test_reuslt";
        try{
            File dir = new File (file_path);
            if(!dir.exists()){
                dir.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(file_path+"/test_1.txt", true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(value+"\n");
            writer.flush();

            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void crash_prediction_time(){

        int near_distance_index = 0;
        float near_distance = 0.F;

        if(!distance_list.isEmpty()) {
            near_distance = Collections.min(distance_list);
//            System.out.println(near_distance/cart_speed_value);
//19.40
            time_value = String.format("%.1f", near_distance/cart_speed_value);
            crash_pre_time = Float.valueOf(time_value);

            for(int i = 0 ; i < distance_list.size(); i++){
                if (distance_list.get(i) == near_distance){
                    near_distance_index = i;
                }
            }
            near_label = label_list.get(near_distance_index);
        }else {
            crash_pre_time = 0.F;
            near_label = "";
        }
        distance_list.clear();
        label_list.clear();
    }

    protected String getModelName() {
        String modelName = "ohhhhh";
        if (USE_MODEL == YOLOV5S) {
            modelName = "YOLOv5s";
        } else if (USE_MODEL == YOLOV4_TINY) {
            modelName = "YOLOv4-tiny";
        } else if (USE_MODEL == MOBILENETV2_YOLOV3_NANO) {
            modelName = "MobileNetV2-YOLOv3-Nano";
        } else if (USE_MODEL == SIMPLE_POSE) {
            modelName = "Simple-Pose";
        } else if (USE_MODEL == YOLACT) {
            modelName = "Yolact";
        } else if (USE_MODEL == ENET) {
            modelName = "ENet";
        } else if (USE_MODEL == FACE_LANDMARK) {
            modelName = "YoloFace500k-landmark106";
        } else if (USE_MODEL == DBFACE) {
            modelName = "DBFace";
        } else if (USE_MODEL == MOBILENETV2_FCN) {
            modelName = "MobileNetV2-FCN";
        } else if (USE_MODEL == MOBILENETV3_SEG) {
            modelName = "MBNV3-Segmentation-small";
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            modelName = "YOLOv5s_Custom_Layer";
        } else if (USE_MODEL == NANODET) {
            modelName = "NanoDet";
        } else if (USE_MODEL == YOLO_FASTEST_XL) {
            modelName = "YOLO-Fastest-xl";
        }else if (USE_MODEL == LANE_LSTR) {
            modelName = "LANE_LSTR";
        }
        return USE_GPU ? "[ GPU ] " + modelName : "[ CPU ] " + modelName;
    }

    @Override
    protected void onDestroy() {
        detectCamera.set(false);
        if (detectService != null) {
            detectService.shutdown();
            detectService = null;
        }
        if (mmr != null) {
            mmr.release();
        }
        CameraX.unbindAll();
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "???????????? ?????? ????????? ????????? ????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_PICK_VIDEO) {
            // photo
            runByPhoto(requestCode, resultCode, data);
        }
//        } else if (requestCode == REQUEST_PICK_IMAGE) {
//            // video
//            Uri uri = data.getData();
//            path_address = getPath(uri);
//            detectOnVideo(path_address);
//        }
             Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    private String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Video
    public void detectOnVideo(final String path) {
        if (detectVideo.get()) {
            Toast.makeText(this, "Video is running", Toast.LENGTH_SHORT).show();
            return;
        }
        detectVideo.set(true);
        Toast.makeText(MainActivity.this, "FPS is not accurate!", Toast.LENGTH_SHORT).show();
        CameraX.unbindAll();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(path);
                String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);  // ms
                String sfps = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);  // fps
//                String sWidth = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);  // w
//                String sHeight = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);  // h
                String rota = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);  // rotation
                int duration = Integer.parseInt(dur);
                System.out.println(dur);
                float fps = Float.parseFloat(sfps);
                float rotate = 0;
                if (rota != null) {
                    rotate = Float.parseFloat(rota);
                }
//                sbVideo.setMax(duration * 1000);
                float frameDis = 1.0f / fps * 1000 * 1000 * videoSpeed;
                videoCurFrameLoc = 0;
                frame_count ++;

                while (detectVideo.get() && (videoCurFrameLoc) < (duration * 1000)) {
                    videoCurFrameLoc = (long) (videoCurFrameLoc + frameDis);
                    //sbVideo.setProgress((int) videoCurFrameLoc);
                    final Bitmap b = mmr.getFrameAtTime(videoCurFrameLoc, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

                    if (b == null) {
                        continue;
                    }
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotate);
                    width = b.getWidth();
                    height = b.getHeight();
                    final Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, false);

                    startTime = System.currentTimeMillis();
//                    detectAndDraw(bitmap.copy(ARGB_8888, true),bitmap.copy(ARGB_8888, true),frame_count);
//                    detectAndDraw(bitmap.copy(ARGB_8888, true),bitmap1,frame_count);

                    detectAndDraw(bitmap.copy(ARGB_8888, true),bitmap.copy(ARGB_8888,true),frame_count);
                    showResultOnUI();
                    frameDis = 1.0f / fps * 1000 * 1000 * videoSpeed;
                }
                mmr.release();

                if (detectVideo.get()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            sbVideo.setVisibility(View.GONE);
//                            sbVideoSpeed.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Video end!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                detectVideo.set(false);
            }
        }, "video detect");
        thread.start();
//        startCamera();
    }

    public void runByPhoto(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "Photo error", Toast.LENGTH_SHORT).show();
            return;
        }
        detectPhoto.set(true);
        final Bitmap image = getPicture(data.getData());
        System.out.println(data.getData());
        if (image == null) {
            Toast.makeText(this, "Photo is null", Toast.LENGTH_SHORT).show();
            return;
        }
        CameraX.unbindAll();
        frame_count ++;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                mutableBitmap = image.copy(ARGB_8888, true);
                width = image.getWidth();
                height = image.getHeight();

                mutableBitmap = detectAndDraw(mutableBitmap,null,frame_count);


                final long dur = System.currentTimeMillis() - start;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String modelName = getModelName();
                        resultImageView.setImageBitmap(mutableBitmap);
                        tvInfo.setText(String.format(Locale.KOREA, "%s\nSize: %dx%d\nTime: %.2f s\nFPS: %.1f",
                                modelName, height, width, dur / 1000.0, 1000.0f / dur));
                    }
                });
            }
        }, "photo detect");
        thread.start();
    }

    private void doSelectMovie()
    {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try
        {
            startActivityForResult(i, 2);
        } catch (android.content.ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        if (bitmap == null) {
            return null;
        }
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    @Override
    public void onBackPressed() {
        //App ?????? ????????? ??????
        if (System.currentTimeMillis() - last_press_back_time > 2000) {
            last_press_back_time = System.currentTimeMillis();
            view_setting_lines = false;
            view_setting();
            param_toggle = false;
        } else {
            super.onBackPressed();
        }
    }

    public void view_setting(){

        if(view_setting_lines){
            tvInfo.setVisibility(View.VISIBLE);
            iv_detect_input.setVisibility(View.GONE);
            iv_lane_input.setVisibility(View.GONE);
            mResultView.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.VISIBLE);
            Rec_video_btn.setVisibility(View.VISIBLE);
            btn_direction.setVisibility(View.VISIBLE);
            btnVideo.setVisibility(View.VISIBLE);
            btnController.setVisibility(View.VISIBLE);
            btnCamera.setVisibility(View.VISIBLE);
            camera_chage.setVisibility(View.VISIBLE);
            menubar_clear.setVisibility(View.VISIBLE);

            int permission = ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.CAMERA);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                btnCamera.setVisibility(View.VISIBLE);
            }

        }else{
            USE_DEBUG_PHOTO_ID=0;
            OBJECT_DETECION = 0;
            LINE_DETECTION=0;
            viewFinder.setVisibility(View.GONE);

            if(advanced_func_key!=0) {
                iv_detect_input.setVisibility(View.VISIBLE);
            }else {
                iv_detect_input.setVisibility(View.GONE);
            }
            iv_lane_input.setVisibility(View.GONE);
            mResultView.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.GONE);
            Rec_video_btn.setVisibility(View.GONE);
            btn_direction.setVisibility(View.GONE);
            btnVideo.setVisibility(View.GONE);
            btnController.setVisibility(View.GONE);
            btnCamera.setVisibility(View.GONE);
            camera_chage.setVisibility(View.GONE);
            menubar_clear.setVisibility(View.GONE);
        }

    }

    public void init_param(){
        SharedPreferences sharedPreferences = getSharedPreferences("adas", MODE_PRIVATE);
        threshold = sharedPreferences.getFloat("threshold", (float)threshold);
        nms_threshold = sharedPreferences.getFloat("nms_threshold", (float)nms_threshold);
        LaneDetectHeight = sharedPreferences.getFloat("LaneDetectHeight", (float)LaneDetectHeight);
        front_detect = sharedPreferences.getFloat("front_detect", (float)front_detect);
        alarm_wait_time = (int)sharedPreferences.getFloat("alarm_wait_time", (float)alarm_wait_time);
        advanced_func_key = sharedPreferences.getLong("advanced_func_key", advanced_func_key);
        long key_create_time1 = new Date().getTime();
        key_create_time = sharedPreferences.getLong("key_create_time", key_create_time1);
        if(key_create_time1 == key_create_time ){
            SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
            SharedPreferences.Editor editor = userInfo.edit();
            editor.putLong("key_create_time", key_create_time1);
            editor.apply();
        }
        //kjk1020
        DetectWidth = sharedPreferences.getFloat("DetectWidth", DetectWidth);
        alarm_mode = sharedPreferences.getInt("alarm_mode", alarm_mode);
        road_type = sharedPreferences.getInt("road_type", road_type);
        auto_adjust_detect_area = sharedPreferences.getInt("auto_adjust_detect_area", auto_adjust_detect_area);
        person_detect_focus = sharedPreferences.getInt("person_detect_focus", person_detect_focus);
        openid = sharedPreferences.getString("openid", openid);
        deviceid = sharedPreferences.getString("deviceid", deviceid);
        USE_GPU = sharedPreferences.getBoolean("USE_GPU", USE_GPU);
        carmera_height = sharedPreferences.getFloat("carmera_height", carmera_height);
        distance_fix = sharedPreferences.getFloat("distance_fix", distance_fix);
        vertical_distance_rate = sharedPreferences.getFloat("vertical_distance_rate", vertical_distance_rate);
        hit_predict = sharedPreferences.getInt("hit_predict_i", hit_predict);

        ultra_fast_mode= sharedPreferences.getBoolean("ultra_fast_mode", ultra_fast_mode);
        if(deviceid.equals("")){
            long deviceid_gen = ((new Date().getTime())) << 16 |  (SystemClock.elapsedRealtime()&0xFFFF);
            deviceid = String.valueOf(deviceid_gen);
            SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
            SharedPreferences.Editor editor = userInfo.edit();
            editor.putString("deviceid", deviceid);
            editor.apply();
        }
    }
}
