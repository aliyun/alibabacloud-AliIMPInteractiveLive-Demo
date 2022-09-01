package com.aliyun.roompaas.uibase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.roompaas.base.util.TimeFormat;
import com.aliyun.roompaas.uibase.R;

/**
 * 播放器播控View
 */

public class ControlView extends RelativeLayout implements View.OnClickListener {
    private View container;
    private ImageView playStatusBtn;
    private SeekBar seekBar;
    private TextView currentPosition;
    private TextView totalPosition;

    private boolean isPlaying = false;

    public ControlView(Context context) {
        this(context, null);
    }

    public ControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.iub_control_view_layout, this, true);
        playStatusBtn = findViewById(R.id.play_status_btn);
        seekBar = findViewById(R.id.seek_bar_position);
        currentPosition = findViewById(R.id.current_position);
        totalPosition = findViewById(R.id.total_position);
        container = findViewById(R.id.container);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ControlView, defStyleAttr, 0);
        int horizontalPadding = (int) a.getDimension(R.styleable.ControlView_horizontalPadding, 0);
        if (horizontalPadding != 0) {
            container.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        }
        a.recycle();

        playStatusBtn.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setCurrentPosition(progress);
                }
                if (seekListener != null) {
                    seekListener.onProgressChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (seekListener != null) {
                    seekListener.onSeekStart(seekBar.getProgress());
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekListener != null) {
                    seekListener.onSeekEnd(seekBar.getProgress());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            if (isPlaying) {
                listener.onPause();
            } else {
                listener.onResume();
            }
        }
        isPlaying = !isPlaying;
        updatePlayState();
    }

    public void setPlayStatus(boolean isPlaying) {
        this.isPlaying = isPlaying;
        updatePlayState();
    }

    private void updatePlayState() {
        playStatusBtn.setImageResource(isPlaying ? R.drawable.iub_alivc_playstate_pause : R.drawable.iub_alivc_playstate_play);
    }

    /**
     * 设置进度条百分比
     * @param progress
     */
    public void updateProgress(long progress) {
        seekBar.setProgress((int) progress);
    }

    /**
     * 设置缓冲进度条
     * @param progress
     */
    public void setBufferedProgress(long progress) {
        seekBar.setSecondaryProgress((int) progress);
    }

    /**
     * 设置当前正在播放的时间
     * @param position
     */
    public void setCurrentPosition(long position) {
        if (currentPosition != null) {
            currentPosition.setText(TimeFormat.formatMs(position));
        }
    }

    /**
     * 设置总时长
     * @param position
     */
    public void setDuration(long position) {
        totalPosition.setText(TimeFormat.formatMs(position));
        seekBar.setMax((int) position);
    }

    public void setPlayStatusClickListener(PlayStatusChange listener) {
        this.listener = listener;
    }

    private PlayStatusChange listener;

    public interface PlayStatusChange {
        void onPause();
        void onResume();
    }

    public interface OnSeekListener {
        void onSeekStart(int position);

        void onSeekEnd(int position);

        void onProgressChanged(int position);
    }

    private OnSeekListener seekListener;

    public void setOnSeekListener(OnSeekListener listener) {
        seekListener = listener;
    }

}
