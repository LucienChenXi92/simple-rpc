package com.lucien.learning.simplerpc.service.impl;

import com.lucien.learning.simplerpc.service.LoginService;
import org.apache.logging.log4j.util.Strings;

public class LoginServiceImpl implements LoginService {
    @Override
    public String loginIn(String un, String pw) {
        if (Strings.isNotEmpty(un) && Strings.isNotEmpty(pw)) {
            if ("lucien".equals(un) && "123123".equals(pw)) {
                return "success";
            }
        }
        return "fail";
    }
}
