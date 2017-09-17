package com.example.yinqinghao.childprotect.QRHelper;

import android.graphics.Bitmap;
import java.util.Map;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * Created by yinqinghao on 17/9/17.
 */

public class QRHelper {
    /**
     *
     * @param bMap
     *
     * @return Qr Code value
     *
     */
    public String readQRCode(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(bitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (result != null)
            contents = result.getText();
        return contents;
    }
}
