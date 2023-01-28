package com.runjian.common.utils;

import java.util.UUID;

/**
 * @author chenjialing
 */
public class UuidUtil {

    public  static String toUuid(){
        String s= UUID.randomUUID().toString();

        return s.replace("-", "");
    }
}
