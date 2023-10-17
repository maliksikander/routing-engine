package com.ef.mediaroutingengine.routing;

import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueEventName;
import com.ef.mediaroutingengine.routing.model.QueueTask;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The type Step timer service.
 */
@Service
public class StepTimerService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(StepTimerService.class);

    /**
     * The Timers.
     */
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    /**
     * Start next.
     *
     * @param task      the task id
     * @param queue     the queue
     * @param stepIndex the step index
     */
    public void startNext(QueueTask task, PrecisionQueue queue, int stepIndex) {
        if (this.timers.get(task.getMediaId()) != null) {
            return;
        }

        try {
            task.setCurrentStep(queue.getNextStep(stepIndex));

            if (task.getCurrentStep() != null && !task.getCurrentStep().isLastStep()) {
                long delay = task.getCurrentStep().getStep().getTimeout() * 1000L;
                Timer timer = new Timer();

                this.timers.put(task.getMediaId(), timer);
                timer.schedule(new StepTimerTask(task, queue), delay);

                logger.debug("Step: {} timer started for task: {}", task.getCurrentStep().getStep().getId(), task);
            }
        } catch (IllegalArgumentException ex) {
            if (!"Negative delay.".equalsIgnoreCase(ExceptionUtils.getRootCause(ex).getMessage())) {
                logger.error(ExceptionUtils.getMessage(ex));
                logger.error(ExceptionUtils.getStackTrace(ex));
            }
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getMessage(ex));
            logger.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Stop.
     *
     * @param taskId the task id
     */
    public void stop(String taskId) {
        Timer timer = this.timers.get(taskId);
        if (timer != null) {
            timer.cancel();
            this.timers.remove(taskId);
        }
    }

    /**
     * The type Step timer task.
     */
    private class StepTimerTask extends TimerTask {
        private final QueueTask task;
        private final PrecisionQueue queue;

        /**
         * Instantiates a new Step timer task.
         *
         * @param task  the task
         * @param queue the queue
         */
        public StepTimerTask(QueueTask task, PrecisionQueue queue) {
            super();
            this.task = task;
            this.queue = queue;
        }

        public void run() {
            logger.debug("Time expired for step: {}, Task: {}", task.getCurrentStep().getStep().getId(), this.task);
            StepTimerService.this.stop(this.task.getMediaId());

            int currentStepIndex = this.queue.getStepIndex(task.getCurrentStep().getStep());
            StepTimerService.this.startNext(task, queue, currentStepIndex + 1);

            PropertyChangeEvent evt = new PropertyChangeEvent(this, QueueEventName.STEP_TIMEOUT, null, null);
            this.queue.getTaskScheduler().propertyChange(evt);
        }
    }
}
