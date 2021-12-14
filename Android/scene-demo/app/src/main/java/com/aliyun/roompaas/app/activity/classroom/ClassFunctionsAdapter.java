package com.aliyun.roompaas.app.activity.classroom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;

import java.util.ArrayList;
import java.util.List;

public  class ClassFunctionsAdapter extends RecyclerView.Adapter {

    private final Context mContext;
    private final ArrayList<Pair<String, Integer>> mFunctions;
    private FunctionCheckedListener mListener;
    private List<Integer> mSelectedViews = new ArrayList<>();
    private List<Integer> mEnableViews = new ArrayList<>();
    private boolean initBtnStatus;

    public void updateFunction(FunctionName functionName, String name) {
        if (mFunctions == null) {
            return;
        }
        Pair<String, Integer> function = mFunctions.get(functionName.function);
        mFunctions.set(functionName.function, new Pair<>(name, function.second));
        notifyItemChanged(functionName.function);
    }

    public ClassFunctionsAdapter(Context context) {
        mContext = context;
        mFunctions = getFunctions();
        mEnableViews.add(0);
        mEnableViews.add(1);
        mEnableViews.add(3);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_rtc_functions, parent, false);
        return new BottomFunctionHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BottomFunctionHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mFunctions != null ? mFunctions.size() : 0;
    }

    public void setListener(FunctionCheckedListener listener) {
        mListener = listener;
    }

    public void initBtnStatus() {
        initBtnStatus = true;
    }

    private class BottomFunctionHolder extends RecyclerView.ViewHolder {

        private final ImageView mFunctionIcon;
        private final TextView mFunctionName;

        public BottomFunctionHolder(View inflate) {
            super(inflate);
            mFunctionIcon = inflate.findViewById(R.id.alivc_big_interactive_class_iv_bottom_function_icon);
            mFunctionName = inflate.findViewById(R.id.alivc_big_interactive_class_tv_bottom_function_name);
        }

        public void bindView(final int position) {
            String str = null;
            int bgResId = -1;
            boolean contains = mSelectedViews.contains(position);
            if (position == 0) {
                str = mContext.getString(contains ? R.string.alivc_biginteractiveclass_string_resume : R.string.alivc_biginteractiveclass_string_mute_mic);
                bgResId = R.drawable.alivc_biginteractiveclass_mute_mic_selector;
            } else if (position == 1) {
                str = mContext.getString(contains ? R.string.alivc_biginteractiveclass_string_open_camera : R.string.alivc_biginteractiveclass_string_camera);
                bgResId = R.drawable.alivc_biginteractiveclass_mute_camera_selector;
            } else if (position == 2) {
//                str = mContext.getString(contains ? R.string.alivc_biginteractiveclass_string_unconn_mic : R.string.alivc_biginteractiveclass_string_conn_mic);
                str = mFunctions.get(position).first;
                bgResId = R.drawable.alivc_biginteractiveclass_conn_mic_selector;
            } else if (position == 3) {
                str = mFunctions.get(position).first;
                bgResId = R.drawable.alivc_biginteractiveclass_rotate_camera_selector;
            } else {
                str = mFunctions.get(position).first;
                bgResId = mFunctions.get(position).second;
            }
            mFunctionName.setText(str);
            mFunctionIcon.setBackgroundResource(bgResId);
            if (contains) {
                mFunctionIcon.setSelected(true);
            } else {
                mFunctionIcon.setSelected(false);
            }
            if (mEnableViews.contains(position)) {
                //                mFunctionIcon.setEnabled(true);
                mFunctionIcon.setAlpha(0.5f);
                mFunctionName.setAlpha(0.5f);
            } else {
                mFunctionIcon.setAlpha(1f);
                mFunctionName.setAlpha(1f);
                //                mFunctionIcon.setEnabled(false);
            }
            ((ViewGroup) mFunctionIcon.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean b = true;
                    if (mListener != null) {
                        b = mListener.onFunctionChecked(FunctionName.of(position));
                    }
                    if (!b) {
                        return;
                    }

                    if (initBtnStatus) {
                        initAllBtnStatus();
                    } else {
                        if (mSelectedViews.contains(position)) {
                            mSelectedViews.remove(Integer.valueOf(position));
                        } else {
                            mSelectedViews.add(position);
                        }
                        mEnableViews.clear();
                        notifyDataSetChanged();
                    }

                }
            });
        }
    }

    public void initAllBtnStatus() {
        mEnableViews.add(0);
        mEnableViews.add(1);
        mEnableViews.add(3);
        mSelectedViews.clear();
        notifyDataSetChanged();
        initBtnStatus = false;
    }

    public interface FunctionCheckedListener {
        boolean onFunctionChecked(FunctionName function);
    }

    private ArrayList<Pair<String, Integer>> getFunctions() {
        String[] names;
        names = mContext.getResources().getStringArray(R.array.functions_landscape);
        ArrayList<Pair<String, Integer>> functions = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            functions.add(new Pair<>(names[i], getFunctionsResId(FunctionName.of(i))));
        }
        return functions;
    }

    private Integer getFunctionsResId(FunctionName functionName) {
        int resId = -1;
        switch (functionName) {
            case Mute_Mic:
                resId = R.drawable.alivc_biginteractiveclass_mute_mic;
                break;
            case Mute_Camera:
                resId = R.drawable.alivc_biginteractiveclass_mute_camera;
                break;
            case Join_RTC:
                resId = R.drawable.alivc_biginteractiveclass_conn_mic;
                break;
            case Rotate_Camera:
                resId = R.drawable.alivc_biginteractiveclass_rotate_camera;
                break;
            case Leave_Channel:
                resId = R.drawable.alivc_biginteractiveclass_leavel_channel;
                break;
            default:
        }
        return resId;
    }

    public enum FunctionName {
        Mute_Mic(0),
        Mute_Camera(1),
        Join_RTC(2),
        Rotate_Camera(3),
        Leave_Channel(4);

        private int function;
        FunctionName(int function) {
            this.function = function;
        }

        public static FunctionName of(int function) {
            for (FunctionName functionName : values()) {
                if (functionName.function == function) {
                    return functionName;
                }
            }
            return null;
        }
    }
}
