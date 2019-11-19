package com.lucien.learning.simplerpc.service;

public interface LoginService {

    /**
     * For login system.
     * @param un username.
     * @param pw password
     * @return login state.
     */
    String loginIn(String un, String pw);

}
