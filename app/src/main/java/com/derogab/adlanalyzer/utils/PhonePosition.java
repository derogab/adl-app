package com.derogab.adlanalyzer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.derogab.adlanalyzer.MainActivity;
import com.derogab.adlanalyzer.R;

import java.util.ArrayList;

public class PhonePosition {

    public static final String IN_LEFT_HAND = "left_hand";
    public static final String IN_RIGHT_HAND = "right_hand";
    public static final String IN_THE_FRONT_LEFT_POCKET = "front_left_pocket";
    public static final String IN_THE_FRONT_RIGHT_POCKET = "back_left_pocket";
    public static final String IN_THE_BACK_LEFT_POCKET = "front_right_pocket";
    public static final String IN_THE_BACK_RIGHT_POCKET = "back_right_pocket";

    private String position;
    private String descriptionSource;

    public PhonePosition(Context context, String position) {

        this.position = position;
        Log.d("PhonePosition", position);

        switch (position){
            case IN_THE_FRONT_LEFT_POCKET:
                descriptionSource = context.getString(R.string.phone_position_in_the_front_left_pocket);
                break;
            case IN_THE_FRONT_RIGHT_POCKET:
                descriptionSource = context.getString(R.string.phone_position_in_the_front_right_pocket);
                break;
            case IN_THE_BACK_LEFT_POCKET:
                descriptionSource = context.getString(R.string.phone_position_in_the_back_left_pocket);
                break;
            case IN_THE_BACK_RIGHT_POCKET:
                descriptionSource = context.getString(R.string.phone_position_in_the_back_right_pocket);
                break;

            case IN_LEFT_HAND:
                descriptionSource = context.getString(R.string.phone_position_in_left_hand);
                break;
            case IN_RIGHT_HAND:
            default:
                descriptionSource = context.getString(R.string.phone_position_in_right_hand);
                break;

        }

    }

    public static ArrayList<PhonePosition> getAll(Context context) {

        ArrayList<PhonePosition> all = new ArrayList<>();
            all.add(new PhonePosition(context, IN_LEFT_HAND));
            all.add(new PhonePosition(context, IN_RIGHT_HAND));
            all.add(new PhonePosition(context, IN_THE_FRONT_LEFT_POCKET));
            all.add(new PhonePosition(context, IN_THE_FRONT_RIGHT_POCKET));
            all.add(new PhonePosition(context, IN_THE_BACK_LEFT_POCKET));
            all.add(new PhonePosition(context, IN_THE_BACK_RIGHT_POCKET));

        return all;

    }

    public String getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return descriptionSource;
    }
}
