package com.ruutdrop.ruutdropapp.activities;

import com.here.android.mpa.routing.Maneuver;
import com.ruutdrop.ruutdropapp.R;

/**
 * created by Aman on 18/11/16.
 */

public class ManeuverHelper {
    static int getManeuverIcon(Maneuver maneuver) {


        if(maneuver.getIcon().value() == Maneuver.Icon.END.value())
        {
            return R.drawable.direction_flag;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.KEEP_LEFT.value() || maneuver.getIcon().value() == Maneuver.Icon.QUITE_LEFT.value())
        {
            return R.drawable.direction_continue_left;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LIGHT_LEFT.value() || maneuver.getIcon().value() == Maneuver.Icon.HIGHWAY_KEEP_LEFT.value())
        {
            return R.drawable.direction_new_name_slight_left;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HEAVY_LEFT.value()) {
            return R.drawable.direction_notification_sharp_left;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.UTURN_LEFT.value() || maneuver.getIcon().value() == Maneuver.Icon.UTURN_RIGHT.value()) {
            return R.drawable.direction_uturn;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ENTER_HIGHWAY_LEFT_LANE.value())
        {
            return R.drawable.direction_merge_slight_left;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LEAVE_HIGHWAY_LEFT_LANE.value())
        {
            return R.drawable.direction_off_ramp_slight_left;
        }


        else if(maneuver.getIcon().value() == Maneuver.Icon.KEEP_RIGHT.value() || maneuver.getIcon().value() == Maneuver.Icon.QUITE_RIGHT.value())
        {
            return R.drawable.direction_continue_right;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LIGHT_RIGHT.value() || maneuver.getIcon().value() == Maneuver.Icon.HIGHWAY_KEEP_RIGHT.value())
        {
            return R.drawable.direction_new_name_slight_right;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HEAVY_RIGHT.value()) {
            return R.drawable.direction_notificaiton_sharp_right;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ENTER_HIGHWAY_RIGHT_LANE.value())
        {
            return R.drawable.direction_merge_slight_right;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LEAVE_HIGHWAY_RIGHT_LANE.value())
        {
            return R.drawable.direction_off_ramp_slight_right;
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_1.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_1_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_2.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_2_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_3.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_3_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_4.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_4_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_5.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_5_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_6.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_6_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_7.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_7_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_8.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_8_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_9.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_9_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_10.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_10_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_11.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_11_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_12.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_12_LH.value()
                ) {
            return R.drawable.direction_roundabout;
        }
        else {
            return R.drawable.direction_new_name_straight;
        }




    }

    static String getNextManeuver(Maneuver maneuver) {

        if(maneuver.getIcon().value() == Maneuver.Icon.START.value()) {
            return "Start Navigation";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.END.value()) {
            return "Destination";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.KEEP_LEFT.value()) {
            return "Keep Left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HEAVY_LEFT.value()) {
            return "Sharp Left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LIGHT_LEFT.value()) {
            return "Light Left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.QUITE_LEFT.value()) {
            return "Turn Left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.UTURN_RIGHT.value()|| maneuver.getIcon().value() == Maneuver.Icon.UTURN_LEFT.value())  {
            return "U turn left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HIGHWAY_KEEP_LEFT.value()) {
            return "Highway Keep Left";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ENTER_HIGHWAY_LEFT_LANE.value()) {
            return "Enter Highway Left Lane";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LEAVE_HIGHWAY_LEFT_LANE.value()) {
            return "Leave Highway Left Lane";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.KEEP_RIGHT.value()) {
            return "Keep Right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HEAVY_RIGHT.value()) {
            return "Sharp Right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LIGHT_RIGHT.value()) {
            return "Light Right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.QUITE_RIGHT.value()) {
            return "Turn Right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.UTURN_RIGHT.value()) {
            return "U turn right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HIGHWAY_KEEP_RIGHT.value()) {
            return "Highway Keep Right";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ENTER_HIGHWAY_RIGHT_LANE.value()) {
            return "Enter Highway Right Lane";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.LEAVE_HIGHWAY_RIGHT_LANE.value()) {
            return "Leave Highway Right Lane";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.CHANGE_LINE.value()) {
            return "Change Line";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.GO_STRAIGHT.value()) {
            return "Go Straight";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.FERRY.value()) {
            return "Ferry";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.HEAD_TO.value()) {
            return "Head To";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.KEEP_MIDDLE.value()) {
            return "Keep Middle";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.PASS_STATION.value()) {
            return "Pass Station";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.UNDEFINED.value()) {
            return "Undefined";
        }
        else if(maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_1.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_1_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_2.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_2_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_3.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_3_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_4.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_4_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_5.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_5_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_6.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_6_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_7.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_7_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_8.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_8_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_9.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_9_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_10.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_10_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_11.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_11_LH.value()||
                maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_12.value() || maneuver.getIcon().value() == Maneuver.Icon.ROUNDABOUT_12_LH.value()
                ) {
            return "Round About";
        }
        else {
            return "UNDEFINED";
        }

    }

}
