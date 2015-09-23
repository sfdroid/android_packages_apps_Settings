package com.android.settings.notification;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.settings.R;

public class TripleSwitchView {
    private static final String TAG = TripleSwitchStates.class.getSimpleName();
    private final Context mContext;
    private final FrameLayout mTripleSwitchGroup;

    private String mPackageName;
    private int mUID;

    private View mToggleBackground;
    private ToggleButton mOffToggleButton;
    private ToggleButton mOnToggleButton;
    private ToggleButton mPriorityToggleButton;
    private TextView mStatusText;

    private static Toast sToast;

    private NotificationAppList.Backend mBackend = new NotificationAppList.Backend();

    public enum TripleSwitchStates {
        OFF,
        ON,
        PRIORITY
    }

    private TripleSwitchStates mCurrentTripleSwitchState = TripleSwitchStates.OFF;

    public TripleSwitchView(Context context, View view, TextView statusText, String pkgName, int uid) {
        mTripleSwitchGroup = (FrameLayout) view;
        mStatusText = statusText;
        mContext = context;
        mPackageName = pkgName;
        mUID = uid;
        setupViews();
    }


    private void setupViews() {
        FrameLayout mTripleSwitch = (FrameLayout) mTripleSwitchGroup.findViewById(R.id.triple_switch_group);
        mToggleBackground = mTripleSwitchGroup.findViewById(R.id.toggle_background);
        mOffToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.off_toggle_button);
        mOnToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.on_toggle_button);
        mPriorityToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.priority_toggle_button);

        setCurrentTripleSwitchState();
        
        View.OnClickListener triggerToast = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources resources = mContext.getResources();
                if (sToast != null) {
                    sToast.cancel();
                }
                switch (mCurrentTripleSwitchState) {
                    case ON:
                        sToast = Toast.makeText(mContext, resources.getString(R.string.notifications_on).toUpperCase() + "\n" + resources.getString(R.string.notifications_on_description), Toast.LENGTH_SHORT);
                        sToast.show();
                        break;
                    case OFF:
                        sToast = Toast.makeText(mContext, resources.getString(R.string.notifications_off).toUpperCase() + "\n" + resources.getString(R.string.notifications_off_description), Toast.LENGTH_SHORT);
                        sToast.show();
                        break;
                    case PRIORITY:
                        sToast = Toast.makeText(mContext, resources.getString(R.string.notifications_priority).toUpperCase() + "\n" + resources.getString(R.string.notifications_priority_description), Toast.LENGTH_SHORT);
                        sToast.show();
                        break;
                    default:
                        Log.wtf(TAG, "Unknow triple switch state: " + mCurrentTripleSwitchState);
                        break;
                }
            }
        };
        
        View.OnClickListener updateTripleState = new View.OnClickListener() {
            private View.OnClickListener mExtraAction;
            public View.OnClickListener setup(View.OnClickListener extra){
                mExtraAction = extra;
                return this;
            }
            @Override
            public void onClick(View v) {
                changeTripleSwitchState();
                if (mExtraAction != null) {
                    mExtraAction.onClick(v);
                }
            }
        }.setup(triggerToast);

        mOffToggleButton.setOnClickListener(updateTripleState);
        mOnToggleButton.setOnClickListener(updateTripleState);
        mPriorityToggleButton.setOnClickListener(updateTripleState);
        
        ((View)mStatusText.getParent()).setOnClickListener(triggerToast);
    }

    private void changeTripleSwitchState() {
        Resources resources = mContext.getResources();

        switch (mCurrentTripleSwitchState) {
            case OFF:
                setTripleSwitchOn(resources);

                if (!TextUtils.isEmpty(mPackageName)) {
                    mBackend.setNotificationsBanned(mPackageName, mUID, false);
                    mBackend.setHighPriority(mPackageName, mUID, false);
                }
                break;
            case ON:
                setTripleSwitchPriority(resources);

                if (!TextUtils.isEmpty(mPackageName)) {
                    mBackend.setNotificationsBanned(mPackageName, mUID, false);
                    mBackend.setHighPriority(mPackageName, mUID, true);
                }
                break;
            case PRIORITY:
                setTripleSwitchOff(resources);

                if (!TextUtils.isEmpty(mPackageName)) {
                    mBackend.setNotificationsBanned(mPackageName, mUID, true);
                    mBackend.setHighPriority(mPackageName, mUID, false);
                }
                break;
            default:
                Log.wtf(TAG, "Unknow triple switch state: " + mCurrentTripleSwitchState);
                break;
        }
    }

    private void setCurrentTripleSwitchState() {
        Resources resources = mContext.getResources();

        if ((!mBackend.getNotificationsBanned(mPackageName, mUID)) &&
                mBackend.getHighPriority(mPackageName, mUID)) {
            setTripleSwitchPriority(resources);
        } else if (mBackend.getNotificationsBanned(mPackageName, mUID)) {
            setTripleSwitchOff(resources);
        } else {
            setTripleSwitchOn(resources);
        }
    }

    private void setTripleSwitchOff(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_grey);
        mOffToggleButton.setChecked(true);
        mOnToggleButton.setChecked(false);
        mPriorityToggleButton.setChecked(false);
        mStatusText.setTextColor(resources.getColor(R.color.tsw_grey_dark));
        mStatusText.setText(resources.getString(R.string.notifications_off));
        mCurrentTripleSwitchState = TripleSwitchStates.OFF;
    }

    private void setTripleSwitchPriority(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_green);
        mOffToggleButton.setChecked(false);
        mOnToggleButton.setChecked(false);
        mPriorityToggleButton.setChecked(true);
        mStatusText.setTextColor(resources.getColor(R.color.tsw_green));
        mStatusText.setText(resources.getString(R.string.notifications_priority));
        mCurrentTripleSwitchState = TripleSwitchStates.PRIORITY;
    }

    private void setTripleSwitchOn(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_blue);
        mOffToggleButton.setChecked(false);
        mOnToggleButton.setChecked(true);
        mPriorityToggleButton.setChecked(false);
        mStatusText.setTextColor(resources.getColor(R.color.tsw_blue));
        mStatusText.setText(resources.getString(R.string.notifications_on));
        mCurrentTripleSwitchState = TripleSwitchStates.ON;
    }
}

