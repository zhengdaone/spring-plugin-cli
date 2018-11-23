package com.pinyi.plugin.devastate;

import com.pinyi.mian.example.HelloWorld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {

    @Autowired
    private HelloWorld helloWorld;

    @Scheduled(fixedRate = 6)
    protected void printHello() {
        helloWorld.printHello();
    }
}
