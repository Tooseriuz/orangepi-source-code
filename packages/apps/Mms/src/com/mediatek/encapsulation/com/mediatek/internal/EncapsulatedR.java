
package com.mediatek.encapsulation.com.mediatek.internal;

import com.mediatek.encapsulation.EncapsulationConstant;

import com.android.mms.R;

/*** M: MTK ADD */
public class EncapsulatedR {

    public static class drawable {

        public static final int DRAWABLE_DEFAULTVALUE = android.R.color.background_light;

        public static final int drm_red_lock;

        public static final int drm_green_lock;

        public static final int sim_locked;

        public static final int sim_radio_off;

        public static final int sim_invalid;

        public static final int sim_searching;

        public static final int sim_roaming;

        public static final int sim_connected;

        public static final int sim_roaming_connected;

        public static final int sim_background_locked;

        public static final int sim_background_blue;

        public static final int sim_background_orange;

        public static final int sim_background_green;

        public static final int sim_background_purple;

        public static final int sim_dark_blue;

        public static final int sim_dark_orange;

        public static final int sim_dark_green;

        public static final int sim_dark_purple;

        public static final int sim_light_blue;

        public static final int sim_light_orange;

        public static final int sim_light_green;

        public static final int sim_light_purple;

        public static final int ic_dialog_menu_generic;

        static {
            if (EncapsulationConstant.USE_MTK_PLATFORM) {
                drm_red_lock = com.mediatek.internal.R.drawable.drm_red_lock;
                drm_green_lock = com.mediatek.internal.R.drawable.drm_green_lock;
                sim_locked = com.mediatek.internal.R.drawable.sim_locked;
                sim_radio_off = com.mediatek.internal.R.drawable.sim_radio_off;
                sim_invalid = com.mediatek.internal.R.drawable.sim_invalid;
                sim_searching = com.mediatek.internal.R.drawable.sim_searching;
                sim_roaming = com.mediatek.internal.R.drawable.sim_roaming;
                sim_connected = com.mediatek.internal.R.drawable.sim_connected;
                sim_roaming_connected = com.mediatek.internal.R.drawable.sim_roaming_connected;
                sim_background_locked = R.drawable.sim_background_locked;

                sim_background_blue = com.mediatek.internal.R.drawable.sim_background_blue;
                sim_background_orange = com.mediatek.internal.R.drawable.sim_background_orange;
                sim_background_green = com.mediatek.internal.R.drawable.sim_background_green;
                sim_background_purple = com.mediatek.internal.R.drawable.sim_background_purple;
                sim_dark_blue = com.mediatek.internal.R.drawable.sim_dark_blue;
                sim_dark_orange = com.mediatek.internal.R.drawable.sim_dark_orange;
                sim_dark_green = com.mediatek.internal.R.drawable.sim_dark_green;
                sim_dark_purple = com.mediatek.internal.R.drawable.sim_dark_purple;
                sim_light_blue = com.mediatek.internal.R.drawable.sim_light_blue;
                sim_light_orange = com.mediatek.internal.R.drawable.sim_light_orange;
                sim_light_green = com.mediatek.internal.R.drawable.sim_light_green ;
                sim_light_purple = com.mediatek.internal.R.drawable.sim_light_purple;

                ic_dialog_menu_generic = com.mediatek.R.drawable.ic_dialog_menu_generic;

            } else {
                /** M: Can not complete for this branch. */
                drm_red_lock = DRAWABLE_DEFAULTVALUE;
                drm_green_lock = DRAWABLE_DEFAULTVALUE;
                sim_locked = DRAWABLE_DEFAULTVALUE;
                sim_radio_off = DRAWABLE_DEFAULTVALUE;
                sim_invalid = DRAWABLE_DEFAULTVALUE;
                sim_searching = DRAWABLE_DEFAULTVALUE;
                sim_roaming = DRAWABLE_DEFAULTVALUE;
                sim_connected = DRAWABLE_DEFAULTVALUE;
                sim_roaming_connected = DRAWABLE_DEFAULTVALUE;
                sim_background_locked = R.drawable.sim_background_locked;

                sim_background_blue = DRAWABLE_DEFAULTVALUE;
                sim_background_orange = DRAWABLE_DEFAULTVALUE;
                sim_background_green = DRAWABLE_DEFAULTVALUE;
                sim_background_purple = DRAWABLE_DEFAULTVALUE;
                sim_dark_blue = DRAWABLE_DEFAULTVALUE;
                sim_dark_orange = DRAWABLE_DEFAULTVALUE;
                sim_dark_green = DRAWABLE_DEFAULTVALUE;
                sim_dark_purple = DRAWABLE_DEFAULTVALUE;
                sim_light_blue = DRAWABLE_DEFAULTVALUE;
                sim_light_orange = DRAWABLE_DEFAULTVALUE;
                sim_light_green = DRAWABLE_DEFAULTVALUE;
                sim_light_purple = DRAWABLE_DEFAULTVALUE;

                ic_dialog_menu_generic = DRAWABLE_DEFAULTVALUE;
            }

        }
    }

    public static class string {

        public static final int STRING_DEFAULTVALUE = android.R.string.ok;

        public static final int url_dialog_choice_title;

        public static final int url_dialog_choice_message;

        public static final int new_sim;

        public static final int ime_action_done;

        public static final int ime_action_next;

        public static final int sim_close;

        static {
            if (EncapsulationConstant.USE_MTK_PLATFORM) {
                url_dialog_choice_message = com.mediatek.internal.R.string.url_dialog_choice_message;
                url_dialog_choice_title = com.mediatek.internal.R.string.url_dialog_choice_title;
                new_sim = com.mediatek.internal.R.string.new_sim;
                ime_action_done = com.android.internal.R.string.ime_action_done;
                ime_action_next = com.android.internal.R.string.ime_action_next;
                sim_close = com.mediatek.R.string.sim_close;
            } else {
                /** M: Can not complete for this branch. */
                url_dialog_choice_message = STRING_DEFAULTVALUE;
                url_dialog_choice_title = STRING_DEFAULTVALUE;
                new_sim = STRING_DEFAULTVALUE;
                ime_action_done = STRING_DEFAULTVALUE;
                ime_action_next = STRING_DEFAULTVALUE;
                sim_close = STRING_DEFAULTVALUE;
            }
        }
    }
}
