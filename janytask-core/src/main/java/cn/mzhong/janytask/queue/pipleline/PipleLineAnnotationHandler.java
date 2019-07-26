package cn.mzhong.janytask.queue.pipleline;

import cn.mzhong.janytask.queue.*;
import cn.mzhong.janytask.core.TaskContext;
import cn.mzhong.janytask.util.ValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class PipleLineAnnotationHandler implements QueueAnnotationHandler<Pipleline> {


    public Class<Pipleline> getAnnotationClass() {
        return Pipleline.class;
    }

    public void handleProducer(TaskContext context, QueueManager queueManager, QueueInfo<Pipleline> queueInfo) {
        Method method = queueInfo.getProducerMethod();
        if (method.getReturnType() != Void.TYPE) {
            throw new RuntimeException("流水线" + queueInfo.ID() + "对应的方法" + method.getName() + "返回值应为void");
        }
    }

    public QueueExecutor<Pipleline> handleConsumer(TaskContext context, QueueManager queueManager, QueueInfo<Pipleline> queueInfo) {
        return new PiplelineTaskExecutor(context, queueManager, queueInfo);
    }
}

class PiplelineTaskExecutor extends QueueExecutor<Pipleline> {

    Pipleline pipleline;

    Logger Log = LoggerFactory.getLogger(PipleLineAnnotationHandler.class);

    public PiplelineTaskExecutor(TaskContext context, QueueManager queueManager, QueueInfo<Pipleline> queueInfo) {
        super(context, queueManager, queueInfo);
        this.pipleline = queueInfo.getAnnotation();
    }

    protected void invoke(Message message) {
        Object consumer = queueInfo.getConsumer();
        Method method = queueInfo.getConsumerMethod();
        MessageDao messageDao = queueInfo.getMessageDao();
        try {
            method.invoke(consumer, message.getContent());
            messageDao.done(message);
        } catch (Exception e) {
            Log.error(e.getLocalizedMessage(), e);
            message.setThrowable(e);
            messageDao.error(message);
        }
    }
}