/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.googlyeyes;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.session.MediaController;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.googlyeyes.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.googlyeyes.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.Toast.LENGTH_SHORT;



/**
 * Activity for Googly Eyes, an app that uses the camera to track faces and superimpose Googly Eyes
 * animated graphics over the eyes.  The app also detects whether the eyes are open or closed,
 * drawing the eyes in the correct state.<p>
 *
 * This app supports both a front facing mode and a rear facing mode, which demonstrate different
 * API functionality trade-offs:<p>
 *
 * Front facing mode uses the device's front facing camera to track one user, in a "selfie" fashion.
 * The settings for the face detector and its associated processing pipeline are set to optimize for
 * the single face case, where the face is relatively large.  These factors allow the face detector
 * to be faster and more responsive to quick motion.<p>
 *
 * Rear facing mode uses the device's rear facing camera to track any number of faces.  The settings
 * for the face detector and its associated processing pipeline support finding multiple faces, and
 * attempt to find smaller faces in comparison to the front facing mode.  But since this requires
 * more scanning at finer levels of detail, rear facing mode may not be as responsive as front
 * facing mode.<p>
 */
public class GooglyEyesActivity extends AppCompatActivity //원래final 있었음
{
    private static final String TAG = "GooglyEyes";

    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    public View onebyone;
    public View onebytwo;
    public View onebythree;
    public View twobyone;
    public View twobytwo;
    public View twobythree;
    public View threebyone;
    public View threebytwo;
    public View threebythree;

    public int chosen_table_num = 11;                                                                 //초기값은 0으로 해놓기

    private boolean mIsFrontFacing = true;

    public PointF leftTop_L = null;
    public PointF leftTop_R = null;
    public PointF leftBottom_L = null;
    public PointF leftBottom_R = null;
    PointF rightTop_L = null;
    PointF rightTop_R = null;
    PointF rightBottom_L=null;
    PointF rightBottom_R=null;

    PointF current_L = null;



    Toast mToast = null; //전역변수로 선언 ①
    String mToastStr;    //전역변수로 선언 ②

    private int what = 0;
    private int count = 0;

