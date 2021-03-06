package cn.mzhong.janytask.queue.provider.jdbc;

import cn.mzhong.janytask.queue.Message;

/**
 * 各数据库软件之间与jdbc对接存在序列化兼容性问题，这里所以有序列化操作使用手工方式。
 * 在从数据库获取throwable、content两个字段时，首先使用{@link java.sql.ResultSet#getBytes(int)}方法得到byte数组。
 * 再分别赋值给throwableBytes、contentBytes两个字段并返回，框架会自动将byte数组反序列化，并赋值给throwable、content字段
 */
public class BytesMessage extends Message {
    private static final long serialVersionUID = -744128535422190869L;

    public BytesMessage() {

    }

    public BytesMessage(Message message) {
        this.id = message.getId();
        this.queueId = message.getQueueId();
        this.pushTime = message.getPushTime();
        this.doneTime = message.getDoneTime();
        this.errorTime = message.getErrorTime();
        this.throwable = message.getThrowable();
        this.args = message.getArgs();
        this.result = message.getResult();
    }

    protected byte[] throwableBytes;
    protected byte[] argsBytes;
    protected byte[] resultBytes;

    public byte[] getThrowableBytes() {
        return throwableBytes;
    }

    public void setThrowableBytes(byte[] throwableBytes) {
        this.throwableBytes = throwableBytes;
    }

    public byte[] getArgsBytes() {
        return argsBytes;
    }

    public void setArgsBytes(byte[] argsBytes) {
        this.argsBytes = argsBytes;
    }

    public byte[] getResultBytes() {
        return resultBytes;
    }

    public void setResultBytes(byte[] resultBytes) {
        this.resultBytes = resultBytes;
    }
}
