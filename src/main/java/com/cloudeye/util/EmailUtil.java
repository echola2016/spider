package com.cloudeye.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Created by lafangyuan on 2018/10/9.
 */
@Component
public class EmailUtil {

    @Autowired
    JavaMailSender jms;

    private static final String from = "layuan@126.com";
    public synchronized void send(String to,String subject,String text){
        SimpleMailMessage mainMessage = new SimpleMailMessage();
        mainMessage.setFrom(from);
        mainMessage.setTo(to);
        mainMessage.setSubject(subject);
        mainMessage.setText(text);
        jms.send(mainMessage);
    }
}