    //remove 안된 message가 존재하는 경우 방지
    private boolean isStop = false;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) //해당 쓰레드의 메세지 큐에 메세지가 존재하는 경우 handleMessage() 메소드가 호출됨,
        {
            //Toast.makeText(getApplicationContext(), "Call Handler : " + count,LENGTH_SHORT).show();
            //여기다가 추가?

           // chosen_table_num=eye_point_now(leftTop_L);                                                //그냥왼쪽눈기준으로 해놨음 완성시킬 예정   // mTableOver.getId() -> id를 return 해줌
            //set_image_tile(chosen_table_num);

            /*switch( msg.what )
            {
            current_L = mGraphicOverlay.getstoreEyes_L();
            //if(current_L.x!=500) chosen_table_num=13;
            }*/

            if((!isStop))
            {
                mHandler.sendEmptyMessageDelayed(what, 100); // what은 구분자, select 문의 case 처럼  // delay 100 으로 send, 0.1 초에 한번 toast
                count++;
                //current_L = mGraphicOverlay.getstoreEyes_L();
                //chosen_table_num=eye_point_now(current_L);                                                //그냥왼쪽눈기준으로 해놨음 완성시킬 예정   // mTableOver.getId() -> id를 return 해줌
                chosen_table_num=eye_point_now(leftBottom_L);
                set_image_tile(chosen_table_num);
            }
            else
                return;
        }
    };

    // Activity Methods
    //==============================================================================================



    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.setContentView(R.layout.main);// main.xml을 화면에 보여준다.//첫번째에 메인을 깔고

        mHandler.sendEmptyMessageDelayed(what, 100);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);       // 화면을 landscape(가로) 화면으로 고정하고 싶은 경우

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        onebyone = findViewById(R.id.one_one);
        onebytwo=findViewById(R.id.one_two);
        onebythree=findViewById(R.id.one_three);
        twobyone=findViewById(R.id.two_one);
        twobytwo=findViewById(R.id.two_two);
        twobythree=findViewById(R.id.two_three);
        threebyone=findViewById(R.id.three_one);
        threebytwo=findViewById(R.id.three_two);
        threebythree=findViewById(R.id.three_three);



        if (savedInstanceState != null)
        {
            mIsFrontFacing = savedInstanceState.getBoolean("IsFrontFacing");
        }
        // Check for the camera permission before accessing the camera.  If the permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED)
        {
            createCameraSource();

        }
        else
        {
            requestCameraPermission();
        }

        final Button button2 = (Button) findViewById(R.id.button2);
        // 버튼의 클릭이벤트를 처리하기 위해 클릭리스너를 버튼에 등록해준다.
        button2.setOnClickListener(new View.OnClickListener()
        {
            // 파라미터로 넘어오는 View는 현재 클된 View이다. 현재 클릭된 View는 button이다.
            public void onClick(View v) {
                leftTop_L = mGraphicOverlay.getstoreEyes_L();
                leftTop_L.x=(-1)*(leftTop_L.x);
                leftTop_L.y=(-1)*(leftTop_L.y);

                leftTop_R = mGraphicOverlay.getstoreEyes_R();
                leftTop_R.x=(-1)*(leftTop_R.x);
                leftTop_R.y=(-1)*(leftTop_R.y);

                mToastStr = "Left eye of Left Top is X =" + leftTop_L.x + " Y = " + leftTop_L.y+ "\n Right eye of Left Top is X =" + leftTop_R.x + " Y = " + leftTop_R.y; //출력할 메시지를 mToastStr에 작성 ⑤
                toastLong();
                //toastShort(); //짧은 Toast 메시지 출력 ⑥
            }
        });

        final Button button3 = (Button) findViewById(R.id.button3);//
        // 버튼의 클릭이벤트를 처리하기 위해 클릭리스너를 버튼에 등록해준다.
        button3.setOnClickListener(new View.OnClickListener()
        {
            // 파라미터로 넘어오는 View는 현재 클된 View이다. 현재 클릭된 View는 button이다.
            public void onClick(View v) {
                leftBottom_L = mGraphicOverlay.getstoreEyes_L();
                leftBottom_L.x=(-1)*(leftBottom_L.x);
                leftBottom_L.y=(-1)*(leftBottom_L.y);

                leftBottom_R = mGraphicOverlay.getstoreEyes_R();
                leftBottom_R.x=(-1)*(leftBottom_R.x);
                leftBottom_R.y=(-1)*(leftBottom_R.y);

                mToastStr = "Left eye of Left Bottom is X =" + leftBottom_L.x + " Y = " + leftBottom_L.y+ "\n Right eye of Left Bottom is X =" + leftBottom_R.x + " Y = " + leftBottom_R.y; //출력할 메시지를 mToastStr에 작성 ⑤
                toastLong();
                //toastShort(); //짧은 Toast 메시지 출력 ⑥
            }
        });


        final Button button4 = (Button) findViewById(R.id.button4);//
        // 버튼의 클릭이벤트를 처리하기 위해 클릭리스너를 버튼에 등록해준다.
        button4.setOnClickListener(new View.OnClickListener()
        {
            // 파라미터로 넘어오는 View는 현재 클된 View이다. 현재 클릭된 View는 button이다.
            public void onClick(View v) {
                rightTop_L = mGraphicOverlay.getstoreEyes_L();
                rightTop_L.x=(-1)*( rightTop_L.x);
                rightTop_L.y=(-1)*( rightTop_L.y);

                rightTop_R = mGraphicOverlay.getstoreEyes_R();
                rightTop_R.x=(-1)*( rightTop_R.x);
                rightTop_R.y=(-1)*( rightTop_R.y);

                mToastStr = "Left eye of Right Top is X =" +  rightTop_L.x + " Y = " +  rightTop_L.y+ "\n Right eye of Right Top is X =" +  rightTop_R.x + " Y = " +  rightTop_R.y; //출력할 메시지를 mToastStr에 작성 ⑤
                toastLong();
                // toastShort(); //짧은 Toast 메시지 출력 ⑥
            }
        });


        final Button button5 = (Button) findViewById(R.id.button5);//
        // 버튼의 클릭이벤트를 처리하기 위해 클릭리스너를 버튼에 등록해준다.
        button5.setOnClickListener(new View.OnClickListener()
        {
            // 파라미터로 넘어오는 View는 현재 클된 View이다. 현재 클릭된 View는 button이다.
            public void onClick(View v) {
                rightBottom_L = mGraphicOverlay.getstoreEyes_L();
                rightBottom_L.x=(-1)*( rightBottom_L.x);
                rightBottom_L.y=(-1)*( rightBottom_L.y);

                rightBottom_R = mGraphicOverlay.getstoreEyes_R();
                rightBottom_R.x=(-1)*( rightBottom_R.x);
                rightBottom_R.y=(-1)*( rightBottom_R.y);

                mToastStr = "Left eye of Right Bottom is X =" +  rightBottom_L.x + " Y = " +  rightBottom_L.y+ "\n Right eye of Right Bottom is X =" +  rightBottom_R.x + " Y = " +  rightBottom_R.y; //출력할 메시지를 mToastStr에 작성 ⑤
                toastLong();
                //toastShort(); //짧은 Toast 메시지 출력 ⑥
            }
        });







        /**
         * 좌표를 받아서, 값을 출력하면 그 좌표값에 따라서 투명도 바꾸기
         */

        //chosen_table_num=eye_point_now(leftTop_L);                                                //그냥왼쪽눈기준으로 해놨음 완성시킬 예정   // mTableOver.getId() -> id를 return 해줌
        //Toast toast = Toast.makeText(this, "메세지 입력." + chosen_table_num,Toast.LENGTH_SHORT);
        //toast.show();

        //chosen_table_num=11;
        //set_image_tile(chosen_table_num);                                                               //됐다안됐다함

    }



    void toastShort() { //③
        if(mToast == null) {
            mToast = Toast.makeText(GooglyEyesActivity.this, mToastStr, LENGTH_SHORT);
        } else {
            mToast.setText(mToastStr);
        }
        mToast.show();
    }

    void toastLong() { //④
        if(mToast == null) {
            mToast = Toast.makeText(GooglyEyesActivity.this, mToastStr, Toast.LENGTH_LONG);
        } else {
            mToast.setText(mToastStr);
        }
        mToast.show();
    }

    /**
     * Handles the requesting of the camera permission.  This includes showing a "Snackbar" message
     * of why the permission is needed then sending the request.
     */
    private void requestCameraPermission()
    {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }
    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    /*
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }*/


    @Override
    protected void onPause() {
        if(mHandler.hasMessages(what)){
            mHandler.removeMessages(what);
            isStop = true;
        }
        super.onPause();
        mPreview.stop();
    }


    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }


    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on
     * {@link #requestPermissions(String[], int)}.<p>
     *
     * <strong>Note:</strong> It is possible that the permissions request interaction with the user
     * is interrupted. In this case you will receive empty permissions and results arrays which
     * should be treated as a cancellation.<p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // UI
    //==============================================================================================

    /**
     * Saves the camera facing mode, so that it can be restored after the device is rotated.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("IsFrontFacing", mIsFrontFacing);
    }

    //==============================================================================================
    // Detector
    //==============================================================================================

    /**
     * Creates the face detector and associated processing pipeline to support either front facing
     * mode or rear facing mode.  Checks if the detector is ready to use, and displays a low storage
     * warning if it was not possible to download the face library.
     */
    @NonNull
    private FaceDetector createFaceDetector(Context context)
    {
        // For both front facing and rear facing modes, the detector is initialized to do landmark
        // detection (to find the eyes), classification (to determine if the eyes are open), and
        // tracking.
        //
        // Use of "fast mode" enables faster detection for frontward faces, at the expense of not
        // attempting to detect faces at more varied angles (e.g., faces in profile).  Therefore,
        // faces that are turned too far won't be detected under fast mode.
        //
        // For front facing mode only, the detector will use the "prominent face only" setting,
        // which is optimized for tracking a single relatively large face.  This setting allows the
        // detector to take some shortcuts to make tracking faster, at the expense of not being able
        // to track multiple faces.
        //
        // Setting the minimum face size not only controls how large faces must be in order to be
        // detected, it also affects performance.  Since it takes longer to scan for smaller faces,
        // we increase the minimum face size for the rear facing mode a little bit in order to make
        // tracking faster (at the expense of missing smaller faces).  But this optimization is less
        // important for the front facing case, because when "prominent face only" is enabled, the
        // detector stops scanning for faces after it has found the first (large) face.
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(mIsFrontFacing)
                .setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
                .build();

        Detector.Processor<Face> processor;
        if (mIsFrontFacing) {
            // For front facing mode, a single tracker instance is used with an associated focusing
            // processor.  This configuration allows the face detector to take some shortcuts to
            // speed up detection, in that it can quit after finding a single face and can assume
            // that the nextIrisPosition face position is usually relatively close to the last seen
            // face position.
            Tracker<Face> tracker = new GooglyFaceTracker(mGraphicOverlay);
            processor = new LargestFaceFocusingProcessor.Builder(detector, tracker).build();
        } else {
            // For rear facing mode, a factory is used to create per-face tracker instances.  A
            // tracker is created for each face and is maintained as long as the same face is
            // visible, enabling per-face state to be maintained over time.  This is used to store
            // the iris position and velocity for each face independently, simulating the motion of
            // the eyes of any number of faces over time.
            //
            // Both the front facing mode and the rear facing mode use the same tracker
            // implementation, avoiding the need for any additional code.  The only difference
            // between these cases is the choice of Processor: one that is specialized for tracking
            // a single face or one that can handle multiple faces.  Here, we use MultiProcessor,
            // which is a standard component of the mobile vision API for managing multiple items.
            MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
                @Override
                public Tracker<Face> create(Face face) {
                    return new GooglyFaceTracker(mGraphicOverlay);
                }
            };
            processor = new MultiProcessor.Builder<>(factory).build();
        }

        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
        return detector;
    }

    //==============================================================================================
    // Camera Source
    //==============================================================================================

    /**
     * Creates the face detector and the camera.
     */
    private void createCameraSource() //카메라 소스 받아서 빌드
    {
        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!mIsFrontFacing)
        {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        // The camera source is initialized to use either the front or rear facing camera.  We use a
        // relatively low resolution for the camera preview, since this is sufficient for this app
        // and the face detector will run faster at lower camera resolutions.
        //
        // However, note that there is a speed/accuracy trade-off with respect to choosing the
        // camera resolution.  The face detector will run faster with lower camera resolutions,
        // but may miss smaller faces, landmarks, or may not correctly detect eyes open/closed in
        // comparison to using higher camera resolutions.  If you have any of these issues, you may
        // want to increase the resolution.
        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */


    private void startCameraSource()
    {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS)
        {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null)
        {
            try
            {
                mPreview.start(mCameraSource, mGraphicOverlay);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    public int eye_point_now(PointF point) //eyepoint 관련함수                                               => 추가해야함
    {

        // Toast toast = Toast.makeText(this, "x 는" + point.x + "y 는 "+ point.y,Toast.LENGTH_SHORT);
        // toast.show();
        if(point == null)
            return 0;
        else if(point.x<point.y)
            return 11;   //여기에 eye가 바라보는 사분면 정보를 뽑아줘야 함                                                     >>>>> 동작안함
        //else if//(point.x+40<point.y-40)
          //  return 12;
        else
            return 13;
    }


    public void init_color() // 모든 framgment 의 visibility 초기화
    {
        onebyone.setVisibility(View.INVISIBLE);
        onebytwo.setVisibility(View.INVISIBLE);
        onebythree.setVisibility(View.INVISIBLE);
        twobyone.setVisibility(View.INVISIBLE);
        twobytwo.setVisibility(View.INVISIBLE);
        twobythree.setVisibility(View.INVISIBLE);
        threebyone.setVisibility(View.INVISIBLE);
        threebytwo.setVisibility(View.INVISIBLE);
        threebythree.setVisibility(View.INVISIBLE);
    }

    private void changeImage(int num) // 쳐다보는 화면의 숫자에 따라서 image의 visibility를 변경시켜줌
    {
        //1번화면을 쳐다보면
        if (num ==11)
        {
            onebyone.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //2번화면
        else if (num ==12)
        {
            onebytwo.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //3번화면
        else if (num ==13)
        {
            onebythree.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //4번화면
        else if (num ==21)
        {
            twobyone.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //5번화면
        else if (num ==22)
        {
            twobytwo.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //6번화면
        else if (num ==23)
        {
            twobythree.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //7번화면
        else if (num ==31)
        {
            threebyone.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //8번화면
        else if (num ==32)
        {
            threebytwo.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }
        //9번화면
        else if (num ==33)
        {
            threebythree.setVisibility(View.VISIBLE);//일정시간//on 켜고
        }

    }

    public void set_image_tile(int chosen_table_num)
    {
        switch (chosen_table_num) //                                                                >>반복적으로 실행되는 것 맞음?
        {
            case (0): //초기값이 없을 때
            {
                init_color();
                break;
            }
            case (11): //1번칸을보고있을때
            {
                init_color();
                changeImage(11);
                break;
            }

            case (12): //1번칸을보고있을때
            {
                init_color();
                changeImage(12);
                break;
            }
            case (13): //1번칸을보고있을때
            {
                init_color();
                changeImage(13);
                break;
            }
            case (21): //1번칸을보고있을때
            {
                init_color();
                changeImage(21);
                break;
            }
            case (22): //1번칸을보고있을때
            {
                init_color();
                changeImage(22);
                break;
            }
            case (23): //1번칸을보고있을때
            {
                init_color();
                changeImage(23);
                break;
            }
            case (31): //1번칸을보고있을때
            {
                init_color();
                changeImage(31);
                break;
            }
            case (32): //1번칸을보고있을때
            {
                init_color();
                changeImage(32);
                break;
            }
            case (33): //1번칸을보고있을때
            {
                init_color();
                changeImage(33);
                break;
            }
            default:
                break;
        }
    }



}
