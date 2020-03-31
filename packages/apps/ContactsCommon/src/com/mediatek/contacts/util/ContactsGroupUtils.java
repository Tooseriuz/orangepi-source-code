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
package com.mediatek.contacts.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.android.contacts.common.R;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;

import com.mediatek.common.telephony.UsimGroup;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simservice.SIMServiceUtils;
import com.mediatek.contacts.simservice.SIMServiceUtils.ServiceWorkData;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.SlotUtils;
import com.mediatek.telephony.TelephonyManagerEx;

import com.mediatek.contacts.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ContactsGroupUtils {
    private static final String TAG = "ContactsGroupUtils";

    public static int sArrayData;
    public static final String CONTACTS_IN_GROUP_SELECT =
    " IN "
            + "(SELECT " + RawContacts.CONTACT_ID
            + " FROM " + "raw_contacts"
            + " WHERE " + "raw_contacts._id" + " IN "
                    + "(SELECT " + "data." + Data.RAW_CONTACT_ID
                    + " FROM " + "data "
                    + "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)"
                    + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                            + "(SELECT " + "groups" + "." + Groups._ID
                            + " FROM " + "groups"
                            + " WHERE " + Groups.DELETED + "=0 AND " + Groups.TITLE + "=?))" 
             + " AND " + RawContacts.DELETED + "=0)";

    public static IIccPhoneBook getIIccPhoneBook(int slotId) {
        String serviceName = SlotUtils.getSimPhoneBookServiceNameForSlot(slotId);
        LogUtils.d(TAG, "[getIIccPhoneBook]slotId:" + slotId + ",serviceName:" + serviceName);
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService(serviceName));
        return iIccPhb;
    }
    
    public static final String SELECTION_MOVE_GROUP_DATA = RawContacts.CONTACT_ID
                                + " IN (%1) AND "
                                + Data.MIMETYPE
                                + "='"
                                + GroupMembership.CONTENT_ITEM_TYPE
                                + "' AND "
                                + GroupMembership.GROUP_ROW_ID + "='%2'";

    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 150;

    /**
     * Move contacts from one USIM group to another
     * 
     * @param data Contacts data.
     * @param ugrpIdArray Must be created before calling, and the array length
     *            must 4. the first one indicates old USIM group id, and the
     *            second one indicates the target USIM group id.
     * @param isInTargetGroup This variable indicates whether a contacts is
     *            already in target group.
     */
    public static boolean moveUSIMGroupMember(ContactsGroupArrayData data, int slotId,
            boolean isInTargetGroup, int fromUgrpId, int toUgrpId) {
        boolean ret = false;
        int simId = data.mSimIndexPhoneOrSim;
        LogUtils.i(TAG,"[moveUSIMGroupMember]sim id: " + simId + ",slotId:" + slotId);
        
        if (simId >= 0) {
            if (slotId >= 0) {

                // Add group data into new USIM group if it is not in new USIM
                // group
                boolean moveSucess = false;
                moveSucess = ContactsGroupUtils.USIMGroup
                        .moveContactToGroups(slotId, data.mSimIndex, new int[] { fromUgrpId }, new int[] { toUgrpId });
                LogUtils.i(TAG, "[moveUSIMGroupMember]moveSucess : " + moveSucess + ",data.mSimIndex:" + data.mSimIndex
                        + ",fromUgrpId:" + fromUgrpId + ",toUgrpId:" + toUgrpId);
                ret = moveSucess;
            }
        }
        
        return ret;
    }
    
    public static final class USIMGroup {
        private static final String TAG = "ContactsGroupUtils.USIMGroup";

        public static final String SIM_TYPE_USIM = "USIM";

        // A inner class USED for Usim group cache.
        private static final class USimGroupArray {
            private ArrayList<ArrayList<UsimGroup>> mUgrpArray;
            private SparseIntArray mMaxUsimGroupNameLength;
            private SparseIntArray mMaxUsimGroupCount;
            private int mSize = 0;

            USimGroupArray(int size) {
                mUgrpArray = new ArrayList<ArrayList<UsimGroup>>();
                mMaxUsimGroupNameLength = new SparseIntArray(size);
                mMaxUsimGroupCount = new SparseIntArray();
                int firstSlotId = SlotUtils.getFirstSlotId();
                for (int i = 0; i < size; i++) {
                    ArrayList<UsimGroup> ugrpList = new ArrayList<UsimGroup>();
                    mUgrpArray.add(firstSlotId + i, ugrpList);
                    mMaxUsimGroupNameLength.put(firstSlotId + i, -1);
                    mMaxUsimGroupCount.put(firstSlotId + i, -1);
                }
                mSize = size;
            }

            ArrayList<UsimGroup> get(int slotId) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[get]slot is invalid,slot id:" + slotId);
                    return null;
                }
                return mUgrpArray.get(slotId);
            }

            boolean addItem(int slotId, UsimGroup usimGroup) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[addItem]slot is invalid,slot id:" + slotId);
                    return false;
                }
                return mUgrpArray.get(slotId).add(usimGroup);
            }

            boolean removeItem(int slotId, int usimGroupId) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[removeItem]slot is invalid,slot id:" + slotId);
                    return false;
                }
                ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slotId);
                int i = 0;
                for (UsimGroup ug : ugrpList) {
                    if (ug.getRecordIndex() == usimGroupId) {
                        break;
                    }
                    i++;
                }
                LogUtils.i(TAG, "[removeItem]size:" + ugrpList.size() + ",i=" + i);
                if (i < ugrpList.size()) {
                    ugrpList.remove(i);
                    return true;
                }
                
                return false;
            }

            UsimGroup getItem(int slotId, int usimGroupId) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[getItem]slot is invalid,slot id:" + slotId);
                    return null;
                }
                ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slotId);
                int i = 0;
                for (UsimGroup ug : ugrpList) {
                    if (ug.getRecordIndex() == usimGroupId) {
                        return ug;
                    }
                }
                
                return null;
            }

            int getMaxUsimGroupCount(int slotId) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[getMaxUsimGroupCount]slot is invalid,slot id:" + slotId);
                    return -1;
                }
                return mMaxUsimGroupCount.get(slotId);
            }

            void setMaxUsimGroupCount(int slotId, int count) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[setMaxUsimGroupCount]slot is invalid,slot id:" + slotId);
                    return;
                }
                mMaxUsimGroupCount.put(slotId, count);
            }

            int getMaxUsimGroupNameLength(int slotId) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[getMaxUsimGroupNameLength]slot is invalid,slot id:" + slotId);
                    return -1;
                }
                return mMaxUsimGroupNameLength.get(slotId);
            }

            void setMaxUsimGroupNameLength(int slotId, int length) {
                if (!SlotUtils.isSlotValid(slotId)) {
                    LogUtils.w(TAG, "[setMaxUsimGroupNameLength]slot is invalid,slot id:" + slotId);
                    return;
                }
                mMaxUsimGroupNameLength.put(slotId, length);
            }
        }

        private static final USimGroupArray UGRP_LISTARRAY = new USimGroupArray(SlotUtils.getSlotCount());

        //TODO: Framework interface, here should be change in future.
        public static int hasExistGroup(int slotId, String grpName) throws RemoteException {
            int grpId = -1;
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            LogUtils.d(TAG, "[hasExistGroup]grpName:" + grpName + "|iIccPhb:" + iIccPhb);
            if (TextUtils.isEmpty(grpName) || iIccPhb == null) {
                return grpId;
            }
            
            ArrayList<UsimGroup> ugrpList = UGRP_LISTARRAY.get(slotId);
            LogUtils.d(TAG, "[hasExistGroup]ugrpList---size:" + ugrpList.size());
            if (ugrpList.isEmpty()) {
                List<UsimGroup> uList = iIccPhb.getUsimGroups();
                for (UsimGroup ug : uList) {
                    String gName = ug.getAlphaTag();
                    int gIndex = ug.getRecordIndex();
                    if (!TextUtils.isEmpty(gName) && gIndex > 0) {
                        ugrpList.add(new UsimGroup(gIndex, gName));
                        if (gName.equals(grpName)) {
                            grpId = gIndex;
                        }
                    }
                }
            } else {
                for (UsimGroup ug : ugrpList) {
                    if (grpName.equals(ug.getAlphaTag())) {
                        grpId = ug.getRecordIndex();
                        break;
                    }
                }
            }
            LogUtils.d(TAG, "[hasExistGroup]grpId:" + grpId);
            
            return grpId;
        }

        public static int syncUSIMGroupNewIfMissing(int slotId, String name)
                throws RemoteException, USIMGroupException {
            int nameLen = 0;
            LogUtils.d(TAG, "[syncUSIMGroupNewIfMissing]name:" + name + ",slotId:" + slotId);
            if (TextUtils.isEmpty(name)) {
                LogUtils.i(TAG, "[syncUSIMGroupNewIfMissing]name is null,return.");
                return -1;
            }
            try {
                nameLen = name.getBytes("GBK").length;
            } catch (java.io.UnsupportedEncodingException e) {
                LogUtils.w(TAG, "[syncUSIMGroupNewIfMissing]UnsupportedEncodingException:" + e);
                nameLen = name.length();
            }

            /**hints generic error when phonebook is not ready.*/
            int usimGrpMaxNameLen = getUSIMGrpMaxNameLen(slotId);
            LogUtils.i(TAG, "[syncUSIMGroupNewIfMissing]nameLen:" + nameLen
                    + " ||usimGrpMaxNameLen:" + usimGrpMaxNameLen);
            
            if (usimGrpMaxNameLen == -1) {
                LogUtils.e(TAG, "[syncUSIMGroupNewIfMissing]nameLen:" + nameLen
                        + " ||getUSIMGrpMaxNameLen(slotId) is -1.");
                throw new USIMGroupException(
                        USIMGroupException.ERROR_STR_GRP_GENERIC_ERROR,
                        USIMGroupException.GROUP_GENERIC_ERROR, slotId);
            }
            /***/
            if (nameLen > usimGrpMaxNameLen) {
                throw new USIMGroupException(
                        USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                        USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
            }
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int grpId = -1;
            grpId = hasExistGroup(slotId, name);
            if (grpId < 1 && iIccPhb != null) {
                grpId = iIccPhb.insertUsimGroup(name);
                LogUtils.i(TAG, "[syncUSIMGroupNewIfMissing]inserted grpId:" + grpId);
                if (grpId > 0) {
                    UGRP_LISTARRAY.addItem(slotId, new UsimGroup(grpId, name));
                }
            }
            LogUtils.d(TAG, "[syncUSIMGroupNewIfMissing]grpId:" + grpId);
            if (grpId < 1) {
                switch (grpId) {
                    case USIMGroupException.USIM_ERROR_GROUP_COUNT:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
                                USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);

                        // Name len has been check before new group.
                        // However, do protect here just for full logic.
                    case USIMGroupException.USIM_ERROR_NAME_LEN:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                                USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
                    default:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_GENERIC_ERROR,
                                USIMGroupException.GROUP_GENERIC_ERROR, slotId);
                }
            }
            return grpId;
        }

        /**
         * If a group has to change name, the mapping group of USIM card should
         * also be changed
         * 
         * @return
         */
        public static int syncUSIMGroupUpdate(int slotId, String oldName, String newName)
                throws RemoteException, USIMGroupException {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int grpId = hasExistGroup(slotId, oldName);
            LogUtils.d(TAG, "[syncUSIMGroupUpdate]grpId:" + grpId + "|slotId:" + slotId + "|oldName:" + oldName + "|newName:"
                    + newName);
            if (grpId > 0) {
                int nameLen = 0;
                try {
                    if (!TextUtils.isEmpty(newName)) {
                        nameLen = newName.getBytes("GBK").length;
                    } else {
                        return grpId;
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    LogUtils.w(TAG, "[syncUSIMGroupUpdate]UnsupportedEncodingException:" + e);
                    nameLen = newName.length();
                }
                if (getUSIMGrpMaxNameLen(slotId) < nameLen) {
                    LogUtils.e(TAG, "[syncUSIMGroupUpdate]nameLen:" + nameLen + ",getUSIMGrpMaxNameLen(slotId):" + getUSIMGrpMaxNameLen(slotId));
                    throw new USIMGroupException(
                            USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                            USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
                }
                int ret = iIccPhb.updateUsimGroup(grpId, newName);
                if (ret == USIMGroupException.USIM_ERROR_NAME_LEN) {
                    LogUtils.e(TAG, "[syncUSIMGroupUpdate]ret:" + ret);
                    throw new USIMGroupException(
                            USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
                            USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
                }

                UsimGroup usimGrp = UGRP_LISTARRAY.getItem(slotId, grpId);
                LogUtils.d(TAG, "[syncUSIMGroupUpdate]: usimGrp is null = " + (usimGrp == null));
                if (usimGrp != null) {
                    usimGrp.setAlphaTag(newName);
                }

            }
            return grpId;
        }


        public static int deleteUSIMGroup(int slotId, String name) {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int errCode = -2;
            try {
                int grpId = hasExistGroup(slotId, name);
                if (grpId > 0) {
                    if (iIccPhb.removeUsimGroupById(grpId)) {
                        UGRP_LISTARRAY.removeItem(slotId, grpId);
                        errCode = 0;
                    } else {
                        errCode = -1;
                    }
                }
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[deleteUSIMGroup]RemoteException:" + e);
            }
            LogUtils.i(TAG, "[deleteUSIMGroup]errCode:" + errCode);
            
            return errCode;
        }

        /**
         * Time consuming, run it in background.
         * @param slotId
         * @param simIndex
         * @param grpId
         * @return
         */
        public static boolean addUSIMGroupMember(int slotId, int simIndex, int grpId) {
            boolean succFlag = false;
            try {
                if (grpId > 0) {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                    if (iIccPhb != null) {
                        succFlag = iIccPhb.addContactToGroup(simIndex, grpId);
                    }
                }
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[deleteUSIMGroup]RemoteException:" + e);
                succFlag = false;
            }
            LogUtils.i(TAG, "[addUSIMGroupMember]succFlag:" + succFlag);
            return succFlag;
        }

        /**
         * Time consuming, run in background
         * @param slotId
         * @param simIndex
         * @param grpId
         * @return
         */
        public static boolean deleteUSIMGroupMember(int slotId, int simIndex, int grpId) {
            LogUtils.d(TAG, "[deleteUSIMGroupMember]slotId:" + slotId
                    + "|simIndex:" + simIndex + "|grpId:" + grpId);
            boolean succFlag = false;
            try {
                if (grpId > 0) {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                    if (iIccPhb != null) {
                        succFlag = iIccPhb.removeContactFromGroup(simIndex, grpId);
                    }
                }
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[deleteUSIMGroup]RemoteException:" + e);
                succFlag = false;
            }
            LogUtils.i(TAG, "[deleteUSIMGroupMember]result:" + succFlag + ",slotId:" + slotId
                    + ",simIndex:" + simIndex + ",grpId:" + grpId);
            
            return succFlag;
        }

        /**
         * Move one member from group (fromGrpIds) to other groups (toGrpIds).
         * @param slotId
         * @param simIndex
         * @param fromGrpIds
         * @param toGrpIds
         * @return true succeed, otherwise false.
         */
        public static boolean moveContactToGroups(int slotId, int simIndex, int[] fromGrpIds, int[] toGrpIds) {
            boolean succFlag = false;
            try {
                LogUtils.d(TAG, "[moveUSIMGroupMember]slotId:" + slotId
                        + "|simIndex:" + simIndex + "|fromGrpIds:" + Arrays.toString(fromGrpIds)
                        + "|toGrpIds:" + Arrays.toString(toGrpIds));

                if (fromGrpIds != null && fromGrpIds.length > 0 && toGrpIds != null && toGrpIds.length > 0) {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                    if (iIccPhb != null) {
                        succFlag = iIccPhb.moveContactFromGroupsToGroups(simIndex, fromGrpIds, toGrpIds);
                    }
                } else {
                    LogUtils.d(TAG, "moveUSIMGroupMember: illegal Group information");
                }
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[deleteUSIMGroup]RemoteException:" + e);
                succFlag = false;
            }
            LogUtils.i(TAG, "[deleteUSIMGroup]result:" + succFlag + ",slotId:" + slotId
                    + ",simIndex:" + simIndex);
            
            return succFlag;
        }

        /**
         * Sync USIM group
         * 
         * @param context
         * @param slotId
         * @param grpIdMap The pass in varible must not be null.
         */
        public static synchronized void syncUSIMGroupContactsGroup(Context context,
                final ServiceWorkData workData, HashMap<Integer, Integer> grpIdMap) {
            LogUtils.i(TAG, "[syncUSIMGroupContactsGroup] begin");

            if (workData.mSimType != SimCardUtils.SimType.SIM_TYPE_USIM) {
                return;
            }
            final int slotId = workData.mSlotId;
            final int simId = workData.mSimId;

            ArrayList<UsimGroup> ugrpList = UGRP_LISTARRAY.get(slotId);

            // Get All groups in USIM
            ugrpList.clear();
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            if (iIccPhb == null) {
                LogUtils.w(TAG, "[syncUSIMGroupContactsGroup]iIccPhb is null,return!" );
                return;
            }
            
            try {
                List<UsimGroup> uList = iIccPhb.getUsimGroups();
                if (uList == null) {
                    return;
                }
                for (UsimGroup ug : uList) {
                    String gName = ug.getAlphaTag();
                    int gIndex = ug.getRecordIndex();
                    LogUtils.i(TAG, "[syncUSIMGroupContactsGroup]gName:" + gName + "|gIndex: " + gIndex);

                    if (!TextUtils.isEmpty(gName) && gIndex > 0) {
                        ugrpList.add(new UsimGroup(gIndex, gName));
                    }
                }
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[syncUSIMGroupContactsGroup]catched exception:" );
                e.printStackTrace();
            }

            try {
                LogUtils.i(TAG, "getUSIMGrpMaxNameLen begin");
                UGRP_LISTARRAY.setMaxUsimGroupNameLength(slotId, iIccPhb.getUsimGrpMaxNameLen());
                LogUtils.i(TAG, "getUSIMGrpMaxNameLen end. slot:" + slotId
                        + "|NAME_LENGTH:"
                        + UGRP_LISTARRAY.getMaxUsimGroupNameLength(slotId));
                LogUtils.i(TAG, "getUSIMGrpMaxCount begin.");
                UGRP_LISTARRAY.setMaxUsimGroupCount(slotId, iIccPhb.getUsimGrpMaxCount());
                LogUtils.i(TAG, "getUSIMGrpMaxCount end. slot:" + slotId
                        + "|GROUP_COUNT:"
                        + UGRP_LISTARRAY.getMaxUsimGroupCount(slotId));
            } catch (android.os.RemoteException e) {
                LogUtils.e(TAG, "[syncUSIMGroupContactsGroup]catched exception before setMax." );
                e.printStackTrace();
                UGRP_LISTARRAY.setMaxUsimGroupNameLength(slotId, -1);
                UGRP_LISTARRAY.setMaxUsimGroupCount(slotId, -1);
            }

            // Query SIM info to get simId
            // Query to get all groups in Phone
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Groups.CONTENT_SUMMARY_URI, null,
                    Groups.DELETED + "=0 AND " +
                            Groups.ACCOUNT_TYPE + "='USIM Account' AND "
                            + Groups.ACCOUNT_NAME + "=" + "'USIM" + slotId + "'", null, null);
            // Query all Group including deleted group

            HashMap<String, Integer> noneMatchedMap = new HashMap<String, Integer>();
            if (c != null) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    String grpName = c.getString(c.getColumnIndexOrThrow(Groups.TITLE));
                    int grpId = c.getInt(c.getColumnIndexOrThrow(Groups._ID));
                    if (!noneMatchedMap.containsKey(grpName)) {
                        noneMatchedMap.put(grpName, grpId);
                    }
                }
                c.close();
            }

            if (ugrpList != null) {
                boolean hasMerged = false;
                for (UsimGroup ugrp : ugrpList) {
                    String ugName = ugrp.getAlphaTag();
                    hasMerged = false;
                    long groupId = -1;
                    if (!TextUtils.isEmpty(ugName)) {
                        int ugId = ugrp.getRecordIndex();
                        if (noneMatchedMap.containsKey(ugName)) {
                            groupId = noneMatchedMap.get(ugName);
                            noneMatchedMap.remove(ugName);
                            hasMerged = true;
                        }

                        if (!hasMerged) {
                            // Need to create on phone
                            ContentValues values = new ContentValues();
                            values.put(Groups.TITLE, ugName);
                            values.put(Groups.GROUP_VISIBLE, 1);
                            values.put(Groups.SYSTEM_ID, 0);
                            values.put(Groups.ACCOUNT_NAME, "USIM" + slotId);
                            values.put(Groups.ACCOUNT_TYPE, "USIM Account");
                            Uri uri = cr.insert(Groups.CONTENT_URI, values);
                            groupId = (uri == null) ? 0 : ContentUris.parseId(uri);
                        }
                        if (groupId > 0) {
                            grpIdMap.put(ugId, (int) groupId);
                        }
                    }
                }

                if (noneMatchedMap.size() > 0) {
                    Integer[] groupIdArray = noneMatchedMap.values().toArray(new Integer[0]);
                    StringBuilder delGroupIdStr = new StringBuilder();
                    for (Integer i : groupIdArray) {
                        int delGroupId = i;
                        delGroupIdStr.append(delGroupId).append(",");
                    }
                    if (delGroupIdStr.length() > 0) {
                        delGroupIdStr.deleteCharAt(delGroupIdStr.length() - 1);
                    }
                    if (delGroupIdStr.length() > 0) {
                        cr.delete(Groups.CONTENT_URI, Groups._ID + " IN ("
                                + delGroupIdStr.toString() + ")", null);
                    }
                }
                LogUtils.i(TAG, "[syncUSIMGroupContactsGroup] end.");
            } else {
                deleteUSIMGroupOnPhone(context, slotId);
            }
        }

        public static int getUSIMGrpMaxNameLen(int slot) {
            if (!SlotUtils.isSlotValid(slot)) {
                LogUtils.w(TAG, "[getUSIMGrpMaxNameLen]slotId invalid: " + slot);
                return -1;
            }
            
            if (!SimCardUtils.isPhoneBookReady(slot)) {
                LogUtils.w(TAG, "[getUSIMGrpMaxNameLen]phoneBookReady:false |slot:" + slot);
                UGRP_LISTARRAY.setMaxUsimGroupNameLength(slot, -1);
                return -1;
            }
            /**
             * M: change for SIM Service Refactoring
             * Sometimes after turn off airplane the phb has ready, 
             * but iIccPhb.getUsimGrpMaxNameLen() will be 0;
             * origin code: if (UGRP_LISTARRAY.getMaxUsimGroupNameLength(slot) < 0)
             */
            if (UGRP_LISTARRAY.getMaxUsimGroupNameLength(slot) <= 0) {
                try {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
                    if (iIccPhb != null) {
                        UGRP_LISTARRAY.setMaxUsimGroupNameLength(slot, iIccPhb.getUsimGrpMaxNameLen());
                    }
                } catch (android.os.RemoteException e) {
                    LogUtils.e(TAG, "[getUSIMGrpMaxNameLen]RemoteException:" + e);
                    UGRP_LISTARRAY.setMaxUsimGroupNameLength(slot, -1);
                }
            }
            LogUtils.d(TAG, "[getUSIMGrpMaxNameLen]end slot:" + slot
                    + "|maxNameLen:" + UGRP_LISTARRAY.getMaxUsimGroupNameLength(slot));
            
            return UGRP_LISTARRAY.getMaxUsimGroupNameLength(slot);
        }

        public static void deleteUSIMGroupOnPhone(Context context, int slotId) {
            ContentResolver cr = context.getContentResolver();
            cr.delete(Groups.CONTENT_URI, Groups.ACCOUNT_TYPE + "='USIM Account' AND "
                    + Groups.ACCOUNT_NAME + "=" + "'USIM" + slotId + "'", null);
        }

    }


    public static class USIMGroupException extends Exception {
        private static final long serialVersionUID = 1L;

        public static final String ERROR_STR_GRP_NAME_OUTOFBOUND = "Group name out of bound";
        public static final String ERROR_STR_GRP_COUNT_OUTOFBOUND = "Group count out of bound";
        public static final String ERROR_STR_GRP_GENERIC_ERROR = "Group generic error";
        
        public static final int GROUP_NAME_OUT_OF_BOUND = 1;
        public static final int GROUP_NUMBER_OUT_OF_BOUND = 2;
        public static final int GROUP_GENERIC_ERROR = 3;
        public static final int GROUP_SIM_ABSENT = 4;
        // Exception type definination in framework.
        public static final int USIM_ERROR_NAME_LEN = UsimPhoneBookManager.USIM_ERROR_NAME_LEN;
        public static final int USIM_ERROR_GROUP_COUNT = UsimPhoneBookManager.USIM_ERROR_GROUP_COUNT;

        int mErrorType;
        int mSlotId;

        public USIMGroupException(String msg, int errorType, int slotId) {
            super(msg);
            mErrorType = errorType;
            mSlotId = slotId;
        }

        public int getErrorType() {
            return mErrorType;
        }

        public int getErrorSlotId() {
            return mSlotId;
        }

        @Override
        public String getMessage() {
            return "Details message: errorType:" + mErrorType + "\n"
                    + super.getMessage();
        }

        public static int getErrorToastId(int errorType) {
            int retMsgId;
            switch(errorType) {
                case GROUP_NAME_OUT_OF_BOUND:
                    retMsgId = R.string.usim_group_name_exceed_limit;
                    break;
                case GROUP_NUMBER_OUT_OF_BOUND:
                    retMsgId = R.string.usim_group_count_exceed_limit;
                    break;
                /** M: ALPS921231 show sim removed toast @{ */
                case GROUP_SIM_ABSENT:
                    retMsgId = R.string.callFailed_simError;
                    break;
                /**@}*/
                default:
                    retMsgId = R.string.generic_failure;
            }
            
            return retMsgId;
        }
    }

    public static class ContactsGroupArrayData {
        private int mSimIndex;
        private int mSimIndexPhoneOrSim;

        public int getmSimIndex() {
            return mSimIndex;
        }

        public int getmSimIndexPhoneOrSim() {
            return mSimIndexPhoneOrSim;
        }

        public ContactsGroupArrayData initData(int simIndex, int mSimIndexPhoneorSim) {
            mSimIndex = simIndex;
            mSimIndexPhoneOrSim = mSimIndexPhoneorSim;
            return this;
        }
    }

    /**
     * Used to get max counts of USIM group.
     * @param slotId
     * @return
     */
    public static int getUsimMaxGrpCount(int slotId) {
        int count = 0;
        final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
        if (iIccPhb == null) {
            LogUtils.i(TAG, "[getUsimMaxGrpCount]IIccPhoneBook is null,return.");
            return count;
        }

        try {
            count = iIccPhb.getUsimGrpMaxCount();
        } catch (android.os.RemoteException e) {
            LogUtils.i(TAG, "[getUsimMaxGrpCount]IIccPhoneBook exception!");
            e.printStackTrace();
        }

        return count;
    }
}
