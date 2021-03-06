package cn.mzhong.janytask.queue;

import cn.mzhong.janytask.application.TaskContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部生产者实例创建者
 */
class InternalProducerFactory implements ProducerFactory {
    private Map<Class<?>, Object> objectMap = new HashMap<Class<?>, Object>();
    private TaskContext context;

    public void setContext(TaskContext context) {
        this.context = context;
    }

    public void registryProducer(Class<?> _class) {
        objectMap.put(_class, ProducerProxyFactory.newInstance(_class, context));
    }

    public Object getObject(Class<?> _class) {
        Object producer = objectMap.get(_class);
        if (producer == null) {
            for (Map.Entry<Class<?>, Object> entry : objectMap.entrySet()) {
                if (_class.isAssignableFrom(entry.getKey())) {
                    producer = entry.getValue();
                    objectMap.put(_class, producer);
                }
            }
            if (producer == null) {
                throw new RuntimeException("未找到生产者：" + _class.getName());
            }
        }
        return producer;
    }
}