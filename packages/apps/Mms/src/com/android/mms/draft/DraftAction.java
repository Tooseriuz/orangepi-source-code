package com.android.mms.draft;

import java.io.InputStream;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.util.Log;

import com.android.mms.model.SlideshowModel;
import com.android.mms.LogTag;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MmsPreferenceActivity;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.google.android.mms.MmsException;
import com.google.android.mms.util.SqliteWrapper;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.mediatek.encapsulation.MmsLog;

/*******************************************************************************
 *
 * which used to do really save and load action
 * these code are copy from WorkingMessage.java 
 *
*******************************************************************************/
public class DraftAction {

    private final static boolean DEBUG = true;

    private final static String TAG = "[Mms][Draft][DraftAction]";

    // Draft message stuff
    private static final String[] MMS_DRAFT_PROJECTION = {
            Mms._ID, // 0
            Mms.SUBJECT, // 1
            Mms.SUBJECT_CHARSET // 2
    };

    private static final int MMS_ID_INDEX = 0;
    private static final int MMS_SUBJECT_INDEX = 1;
    private static final int MMS_SUBJECT_CS_INDEX = 2;

    private SlideshowModel mSlideshow;

    public Uri readDraftMmsMessage(final Context context, final long threadId, StringBuilder sb) {

        long startTime = System.currentTimeMillis();
        Log.d(TAG, "[readDraftMmsMessage] begin");

        if (threadId <= 0) {
            Log.d(TAG, "[readDraftMmsMessage] threadId <= 0");
            return null;
        }

        Cursor cursor;
        ContentResolver cr = context.getContentResolver();

        final String selection = Mms.THREAD_ID + " = " + threadId;
        cursor = SqliteWrapper.query(context, cr, Mms.Draft.CONTENT_URI,
                MMS_DRAFT_PROJECTION, selection, null, null);

        if (cursor == null) {
            Log.d(TAG, "[readDraftMmsMessage] cursor is null");
            return null;
        }

        Uri uri;
        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    uri = ContentUris.withAppendedId(Mms.Draft.CONTENT_URI,
                            cursor.getLong(MMS_ID_INDEX));
                    String subject = MessageUtils.extractEncStrFromCursor(
                            cursor, MMS_SUBJECT_INDEX, MMS_SUBJECT_CS_INDEX);
                    if (subject != null) {
                        sb.append(subject);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "[readDraftMmsMessage] return uri : " + uri);
                        Log.d(TAG, "[readDraftMmsMessage] return subject : " + sb);
                    }
                    long costTime = System.currentTimeMillis() - startTime;
                    Log.d(TAG, "[readDraftMmsMessage] end and return uri, and cost : " + costTime);
                    return uri;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        long costTime1 = System.currentTimeMillis() - startTime;
        Log.d(TAG, "[readDraftMmsMessage] end and return null, and cost : " + costTime1);
        return null;
    }

    public boolean loadFromUri(final Context context, final Uri uri) {
        if (uri == null) {
            Log.d(TAG, "[loadFromUri] uri which to load is null");
            return false;
        }

        if (DEBUG) {
            Log.d(TAG, "[loadFromUri] uri : " + uri);
        }

        try {
            mSlideshow = SlideshowModel.createFromMessageUri(context, uri);
            if (mSlideshow != null) {
                Log.d(TAG, "loadFromUri Slideshow num = " + mSlideshow.size());
            } else {
                Log.d(TAG, "loadFromUri Slideshow = null");
            }
        } catch (MmsException e) {
            LogTag.error("Couldn't load URI %s", uri);
            return false;
        }

        return true;
    }

    public Uri createDraftMmsMessage(PduPersister persister, SendReq sendReq,
            SlideshowModel slideshow, Uri preUri, Context context) throws MmsException {
        if (slideshow == null || persister == null || sendReq == null) {
            Log.d(TAG, "[createDraftMmsMessage] parameters is not correct");
            return null;
        }

        try {
            long pbStartTime = System.currentTimeMillis();

            PduBody pb = slideshow.toPduBody();

            long pbCostTime = System.currentTimeMillis() - pbStartTime;
            Log.d(TAG, "[createDraftMmsMessage] slideshow.toPduBody() cost : " + pbCostTime);

            sendReq.setBody(pb);

            long persistStartTime = System.currentTimeMillis();

            Uri res = persister.persist(sendReq, preUri == null ? Mms.Draft.CONTENT_URI : preUri,
                    true, MmsPreferenceActivity.getIsGroupMmsEnabled(context), null);

            long persistCostTime = System.currentTimeMillis() - persistStartTime;
            Log.d(TAG, "[createDraftMmsMessage] persist to DB total cost : " + persistCostTime);

            slideshow.sync(pb);
            slideshow.resetUpdateState();
            return res;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void updateDraftMmsMessage(final Uri uri, PduPersister persister,
            SlideshowModel slideshow, SendReq sendReq) {
        if (uri == null || persister == null || sendReq == null) {
            Log.d(TAG, "[updateDraftMmsMessage] parameters maybe not correct");
            return;
        }
        Log.d(TAG, "[updateDraftMmsMessage] begin");

        /// M: @{
        // persister.updateHeaders(uri, sendReq);
        long updateHeadersStart = System.currentTimeMillis();
        try {
            persister.updateHeaders(uri, sendReq);
        } catch (IllegalArgumentException e) {
            MmsLog.e(TAG, "updateDraftMmsMessage: cannot update message " + uri);
        }
        long updateHeadersCost = System.currentTimeMillis() - updateHeadersStart;
        Log.d(TAG, "[updateDraftMmsMessage] update headers total cost : " + updateHeadersCost);

        if (slideshow == null) {
            Thread.dumpStack();
            Log.e(TAG, "updateDraftMmsMessage, oops slideshow is null");
            Log.e(TAG, "updateDraftMmsMessage, sendreq " + sendReq);
            return;
        }

        final PduBody pb = slideshow.toPduBody();
        long updatePartsStart = System.currentTimeMillis();
        try {
            MessageUtils.updatePartsIfNeeded(slideshow, persister, uri, pb, null);
            //persister.updateParts(uri, pb, null);
        } catch (MmsException e) {
            Log.e(TAG, "updateDraftMmsMessage: cannot update message " + uri);
        }
        slideshow.sync(pb);
        long updatePartsCost = System.currentTimeMillis() - updatePartsStart;
        Log.d(TAG, "[updateDraftMmsMessage] update parts total cost : " + updatePartsCost);
    }

    public SlideshowModel getSlideshow() {
        return mSlideshow;
    }

}
