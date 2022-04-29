package com.team8.mcq_scanner.app.managers;

import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_USERNAME;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.team8.mcq_scanner.app.ImageViewerActivity;
import com.team8.mcq_scanner.app.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Utills {

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        // Create a new LinkedHashSet

        // Add the elements to set
        Set<T> set = new LinkedHashSet<>(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }

    public static void setProfileImage(Context context, String imgUrl, ImageView mImageView) {
        try {

            if (imgUrl != null) {
//                Picasso.get().load(imgUrl).fit().placeholder(R.drawable.profile_avatar).into(mImageView);
                Glide.with(context).load(imgUrl).placeholder(R.drawable.ic_baseline_image_24)
                        .thumbnail(0.5f)
                        .into(mImageView);
            } else {
//                Picasso.get().load(R.drawable.profile_avatar).fit().into(mImageView);
                Glide.with(context).load(R.drawable.ic_baseline_image_24).diskCacheStrategy(DiskCacheStrategy.ALL).into(mImageView);
            }
        } catch (Exception ignored) {
        }
    }
    public static void getErrors(final Exception e) {
        if (true) {
            final String stackTrace = "Pra ::" + Log.getStackTraceString(e);
            System.out.println(stackTrace);
        }
    }
    public static String getDateTime() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date date = new Date();

        return dateFormat.format(date);
    }

    public static String getCapsWord(String name) {
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static void openFullImageViewActivity(final View view, final String imgPath, final String username, final Context context) {
        openFullImageViewActivity(view, imgPath, "", username, context);
    }

    public static void openFullImageViewActivity(final View view, final String imgPath, final String groupName, final String username,final Context context) {
        final Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMGPATH, imgPath);
        intent.putExtra(EXTRA_USERNAME, username);
        try {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, view, username);
            context.startActivity(intent, options.toBundle());
        } catch (Exception e) {
            context.startActivity(intent);
        }
    }

    public static boolean isEmpty(final Object s) {
        if (s == null) {
            return true;
        }
        if ((s instanceof String) && (((String) s).trim().length() == 0)) {
            return true;
        }
        if (s instanceof Map) {
            return ((Map<?, ?>) s).isEmpty();
        }
        if (s instanceof List) {
            return ((List<?>) s).isEmpty();
        }
        if (s instanceof Object[]) {
            return (((Object[]) s).length == 0);
        }
        return false;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void setWindow(final Window w) {
        //make status bar transparent
//        w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(ContextCompat.getColor(w.getContext(), R.color.black));
        w.setNavigationBarColor(ContextCompat.getColor(w.getContext(), R.color.black));
    }

    public static String getExtension(Context context, final Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
