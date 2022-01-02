package com.example.licnesingservice.utils;

import com.example.licnesingservice.service.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeoutException;


public class LicenseUtils {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);
    public static  void randomlyRunLong(){
        Random rand = new Random();
        int randomNum = rand.nextInt(3) + 1;
        if (randomNum==3) sleep();
    }

    public static void sleep(){
        try {
            Thread.sleep(5000);
            throw new java.util.concurrent.TimeoutException();
        } catch (InterruptedException | TimeoutException e) {
            logger.error(e.getMessage());
        }
    }
}
