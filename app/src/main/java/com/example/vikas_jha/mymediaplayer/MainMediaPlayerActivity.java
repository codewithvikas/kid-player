package com.example.vikas_jha.mymediaplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class MainMediaPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,MediaController.MediaPlayerControl {

    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurfaceView;
    private FrameLayout parentFrameLayout;
    //String vidAddress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
    String vidAddress = "";
    private Cursor videoCursor;
    //private int count;
    private int videoColumnIndex;
    //String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,MediaStore.Video.Thumbnails.VIDEO_ID};
    //private String thumbPath;
    private Point screenSize = new Point();
    private MediaController mMediaController;
    private Handler handler = new Handler();
    private int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_media_player);
        parentFrameLayout = (FrameLayout) findViewById(R.id.parentRelative);
        vidSurfaceView = (VideoView) parentFrameLayout.findViewById(R.id.videoSurfaceView);
        //videoView = (VideoView) findViewById(R.id.myVideoView);
        //Uri vidUri = Uri.parse(vidAddress);
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screenSize);

        initialization();
    }

    @Override
    public void onBackPressed() {


        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            closeVideo();
        }
        else {
            super.onBackPressed();
        }


    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null){
            mediaPlayer.stop();

        }
        closeVideo();
        super.onPause();
    }


    private void initialization() {
        System.gc();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,screenSize.y /3);
        parentFrameLayout.setLayoutParams(layoutParams);
        String[] videoProjection = {
                                     MediaStore.Video.Media._ID,MediaStore.Video.Media.DATA,
                                     MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE,
                                     MediaStore.Video.Media.DURATION

                                    };
        //videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, MediaStore.Video.Media.DATA + " like ? ", new String[]{"%movies%","%download%"}, null);
        videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null);
        //count = videoCursor.getCount();

        ListView videoList = (ListView) findViewById(R.id.PhoneVideoList);
        videoList.setAdapter(new VideoListAdapter(this.getApplication()));
        videoList.setOnItemClickListener(videoGridListener);
        parentFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mMediaController.isShowing()){
                    mMediaController.hide();
                }
                else {
                    mMediaController.show();
                }
                return true;
            }
        });
    }

    private AdapterView.OnItemClickListener videoGridListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            System.gc();
            videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            videoCursor.moveToPosition(position);
            String fileName = videoCursor.getString(videoColumnIndex);
            Log.i("FileName: ",fileName);
            vidAddress = fileName;

            parentFrameLayout.bringToFront();
            vidSurfaceView.setVisibility(View.VISIBLE);
            parentFrameLayout.setVisibility(View.VISIBLE);
            vidHolder = vidSurfaceView.getHolder();

            //vidHolder.addCallback(MainMediaPlayerActivity.this);
            setMediaPlayer();
            /*videoView = new VideoView(MainMediaPlayerActivity.this);
            videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentFrameLayout.addView(videoView);
            videoView.bringToFront();
            Uri vidUri = Uri.parse(vidAddress);
            videoView.setVideoURI(vidUri);
            */


        }
    };
    private void setMediaPlayer(){
        try {
            if (mediaPlayer!=null){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.release();

            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setDataSource(vidAddress);

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    int videoWidth = mediaPlayer.getVideoWidth();
                    int videoHeight = mediaPlayer.getVideoHeight();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) vidSurfaceView.getLayoutParams();
                    layoutParams.height = screenSize.y/3;
                    layoutParams.width = layoutParams.height * videoWidth/videoHeight;
                    layoutParams.gravity = Gravity.CENTER;
                    vidSurfaceView.setLayoutParams(layoutParams);

                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    closeVideo();
                }
            });

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaController = new MediaController(MainMediaPlayerActivity.this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



   /* @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setDataSource(vidAddress);

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    int videoWidth = mediaPlayer.getVideoWidth();
                    int videoHeight = mediaPlayer.getVideoHeight();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) vidSurfaceView.getLayoutParams();
                    layoutParams.height = screenSize.y/3;
                    layoutParams.width = layoutParams.height * videoWidth/videoHeight;
                    layoutParams.gravity = Gravity.CENTER;
                    vidSurfaceView.setLayoutParams(layoutParams);
                }
            });
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    private void playNext(){
        System.gc();
        videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        if (videoCursor.moveToNext()){
            String fileName = videoCursor.getString(videoColumnIndex);
            Log.i("FileName: ",fileName);
            vidAddress = fileName;

            parentFrameLayout.bringToFront();
            parentFrameLayout.setVisibility(View.VISIBLE);
            vidHolder = vidSurfaceView.getHolder();
            setMediaPlayer();
        }


    }

    private void playPrevious(){
        System.gc();
        videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        if (videoCursor.moveToPrevious()){
            String fileName = videoCursor.getString(videoColumnIndex);
            Log.i("FileName: ",fileName);
            vidAddress = fileName;

            parentFrameLayout.bringToFront();
            parentFrameLayout.setVisibility(View.VISIBLE);
            vidHolder = vidSurfaceView.getHolder();
            setMediaPlayer();
        }
    }

    private void closeVideo() {

       /*     mediaPlayer.release();
        mediaPlayer = null;*/
        if (mMediaController!=null){
            mMediaController.setVisibility(View.INVISIBLE);
        }

        vidSurfaceView.setVisibility(View.INVISIBLE);
        parentFrameLayout.setVisibility(View.INVISIBLE);
    }
    @Override
    public void start() {

        Log.e("MediaContoler","Started");
        mediaPlayer.start();

    }

    @Override
    public void pause()
    {
        Log.e("MediaContoler","Paused!!!");
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer!=null?mediaPlayer.getDuration():0;
    }

    @Override
    public int getCurrentPosition() {

        return mediaPlayer!=null?mediaPlayer.getCurrentPosition():0;
    }

    @Override
    public void seekTo(int pos) {
            Log.e("pos ",""+pos);
            mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }


    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    public class VideoListAdapter extends BaseAdapter
    {
        private Context vContext;
        public VideoListAdapter(Context c){
           vContext = c;
        }
        @Override
        public int getCount() {
            return videoCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItemRow = LayoutInflater.from(vContext).inflate(R.layout.listitem,parent,false);

            TextView txtTitle = (TextView) listItemRow.findViewById(R.id.txtTitle);
            TextView txtSize = (TextView) listItemRow.findViewById(R.id.txtSize);
            TextView txtDuration = (TextView) listItemRow.findViewById(R.id.txtDuration);

            ImageView thumbImage = (ImageView) listItemRow.findViewById(R.id.imgIcon);
            videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            videoCursor.moveToPosition(position);
            txtTitle.setText(videoCursor.getString(videoColumnIndex));

            videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            //videoCursor.moveToPosition(position);
            String sizeInKb = videoCursor.getString(videoColumnIndex);
            long sizeInMb = Long.parseLong(sizeInKb);
            sizeInMb = sizeInMb / 1024;
            txtSize.setText("File Size :" + sizeInMb+"MB");

            videoColumnIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            String durationString = videoCursor.getString(videoColumnIndex);
            long durationInMs = Long.parseLong(durationString);
            long durationInSecond = durationInMs / 1000;

            txtDuration.setText("Duration :"+(durationInSecond / 60)+" min"+ (durationInSecond % 60) + " sec");


            //int videoId = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

            // create thumbnail for vedio

            Bitmap thumbail = ThumbnailUtils.createVideoThumbnail(videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media.DATA)),MediaStore.Images.Thumbnails.MICRO_KIND);
            //Cursor videoThumbNailCursor = managedQuery(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
            //        thumbColumns,MediaStore.Video.Thumbnails.VIDEO_ID+ "=" + videoId,null,null);
           /* if (videoThumbNailCursor.moveToFirst()){
                //thumbPath = videoThumbNailCursor.getString(videoThumbNailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                //Log.i("ThubPath: ",thumbPath);

            }*/
            thumbImage.setLayoutParams(new LinearLayout.LayoutParams(500,500));
            thumbImage.setScaleType(ImageView.ScaleType.FIT_XY);
            thumbImage.setImageBitmap(thumbail);
           // thumbImage.setImageURI(Uri.parse(thumbPath));


           return listItemRow;

        }
    }

   /* @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }*/

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaController.setMediaPlayer(this);
        mMediaController.setAnchorView(parentFrameLayout);
        mMediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                mMediaController.setEnabled(true);
                mediaPlayer.start();
                mMediaController.show();
            }
        });


    }
}
