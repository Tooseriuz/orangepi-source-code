/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.calloption;

import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.mediatek.common.featureoption.FeatureOption;

public class EmergencyCallOptionHandler extends CallOptionBaseHandler {

    private static final String TAG = "EmergencyCallOptionHandler";

    @Override
    public void handleRequest(final Request request) {
        log("handleRequest()");

        String number = CallOptionUtils.getInitialNumber(request.getApplicationContext(), request.getIntent());
        if (Intent.ACTION_CALL_EMERGENCY.equals(request.getIntent().getAction())) {
            request.getResultHandler().onPlaceCallDirectly(request.getIntent());
            return;
        } else {
            boolean isEmergencyNumber = FeatureOption.EVDO_DT_SUPPORT ?
                        (PhoneNumberUtils.isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_CDMA) ||
                            PhoneNumberUtils.isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_GSM)) :
                        PhoneNumberUtils.isEmergencyNumber(number);
            Uri requestData = request.getIntent().getData();
            boolean isVoiceMail = "voicemail:".equals(requestData == null ? "" : requestData.toString());
            if (isEmergencyNumber && !isVoiceMail) {
                request.getResultHandler().onPlaceCallDirectly(request.getIntent());
                return;
            }
        }
        if (null != mSuccessor) {
            mSuccessor.handleRequest(request);
        }
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
