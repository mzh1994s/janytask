package cn.mzhong.janytask.queue.provider.zookeeper;

import cn.mzhong.janytask.tool.IDGenerator;

public class ZookeeperPathGenerator extends IDGenerator {

    public ZookeeperPathGenerator(String root) {
        super(root, "/");
    }
}
