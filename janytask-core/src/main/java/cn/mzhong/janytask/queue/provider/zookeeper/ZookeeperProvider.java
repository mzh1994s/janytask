package cn.mzhong.janytask.queue.provider.zookeeper;

import cn.mzhong.janytask.application.TaskContext;
import cn.mzhong.janytask.queue.MessageDao;
import cn.mzhong.janytask.queue.QueueInfo;
import cn.mzhong.janytask.queue.provider.AbstractProvider;

public class ZookeeperProvider extends AbstractProvider {

    protected String connectString;
    protected String root = "janytask";
    protected TaskContext context;

    public void setContext(TaskContext context) {
        this.context = context;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public MessageDao createMessageDao(QueueInfo queueInfo) {
        return new ZookeeperMessageDao(context, queueInfo, connectString, root);
    }

    public void init() {
    }
}
