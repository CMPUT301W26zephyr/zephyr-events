package com.example.zephyrevents.controller;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.view.EventDetailViewActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Handles creation and parsing of QR codes
 */
public class QRController {
    private static final String EVENT_LINK_PREFIX = "zephyrevents://eventdetails?id=";

    /**
     * Public constructor for use within Android Activities or Fragments.
     */
    public QRController() {}

    /**
     * Creates QR code Bitmap for a an event from the event ID.
     * @param eventID The ID of the event
     * @param displaySize The minimum size of the desired image
     * @return A bitmap linking to the event
     */
    public static Bitmap generateEventQRCode(String eventID, int displaySize) {
        Bitmap smallBitmap = generateQRCode(EVENT_LINK_PREFIX + eventID, 41);
        if (smallBitmap == null) return null;
//        displaySize = Math.ceilDiv(displaySize, smallBitmap.getHeight()) * smallBitmap.getHeight();  // assuming square
        return Bitmap.createScaledBitmap(smallBitmap, displaySize, displaySize, false);
    }

    /**
     * Creates QR code Bitmap for a QR code from string, given a size.
     * @param content: Text content of QR code.
     */
    public static Bitmap generateQRCode(String content, int size) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size, size);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a scanned string from an Event QR code and returns an Intent to its DetailViewActivity
     * @param context Context for making Intent
     * @param rawUri Raw uri
     */
    public static Intent getEventIntentFromUri(Context context, String rawUri) {
        if (rawUri.startsWith(EVENT_LINK_PREFIX)) {
            Uri uri = Uri.parse(rawUri);
            String eventId = uri.getQueryParameter("id");

            if (eventId != null) {
                Intent intent = new Intent(context, EventDetailViewActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                return intent;
            }
        }
        return null;
    }

    public static void saveQRCodeImage(Context context, Bitmap qrBitmap, String eventId) {
        String filename = "EventQR_" + eventId + "_" + System.currentTimeMillis() + ".png";
        OutputStream fos;

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            // NOTE: Android 10 and above
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ZephyrEvents");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                fos = context.getContentResolver().openOutputStream(imageUri);
                assert fos != null;
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                // Release the "pending" status for Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    context.getContentResolver().update(imageUri, values, null, null);
                }

                Toast.makeText(context, "QR Code saved to Gallery in Pictures/ZephyrEvents", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
