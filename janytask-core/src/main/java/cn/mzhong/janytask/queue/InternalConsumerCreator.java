package cn.mzhong.janytask.queue;

public class InternalConsumerCreator implements InstanceCreator<Object> {

    public Object create(Class<Object> consumerClass) {
        try {
            return consumerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
