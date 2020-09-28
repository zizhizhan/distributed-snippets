package com.zizhizhan.mvcc;

/**
 * 模仿undo log链
 *
 * @author zizhizhan
 * @date 2020-07-25 19:52
 */
public class UndoLog {
    /**
     * 指向上一个undo log
     */
    private UndoLog pre;

    /**
     * 指向下一个undo log
     */
    private UndoLog next;

    /**
     * 记录数据的ID
     */
    private Integer recordId;

    /**
     * 记录的数据
     */
    private String value;
    /**
     * 记录当前数据所属的事务ID
     */
    private Integer trxId;

    /**
     * 操作类型，感觉用整型好一点，但是如果用整型，又要搞个枚举，麻烦，所以直接用字符串了，能表达意思就好
     * update 更新
     * add 新增
     * del 删除
     */
    private String operationType;

    public UndoLog(Integer recordId, String value, Integer trxId, String operationType) {
        this.recordId = recordId;
        this.value = value;
        this.trxId = trxId;
        this.operationType = operationType;
    }

    public UndoLog getPre() {
        return pre;
    }

    public void setPre(UndoLog pre) {
        this.pre = pre;
    }

    public UndoLog getNext() {
        return next;
    }

    public void setNext(UndoLog next) {
        this.next = next;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getTrxId() {
        return trxId;
    }

    public void setTrxId(Integer trxId) {
        this.trxId = trxId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
