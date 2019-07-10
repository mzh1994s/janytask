package cn.mzhong.janytask.consumer;

import cn.mzhong.janytask.queue.Loopline;
import cn.mzhong.janytask.queue.Pipleline;
import cn.mzhong.janytask.initializer.TaskComponentInitializer;
import cn.mzhong.janytask.queue.LooplineInfo;
import cn.mzhong.janytask.queue.PiplelineInfo;
import cn.mzhong.janytask.core.TaskContext;
import cn.mzhong.janytask.executor.TaskExecutorService;
import cn.mzhong.janytask.executor.TaskLooplineExecutor;
import cn.mzhong.janytask.executor.TaskPiplelineExecutor;
import cn.mzhong.janytask.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;


public class TaskConsumerInitializer implements TaskComponentInitializer {

    final static Logger Log = LoggerFactory.getLogger(TaskConsumerInitializer.class);

    /**
     * 在JAnyMQ中，消费者是提供者的实现，所以扫描消费者Class的接口，目的是找到提供者中的Pipleline、Loopline等注解
     *
     * @param consumerClass
     * @param method
     * @return
     */
    private static <A extends Annotation> MethodInfo<A> findLineInfo(Class<?> consumerClass, Method method, Class<A> annotationType) {
        Set<Class<?>> interfaces = ClassUtils.getInterfaces(consumerClass);
        MethodInfo lineInfo = null;
        for (Class<?> _interface : interfaces) {
            try {
                Method pMethod = _interface.getMethod(method.getName(), method.getParameterTypes());
                if (pMethod != null) {
                    A annotation = pMethod.getAnnotation(annotationType);
                    if (annotation != null) {
                        lineInfo = new MethodInfo<A>(_interface, pMethod, annotation);
                        break;
                    }
                }
            } catch (NoSuchMethodException e) {
                continue;
            }
        }
        return lineInfo;
    }

    private static PiplelineInfo findPipleline(Class<?> consumerClass, Method method) {
        MethodInfo<Pipleline> lineInfo =
                findLineInfo(consumerClass, method, Pipleline.class);
        if (lineInfo != null) {
            Pipleline piplelineAnnotation = lineInfo.annotation;
            return new PiplelineInfo(lineInfo._interface, lineInfo.method, piplelineAnnotation);
        }
        return null;
    }

    private static LooplineInfo findLoopline(Class<?> consumerClass, Method method) {
        MethodInfo<Loopline> lineInfo =
                findLineInfo(consumerClass, method, Loopline.class);
        if (lineInfo != null) {
            Loopline looplineAnnotation = lineInfo.annotation;
            return new LooplineInfo(lineInfo._interface, lineInfo.method, looplineAnnotation);
        }
        return null;
    }

    protected void initExecutor(TaskContext context, Collection<Object> consumerSet) {
        ExecutorService consumerExecutor = new TaskExecutorService("JAnyMQ-Executor");
        for (Object consumer : consumerSet) {
            Class<?> consumerClass = consumer.getClass();
            for (Method method : consumerClass.getMethods()) {
                PiplelineInfo pipleline = findPipleline(consumerClass, method);
                if (pipleline != null) {
                    consumerExecutor.execute(new TaskPiplelineExecutor(context, consumer, method, pipleline));
                    continue;
                }
                LooplineInfo loopline = findLoopline(consumerClass, method);
                if (loopline != null) {
                    consumerExecutor.execute(new TaskLooplineExecutor(context, consumer, method, loopline));
                }
            }
        }
        context.setConsumerExecutorService(consumerExecutor);
    }

    public void init(TaskContext context) {
        Map<Class<?>, Object> consumerMap = new HashMap<Class<?>, Object>();
        try {
            for (Class<?> consumerClass : context.getConsumerClassSet()) {
                Object consumer = consumerClass.getDeclaredConstructor().newInstance();
                consumerMap.put(consumerClass, consumer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.setConsumerMap(consumerMap);
        initExecutor(context, consumerMap.values());
    }
}

class MethodInfo<A extends Annotation> {
    Class<?> _interface;
    Method method;
    A annotation;

    public MethodInfo(Class<?> _class, Method method, A annotation) {
        this._interface = _class;
        this.method = method;
        this.annotation = annotation;
    }
}