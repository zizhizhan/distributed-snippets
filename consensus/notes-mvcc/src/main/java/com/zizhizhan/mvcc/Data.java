package com.zizhizhan.mvcc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模仿数据库的数据存储地方
 *
 * @author zizhizhan
 * @date 2020-07-25 19:24
 */
public class Data {
    /**
     * 模拟一个存放数据的表
     */
    private static Map<Integer, Data> dataMap = new ConcurrentHashMap<>();

    /**
     * 记录的ID
     */
    private Integer id;

    /**
     * 记录的数据
     */
    private String value;

    /**
     * 记录当前记录的事务ID
     */
    private Integer dataTrxId;

    /**
     * 指向上个版本的undo log的引用
     */
    private UndoLog nextUndoLog;

    /**
     * 标记数据是否删除，实际数据库会根据情况由purge线程完成真实数据的清楚
     */
    private boolean isDelete;

    public Data(Integer dataTrxId) {
        this.dataTrxId = dataTrxId;
    }

    /**
     * 模拟数据库更新操作,这里就不要自增id，直接指定id了
     *
     * @param id
     * @param value
     * @return
     */
    public Integer update(Integer id, String value) {

        // 获取原值,这里就不判断是否存在，于本例核心偏离的逻辑了
        Data oldData = dataMap.get(id);

        // 更新当前数据
        this.id = id;
        this.value = value;

        // 不要忘了，为了数据的一致性，要准备好随时失败回滚的undo log，这里既然是修改数据，那代表以前这条数据是就记录一下以前的旧值，
        // 将旧值构造成一个undo log记录
        UndoLog undoLog = new UndoLog(id, oldData.getValue(), oldData.getDataTrxId(), "update");

        // 将旧值的undo log挂到当前新的undo log之后，形成一个按从新到旧顺序的一个undo log链表
        undoLog.setNext(oldData.getNextUndoLog());

        // 将当前数据的undo log引用指向新生成的undo log
        this.nextUndoLog = undoLog;

        //  更新数据表当前id的数据
        dataMap.put(id, this);
        return id;
    }

    /**
     * 按照上面更新操作的理解,删除相当于是把原纪录修改成标记成已删除状态的记录了
     *
     * @param id
     */
    public void delete(Integer id) {
        // 获取原值,这里就不判断是否存在，于本例核心偏离的逻辑了
        Data oldData = dataMap.get(id);
        this.id = id;

        // 将当前数据标记成已删除
        this.setDelete(true);

        // 同样，为了数据的一致性，要准备好随时失败回滚的undo log，这里既然是删除数据，那代表以前这条数据存在，
        // 就记录一下以前的旧值,并将旧值构造成一个逻辑上新增的undo log记录
        UndoLog undoLog = new UndoLog(id, oldData.getValue(), oldData.getDataTrxId(), "add");

        // 将旧值的undo log挂到当前新的undo log之后，形成一个按从新到旧顺序的一个undo log链表
        undoLog.setNext(oldData.getNextUndoLog());

        // 将当前数据的undo log引用指向新生成的undo log
        this.nextUndoLog = undoLog;

        //  更新数据表当前id的数据
        dataMap.put(id, this);
    }

    /**
     * 按照上面更新操作的理解,新增相当于是把原纪录原来不存在的记录修改成了新的记录
     *
     * @param id
     */
    public void insert(Integer id, String value) {
        // 更新当前数据
        this.id = id;
        this.value = value;

        // 同样，为了数据的一致性，要准备好随时失败回滚的undo log，这里既然是新增数据，那代表以前这条数据不存在，
        // 就记录一下以前为空值,并将空值构造成一个逻辑上删除的undo log记录
        UndoLog undoLog = new UndoLog(id, null, null, "delete");

        // 将当前数据的undo log引用指向新生成的undo log
        this.nextUndoLog = undoLog;

        //  更新数据表当前id的数据
        dataMap.put(id, this);
    }

    /**
     * 模拟使用mvcc非锁定读，这里的mode表示事务隔离级别，只有rc和rr级别才需要用到mvcc，同样为了方便，
     * 使用英文表示隔离级别，rc表示读已提交，rr表示可重复读
     */
    public Data select(Integer id) {
        // 拿到当前事务，然后判断事务隔离级别，如果是rc，则执行一个语句就要更新一下ReadView,这里写的这么直接就是为了好理解
        Transaction currTrx = Transaction.getCurrRunningTrxMap().get(this.getDataTrxId());
        String trxMode = currTrx.getTrxMode();
        if ("rc".equalsIgnoreCase(trxMode)) {
            currTrx.updateReadView();
        }

        // 拿到当前事务辅助视图ReadView
        ReadView readView = currTrx.getReadView();

        // 模拟根据id取出一行数据
        Data data = Data.getDataMap().get(id);

        // 使用readView判断并读取当前事务可以读取到的最终数据
        return readView.read(data);
    }


    public static Map<Integer, Data> getDataMap() {
        return dataMap;
    }

    public static void setDataMap(Map<Integer, Data> dataMap) {
        Data.dataMap = dataMap;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDataTrxId() {
        return dataTrxId;
    }

    public void setDataTrxId(Integer dataTrxId) {
        this.dataTrxId = dataTrxId;
    }

    public UndoLog getNextUndoLog() {
        return nextUndoLog;
    }

    public void setNextUndoLog(UndoLog nextUndoLog) {
        this.nextUndoLog = nextUndoLog;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
