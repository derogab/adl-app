package com.derogab.adlanalyzer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.derogab.adlanalyzer.MainActivity;
import com.derogab.adlanalyzer.R;

import java.util.ArrayList;

public class PhonePosition {

    public static final String IN_HAND = "in_the_hand";
    public static final String IN_THE_POCKET = "in_the_pocket";

    private String position;
    private String descriptionSource;

    public PhonePosition(Context context, String position) {

        this.position = position;
        Log.d("PhonePosition", position);

        switch (position){
            case IN_THE_POCKET:
                descriptionSource = context.getString(R.string.phone_position_in_the_pocket);
                break;

            case IN_HAND:
            default:
                descriptionSource = context.getString(R.string.phone_position_in_hand);
                break;

        }

    }

    public static ArrayList<PhonePosition> getAll(Context context) {

        ArrayList<PhonePosition> all = new ArrayList<>();
            all.add(new PhonePosition(context, IN_HAND));
            all.add(new PhonePosition(context, IN_THE_POCKET));

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
