package com.example.security.controller;

import com.example.security.utils.QrCodeUtils;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Hashtable;

/**
 * @author ddd
 */
@Controller
@RequestMapping("/api")
public class QrCodeController {

    @GetMapping("qrcode")
    public String qrcode(){
        return "/qrcode";
    }


    @GetMapping("qrimage")
    public ResponseEntity<byte[]> get(){

        Hashtable hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET,"utf-8");
        hints.put(EncodeHintType.MARGIN,1);

        byte[] qrcode = null;
        try {
            qrcode = QrCodeUtils.getCodeImage("http://www.baidu.com",350,350);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<byte[]>(qrcode,httpHeaders, HttpStatus.CREATED);
    }
}
