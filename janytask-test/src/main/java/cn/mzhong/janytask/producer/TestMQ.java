package cn.mzhong.janytask.producer;

import cn.mzhong.janytask.queue.Loopline;
import cn.mzhong.janytask.queue.Pipleline;

@Producer
public interface TestMQ {

    @Pipleline
    void testPipleline(String value);

    @Loopline
    boolean testLoopline(String value);
}
