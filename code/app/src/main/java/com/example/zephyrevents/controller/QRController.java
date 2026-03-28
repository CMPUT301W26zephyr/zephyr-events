package com.example.zephyrevents.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.zephyrevents.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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
            return null;
        }
    }
}
