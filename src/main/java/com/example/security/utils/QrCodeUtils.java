package com.example.security.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 该类是关于二维码的生成（图片型和字节型） 以及  二维码的解析（解析的格式是图片）
 * @author ddd
 */
public class QrCodeUtils extends LuminanceSource {

    /**
     * private static final String PATH = "C:\\Users\\yinhj\\Desktop\\newMan\\MyQRCode.png";*/
    private static final String CHARACTER_SET = "utf-8";
    private static Hashtable hints = new Hashtable<>();
    static {
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET,CHARACTER_SET);
        hints.put(EncodeHintType.MARGIN,1);
    }

    private final BufferedImage IMAGE;
    private final int LEFT;
    private final int TOP;

    public QrCodeUtils(BufferedImage image){

        this(image.getWidth(),image.getHeight(),image,0,0);
    }

    public QrCodeUtils(int width, int height, BufferedImage image, int left, int top) {
        super(width, height);

        int sourceHeight = image.getHeight();
        int sourceWidth = image.getWidth();

        if(left+width>sourceWidth || top+height>sourceHeight){
            throw new IllegalArgumentException("Crop rectangle does not fit within image data");
        }

        for(int y = top;y<top+height;y++){
            for(int x = left;x<left+width;x++){
                if((image.getRGB(x,y) & 0xFF000000)==0){
                    image.setRGB(x,y,0xFF000000);
                }
            }
        }

        IMAGE = new BufferedImage(sourceWidth,sourceHeight,BufferedImage.TYPE_BYTE_GRAY);
        IMAGE.getGraphics().drawImage(image,0,0,null);
        LEFT = left;
        TOP = top;
    }

    /**
     *
     * @param text
     * @param width
     * @param height
     * @param path
     * @throws WriterException
     * @throws IOException
     *
     * 生成png图片
     */
    public static void generate(String text,int width,int height,String path) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height,hints);
        Path path1 = FileSystems.getDefault().getPath(path);
        MatrixToImageWriter.writeToPath(bitMatrix,"PNG",path1);
    }

    /**
     *
     * @param text
     * @param width
     * @param height
     * @return
     * @throws WriterException
     * @throws IOException
     *
     * 生成字节二维码
     */
    public static byte[] getCodeImage(String text,int width,int height) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix,"PNG",byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return bytes;
    }

    /**
     *
     * @param filePaths
     *
     * 解析二维码图片（可以解析多张）
     */
    public static void decodeImage(List<String> filePaths){

        for(int i = 0;i<filePaths.size();i++){
            File file = new File(filePaths.get(i)   );
            BufferedImage image = null;
            try {
                image = ImageIO.read(file);
                decodeCommon(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param image
     * 解析二进制图片
     */
    public static void decodeImage(byte[] image){

        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            decodeCommon(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decodeCommon(BufferedImage bufferedImage){
        try {
            MultiFormatReader formatReader = new MultiFormatReader();

//                BufferedImageLuminanceSource luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            QrCodeUtils qrCodeUtils = new QrCodeUtils(bufferedImage);
            HybridBinarizer binaries = new HybridBinarizer(qrCodeUtils);
            BinaryBitmap bitmap = new BinaryBitmap(binaries);

            Map hints = new HashMap(10);
            hints.put(EncodeHintType.CHARACTER_SET,"utf-8");

            Result result = formatReader.decode(bitmap, hints);
            System.out.println(result.toString());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getRow(int i, byte[] bytes) {

        if(i<0 || i>getHeight()){
            throw new IllegalArgumentException("Requested row is outside the image: " + i);
        }

        int width = getWidth();
        if(bytes == null || bytes.length < width){
            bytes = new byte[width];
        }
        IMAGE.getRaster().getDataElements(LEFT,TOP+i,width,1,bytes);
        return bytes;
    }

    @Override
    public byte[] getMatrix() {

        int height = getHeight();
        int width = getWidth();
        int area = height * width;
        byte[] matrix = new byte[area];
        IMAGE.getRaster().getDataElements(LEFT,TOP,width,height,matrix);

        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }

    @Override
    public LuminanceSource crop(int left, int top, int width, int height) {
        return new QrCodeUtils(width,height,IMAGE,LEFT+left,TOP+top);
    }

    @Override
    public boolean isRotateSupported() {
        return true;
    }

    @Override
    public LuminanceSource rotateCounterClockwise() {
        int height = IMAGE.getHeight();
        int width = IMAGE.getWidth();

        AffineTransform affineTransform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, width);
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = rotatedImage.createGraphics();
        graphics.drawImage(IMAGE,affineTransform,null);
        graphics.dispose();

        int width1 = getWidth();
        return new BufferedImageLuminanceSource(rotatedImage,TOP,width-(LEFT+width1),getHeight(),width1);
    }
}
