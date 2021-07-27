package com.ef.mediaroutingengine;

import com.ef.mediaroutingengine.bootstrap.Bootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * The type Media routing engine application.
 */
@SpringBootApplication
public class MediaRoutingEngineApplication {
    /**
     * application's starting point.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) {
        //Todo: when the precision-queue is deleted from the config -> delete that queue associated with a
        //Todo: task in the redis DB.

        ApplicationContext applicationContext = SpringApplication.run(MediaRoutingEngineApplication.class, args);

        Bootstrap bootstrap = applicationContext.getBean(Bootstrap.class);
        bootstrap.subscribeToStateEventsChannel();
        bootstrap.loadPools();
    }
}
