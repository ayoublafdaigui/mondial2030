package com.mondial2030.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Utility class for generating QR codes using the ZXing library.
 * QR codes can be used for ticket validation using blockchain hashes.
 */
public class QRCodeGenerator {

    private static final int DEFAULT_WIDTH = 250;
    private static final int DEFAULT_HEIGHT = 250;
    private static final String DEFAULT_FORMAT = "PNG";

    /**
     * Generates a QR code image from the given text (e.g., blockchain hash).
     * Returns a JavaFX Image that can be displayed directly in the UI.
     *
     * @param text The text to encode in the QR code (typically a blockchain_hash)
     * @return JavaFX Image containing the QR code
     * @throws WriterException if QR code generation fails
     * @throws IOException if image conversion fails
     */
    public static Image generateQRCodeImage(String text) throws WriterException, IOException {
        return generateQRCodeImage(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generates a QR code image with custom dimensions.
     *
     * @param text   The text to encode in the QR code
     * @param width  The width of the QR code image
     * @param height The height of the QR code image
     * @return JavaFX Image containing the QR code
     * @throws WriterException if QR code generation fails
     * @throws IOException if image conversion fails
     */
    public static Image generateQRCodeImage(String text, int width, int height) 
            throws WriterException, IOException {
        
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text to encode cannot be null or empty");
        }

        // Configure QR code generation hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); // Quiet zone margin

        // Generate QR code matrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // Convert BitMatrix to BufferedImage
        BufferedImage bufferedImage = toBufferedImage(bitMatrix);

        // Convert BufferedImage to JavaFX Image
        return convertToFxImage(bufferedImage);
    }

    /**
     * Generates a QR code with custom colors.
     *
     * @param text        The text to encode
     * @param width       The width of the QR code
     * @param height      The height of the QR code
     * @param foreground  The foreground color (QR code modules)
     * @param background  The background color
     * @return JavaFX Image containing the QR code
     * @throws WriterException if QR code generation fails
     * @throws IOException if image conversion fails
     */
    public static Image generateQRCodeImage(String text, int width, int height, 
            Color foreground, Color background) throws WriterException, IOException {
        
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text to encode cannot be null or empty");
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = toBufferedImage(bitMatrix, foreground.getRGB(), background.getRGB());

        return convertToFxImage(bufferedImage);
    }

    /**
     * Generates a QR code for a ticket with blockchain validation.
     * Embeds ticket information in a formatted string.
     *
     * @param ticketId      The ticket ID
     * @param blockchainHash The blockchain hash for validation
     * @param matchInfo     Additional match information
     * @return JavaFX Image containing the QR code
     * @throws WriterException if QR code generation fails
     * @throws IOException if image conversion fails
     */
    public static Image generateTicketQRCode(Long ticketId, String blockchainHash, String matchInfo) 
            throws WriterException, IOException {
        
        // Create a JSON-like structure for the ticket data
        String qrContent = String.format(
            "{\"ticketId\":%d,\"hash\":\"%s\",\"match\":\"%s\",\"timestamp\":%d}",
            ticketId,
            blockchainHash,
            matchInfo,
            System.currentTimeMillis()
        );
        
        return generateQRCodeImage(qrContent);
    }

    /**
     * Converts a BitMatrix to a BufferedImage with default black/white colors.
     */
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        return toBufferedImage(matrix, Color.BLACK.getRGB(), Color.WHITE.getRGB());
    }

    /**
     * Converts a BitMatrix to a BufferedImage with custom colors.
     */
    private static BufferedImage toBufferedImage(BitMatrix matrix, int foregroundColor, int backgroundColor) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? foregroundColor : backgroundColor);
            }
        }
        
        return image;
    }

    /**
     * Converts a BufferedImage to a JavaFX Image.
     */
    private static Image convertToFxImage(BufferedImage bufferedImage) throws IOException {
        // Method 1: Using SwingFXUtils (preferred if available)
        try {
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            // Fallback: Convert through byte array
            return convertViaByteArray(bufferedImage);
        }
    }

    /**
     * Fallback conversion method using byte array.
     */
    private static Image convertViaByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, DEFAULT_FORMAT, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return new Image(inputStream);
    }

    /**
     * Generates a QR code and returns it as a byte array (useful for saving to file or database).
     *
     * @param text   The text to encode
     * @param width  The width of the QR code
     * @param height The height of the QR code
     * @return byte array containing the QR code image in PNG format
     * @throws WriterException if QR code generation fails
     * @throws IOException if image conversion fails
     */
    public static byte[] generateQRCodeBytes(String text, int width, int height) 
            throws WriterException, IOException {
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = toBufferedImage(bitMatrix);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, DEFAULT_FORMAT, outputStream);
        
        return outputStream.toByteArray();
    }
}
