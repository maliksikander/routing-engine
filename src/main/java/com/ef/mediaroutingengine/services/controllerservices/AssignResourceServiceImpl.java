package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceServiceImpl implements AssignResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignResourceServiceImpl.class);
    private final TasksPool tasksPool;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param tasksPool pool of all tasks.
     */
    @Autowired
    public AssignResourceServiceImpl(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    @Override
    public void assign(AssignResourceRequest request) {
        LOGGER.debug("assign method started");
        this.tasksPool.enqueueTask(request);
        LOGGER.debug("Task enqueued");
        LOGGER.debug("assign method ended");
    }
}
