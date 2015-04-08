package com.example.csuay.twear.utils;

import android.graphics.Bitmap;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by csuay on 08/04/15.
 */
public class TwearUtils {
    static final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    public static Date parseTwitterDate(String date) {
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        Date ret = null;
        try {
            ret = sf.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
        return ret;
    }

    public static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}
