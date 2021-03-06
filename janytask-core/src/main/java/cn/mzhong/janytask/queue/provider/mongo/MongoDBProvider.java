package cn.mzhong.janytask.queue.provider.mongo;

import cn.mzhong.janytask.application.TaskContext;
import cn.mzhong.janytask.queue.MessageDao;
import cn.mzhong.janytask.queue.QueueInfo;
import cn.mzhong.janytask.queue.provider.AbstractProvider;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

public class MongoDBProvider extends AbstractProvider {

    protected String host;
    protected int port;
    protected String username;
    protected String database;
    protected String password;
    protected MongoDatabase mongoDatabase;
    protected TaskContext context;

    public void setContext(TaskContext context) {
        this.context = context;
    }

    public MessageDao createMessageDao(QueueInfo queueInfo) {
        return new MongoDbMessageDao(context, queueInfo, mongoDatabase.getCollection(queueInfo.getId()));
    }

    public void init() {
        ServerAddress serverAddress = new ServerAddress(host, port);
        MongoCredential credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
        List<ServerAddress> addressList = new ArrayList<ServerAddress>();
        addressList.add(serverAddress);
        List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
        credentialList.add(credential);
        MongoClient mongoClient = new MongoClient(addressList, credentialList);
        this.mongoDatabase = mongoClient.getDatabase(database);
    }
}
