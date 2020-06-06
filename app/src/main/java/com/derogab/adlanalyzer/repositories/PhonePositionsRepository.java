package com.derogab.adlanalyzer.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.models.PhonePosition;

import java.util.ArrayList;
import java.util.List;

public class PhonePositionsRepository {

    private static final String TAG = "PhonePositionsRepository";

    private static PhonePositionsRepository instance;
    private List<PhonePosition> phonePositions;

    private PhonePositionsRepository(Context context) {

        // Create list
        phonePositions = new ArrayList<>();
        // Add all positions
        phonePositions.add(new PhonePosition(PhonePosition.IN_LEFT_HAND,
                context.getString(R.string.phone_position_in_left_hand)));
        phonePositions.add(new PhonePosition(PhonePosition.IN_RIGHT_HAND,
                context.getString(R.string.phone_position_in_right_hand)));
        phonePositions.add(new PhonePosition(PhonePosition.IN_THE_FRONT_LEFT_POCKET,
                context.getString(R.string.phone_position_in_the_front_left_pocket)));
        phonePositions.add(new PhonePosition(PhonePosition.IN_THE_FRONT_RIGHT_POCKET,
                context.getString(R.string.phone_position_in_the_front_right_pocket)));
        phonePositions.add(new PhonePosition(PhonePosition.IN_THE_BACK_LEFT_POCKET,
                context.getString(R.string.phone_position_in_the_back_left_pocket)));
        phonePositions.add(new PhonePosition(PhonePosition.IN_THE_BACK_RIGHT_POCKET,
                context.getString(R.string.phone_position_in_the_back_right_pocket)));

    }

    public static synchronized PhonePositionsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PhonePositionsRepository(context);
        }
        return instance;
    }

    public void getPhonePositions(MutableLiveData<List<PhonePosition>> phonePositions) {

        phonePositions.postValue(this.phonePositions);

    }


}

