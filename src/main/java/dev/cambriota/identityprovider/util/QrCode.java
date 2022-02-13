package dev.cambriota.identityprovider.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QrCode {
    private static final Logger log = Logger.getLogger(QrCode.class);
    private static final String PREFIX = "data:image/png;base64,";

    public static String generateQRCodeImage(String barcodeText) {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            baos.close();
            return PREFIX + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warnf("QR Code could not be generated for value: %s", barcodeText);
        }
        return "";
    }
}
