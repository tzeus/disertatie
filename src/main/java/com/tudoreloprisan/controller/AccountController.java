package com.tudoreloprisan.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @CrossOrigin(origins = "*") //TODO CHANGEME
    public String getAccountDetails(){
        return "THIS WILL BE CHANGED";
    }
}
