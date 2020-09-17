package com.example.security;

import com.example.security.utils.QrCodeUtils;
import com.google.zxing.*;

import java.io.IOException;

/**
 * @author ddd
 */
public class App {

    public static void main(String[] args) {

        String data = "{\n" +
                "  \"lastName\" : \"cheng\",\n" +
                "  \"firstName\" : \"chen\",\n" +
                "  \"birth\" : \"1999-09-06\"\n" +
                "}";

        System.out.println(data);
    }
}