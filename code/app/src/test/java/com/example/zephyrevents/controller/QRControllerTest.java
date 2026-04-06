package com.example.zephyrevents.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

/**
 * JVM unit tests: {@link Bitmap}, {@link Uri}, and {@link Intent} are stubs. Use {@link MockedStatic}
 * / {@link MockedConstruction} so {@link QRController} can run without Robolectric.
 * Stub {@link Intent#getStringExtra} does not return values; verify {@link Intent#putExtra} instead.
 */
public class QRControllerTest {

    @Test
    public void getEventIntentFromUri_validLink_returnsIntentWithEventId() {
        Context context = Mockito.mock(Context.class);
        String raw = "zephyrevents://eventdetails?id=evt-123";

        try (MockedStatic<Uri> uriStatic = Mockito.mockStatic(Uri.class);
             MockedConstruction<Intent> intentConstruction = Mockito.mockConstruction(Intent.class)) {

            Uri fakeUri = mock(Uri.class);
            when(fakeUri.getQueryParameter("id")).thenReturn("evt-123");
            uriStatic.when(() -> Uri.parse(raw)).thenReturn(fakeUri);

            Intent intent = QRController.getEventIntentFromUri(context, raw);

            assertNotNull(intent);
            List<Intent> built = intentConstruction.constructed();
            assertEquals(1, built.size());
            assertSame(intent, built.get(0));
            // Must match EventDetailViewActivity.EXTRA_EVENT ("extra_event")
            verify(intent).putExtra("extra_event", "evt-123");
        }
    }

    @Test
    public void getEventIntentFromUri_wrongPrefix_returnsNull() {
        Context context = Mockito.mock(Context.class);
        assertNull(QRController.getEventIntentFromUri(context, "https://example.com/event/1"));
    }

    @Test
    public void getEventIntentFromUri_missingId_returnsNull() {
        Context context = Mockito.mock(Context.class);
        String raw = "zephyrevents://eventdetails?other=x";

        try (MockedStatic<Uri> uriStatic = Mockito.mockStatic(Uri.class)) {
            Uri fakeUri = mock(Uri.class);
            when(fakeUri.getQueryParameter("id")).thenReturn(null);
            uriStatic.when(() -> Uri.parse(raw)).thenReturn(fakeUri);

            assertNull(QRController.getEventIntentFromUri(context, raw));
        }
    }

    @Test
    public void generateQRCode_returnsBitmapWithRequestedSize() {
        try (MockedStatic<Bitmap> bitmapStatic = Mockito.mockStatic(Bitmap.class)) {
            bitmapStatic
                    .when(() -> Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class)))
                    .thenAnswer(invocation -> {
                        int w = invocation.getArgument(0);
                        int h = invocation.getArgument(1);
                        Bitmap b = mock(Bitmap.class);
                        when(b.getWidth()).thenReturn(w);
                        when(b.getHeight()).thenReturn(h);
                        return b;
                    });

            Bitmap bitmap = QRController.generateQRCode("test-content", 64);

            assertNotNull(bitmap);
            assertEquals(64, bitmap.getWidth());
            assertEquals(64, bitmap.getHeight());
        }
    }

    @Test
    public void generateEventQRCode_scalesToDisplaySize() {
        try (MockedStatic<Bitmap> bitmapStatic = Mockito.mockStatic(Bitmap.class)) {
            bitmapStatic
                    .when(() -> Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class)))
                    .thenAnswer(invocation -> {
                        int w = invocation.getArgument(0);
                        int h = invocation.getArgument(1);
                        Bitmap b = mock(Bitmap.class);
                        when(b.getWidth()).thenReturn(w);
                        when(b.getHeight()).thenReturn(h);
                        return b;
                    });
            bitmapStatic
                    .when(() -> Bitmap.createScaledBitmap(any(Bitmap.class), anyInt(), anyInt(), anyBoolean()))
                    .thenAnswer(invocation -> {
                        int size = invocation.getArgument(1);
                        Bitmap b = mock(Bitmap.class);
                        when(b.getWidth()).thenReturn(size);
                        when(b.getHeight()).thenReturn(size);
                        return b;
                    });

            Bitmap bitmap = QRController.generateEventQRCode("event-abc", 128);

            assertNotNull(bitmap);
            assertEquals(128, bitmap.getWidth());
            assertEquals(128, bitmap.getHeight());
        }
    }
}
