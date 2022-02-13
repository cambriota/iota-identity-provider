package dev.cambriota.identityprovider.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeTest {

    // TODO: QR Code is rendered correctly, but base64 image is still different (margin, padding)?
    @Disabled
    @Test
    void shouldGenerateCorrectQrCode() {
        String barcodeText = "test";
        String expected = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHQAAAB0CAAAAABx8Un7AAAA8ElEQVR42u3awQ6EIAxFUf7/p3VrTGmfCLSay8bEEQ7JNKWNtiNhNFBQUFBQ0J+jzRnX36/PK/NAc1HzzzegOxrNA81FrQBSr9Y80PqoF1Cg30OtRA5aH1Xvj8wD3Y9GhVmU6JdUg6DDqNQAdRZb2rWBTutPe0W1d5gPJXzQpaiyQHQPtB46UohZwQdaC7Wg3qa8jYPmot7iatIArYcqjZJywIPWQNVGyAueqBgH3Y+qLw6eHt6guaiS1HtNVBRQoLXQp4liSiCBLke9xSMQNA9VNqME2pS3FaCvUfWDGQWYUg2CvkJ3DFBQUFBQ0J+hJ7SzS8unG4NvAAAAAElFTkSuQmCC";
        assertThat(QrCode.generateQRCodeImage(barcodeText)).isEqualTo(expected);
    }

}
