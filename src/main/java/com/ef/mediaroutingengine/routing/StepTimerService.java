package com.ef.mediaroutingengine.routing;

import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.QueueEventName;
import com.ef.mediaroutingengine.routing.model.QueueTask;
import com.ef.mediaroutingengine.routing.model.Step;
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
     * @param queueTask      the task id
     * @param queue     the queue
     * @param stepIndex the step index
     */
    public void startNext(QueueTask queueTask, PrecisionQueue queue, int stepIndex) {
        if (this.timers.get(queueTask.getTaskId()) != null) {
            return;
        }

        try {
            queueTask.setCurrentStep(queue.getNextStep(stepIndex));

            if (queueTask.getCurrentStep() != null && !queueTask.getCurrentStep().isLastStep()) {
                long delay = queueTask.getCurrentStep().getStep().getTimeout() * 1000L;
                Timer timer = new Timer();
                timer.schedule(new StepTimerTask(queueTask, queue), delay);

                this.timers.put(queueTask.getTaskId(), timer);

                String stepId = queueTask.getCurrentStep().getStep().getId();
                logger.debug("Step: {} timer started for Queue Task: {}", stepId, queueTask);
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
        private final QueueTask queueTask;
        private final PrecisionQueue queue;

        /**
         * Instantiates a new Step timer task.
         *
         * @param queueTask  the task
         * @param queue the queue
         */
        public StepTimerTask(QueueTask queueTask, PrecisionQueue queue) {
            super();
            this.queueTask = queueTask;
            this.queue = queue;
        }

        public void run() {
            Step currentStep = queueTask.getCurrentStep().getStep();
            logger.debug("Time expired for step: {}, Queue Task: {}", currentStep.getId(), this.queueTask);

            StepTimerService.this.stop(this.queueTask.getTaskId());

            int currentStepIndex = this.queue.getStepIndex(currentStep);
            StepTimerService.this.startNext(queueTask, queue, currentStepIndex + 1);

            PropertyChangeEvent evt = new PropertyChangeEvent(this, QueueEventName.STEP_TIMEOUT, null, null);
            this.queue.getTaskScheduler().propertyChange(evt);
        }
    }
}
