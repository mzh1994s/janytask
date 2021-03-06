package cn.mzhong.janytask.queue.provider.zookeeper;

import cn.mzhong.janytask.application.TaskContext;
import cn.mzhong.janytask.queue.LockedMessageDao;
import cn.mzhong.janytask.queue.Message;
import cn.mzhong.janytask.queue.QueueInfo;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class ZookeeperMessageDao extends LockedMessageDao {
    final static Logger Log = LoggerFactory.getLogger(ZookeeperMessageDao.class);
    protected String connectString;
    protected ZookeeperClient zkClient;
    protected String waitPath;
    protected String donePath;
    protected String errorPath;
    protected String lockPath;
    protected String rootPath;

    public void initZookeeperClient(String connectString) {
        this.connectString = connectString;
        this.zkClient = new ZookeeperClient(connectString);
    }

    public void initParentPath() {
        String parentPath = rootPath + "/" + queueInfo.getId();
        this.waitPath = parentPath + "/wait";
        this.donePath = parentPath + "/done";
        this.errorPath = parentPath + "/error";
        this.lockPath = parentPath + "/lock";
        // 创建父级目录
        zkClient.create(waitPath, null, CreateMode.PERSISTENT);
        zkClient.create(donePath, null, CreateMode.PERSISTENT);
        zkClient.create(errorPath, null, CreateMode.PERSISTENT);
        zkClient.create(lockPath, null, CreateMode.PERSISTENT);
    }

    public void initRootPath(String rootPath) {
        this.rootPath = rootPath.startsWith("/") ? rootPath : "/" + rootPath;
    }

    ZookeeperMessageDao(TaskContext context, QueueInfo lineInfo, String connectString, String rootPath) {
        super(context, lineInfo);
        this.initZookeeperClient(connectString);
        this.initRootPath(rootPath);
        this.initParentPath();
    }

    protected void push(String parent, Message message) {
        try {
            byte[] data = serializer.serialize(message);
            String path = parent + "/" + message.getId();
            zkClient.create(path, data, CreateMode.PERSISTENT);
        } catch (Exception e) {
            throw new RuntimeException("推送消息出错！", e);
        }
    }

    protected void delete(String parent, Message message) {
        zkClient.delete(parent + "/" + message.getId());
    }

    public void push(Message message) {
        push(waitPath, message);
    }

    public void done(Message message) {
        push(donePath, message);
        delete(waitPath, message);
        unLock(message.getId());
    }

    public void error(Message message) {
        push(errorPath, message);
        delete(waitPath, message);
        unLock(message.getId());
    }

    public long length() {
        return zkClient.getChildren(waitPath).size();
    }

    protected boolean lock(String id) {
        return zkClient.create(lockPath + "/" + id, null, CreateMode.EPHEMERAL);
    }

    protected boolean unLock(String id) {
        zkClient.delete(lockPath + "/" + id);
        return true;
    }

    public Message get(String id) {
        byte[] data = zkClient.getData(waitPath + "/" + id);
        try {
            return (Message) serializer.deserialize(data);
        } catch (Exception e) {
            Log.error("消息反序列化出错，消息已被忽略！消息ID:" + id, e);
        }
        return null;
    }

    protected LinkedList<String> keys() {
        return new LinkedList<String>(zkClient.getChildren(waitPath));
    }
}
