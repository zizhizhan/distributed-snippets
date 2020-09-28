package com.zizhizhan.mvcc;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟mysql中的ReadView
 *
 * @author zizhizhan
 * @date 2020-07-25 20:31
 */
public class ReadView {

    /**
     * 记录当前活跃的事务ID
     */
    private List<Integer> mIds = new ArrayList<>();

    /**
     * 记录当前活跃的最小事务ID
     */
    private Integer minTrxId;

    /**
     * 记录当前最大事务ID，注意并不是活跃的最大ID，包括已提交的，因为有可能最大的事务ID已经提交了
     */
    private Integer maxTrxId;

    /**
     * 记录当前生成readView时的事务ID
     */
    private Integer currTrxId;

    public ReadView(Integer currTrxId) {
        this.currTrxId = currTrxId;
    }

    public Data read(Data data) {
        // 先判断当前最新数据是否可以访问
        if (canRead(data.getDataTrxId())) {
            return data;
        }

        // 获取到该数据的undo log引用
        UndoLog undoLog = data.getNextUndoLog();
        do {
            // 如果undoLog存在并且可读，则合并返回
            if (undoLog != null && canRead(undoLog.getTrxId())) {
                return merge(data, undoLog);
            }

            // 还没找到可读版本，继续获取下一个更旧版本
            undoLog = undoLog.getNext();
        } while (undoLog != null && undoLog.getNext() != null);

        // 整个undo log链都找不到可读的，没办法了我也帮不鸟你
        return null;
    }

    /**
     * 合并最新数据和目标版本的undo log数据，返回最终可访问数据
     */
    private Data merge(Data data, UndoLog undoLog) {
        if (undoLog == null) {
            return data;
        }

        // update 更新 直接把undo保存的数据替换过来返回
        // add 新增 直接把undo保存的数据替换过来返回
        // del 删除 数据当时是不存在的，直接返回null就好了
        if ("update".equalsIgnoreCase(undoLog.getOperationType())) {
            data.setValue(undoLog.getValue());
            return data;
        } else if ("add".equalsIgnoreCase(undoLog.getOperationType())) {
            data.setId(undoLog.getRecordId());
            data.setValue(undoLog.getValue());
            return data;
        } else if ("del".equalsIgnoreCase(undoLog.getOperationType())) {
            return null;
        } else {
            //其余情况，不管了，直接返回算了
            return data;
        }
    }

    private boolean canRead(Integer dataTrxId) {
        // 1.如果当前数据的所属事务正好是当前事务或者数据的事务小于mIds的最小事务ID，
        // 则说明产生该数据的事务在生成ReadView之前已经提交了，该数据可以访问
        if (dataTrxId == null || dataTrxId.equals(currTrxId) || dataTrxId < minTrxId) {
            return true;
        }

        // 2.如果当前数据所属事务大于当前最大事务ID（并不是mIds的最大事务，好多人都觉得是），
        // 则说明产生该数据是在生成ReadView之后，则当前事务不可访问
        if (dataTrxId > maxTrxId) {
            return false;
        }

        // 3.如果当前数据所属事务介于mIds最小事务和当前最大事务ID之间，则需要进一步判断
        if (dataTrxId >= minTrxId && dataTrxId <= maxTrxId) {

            // 如果当前数据所属事务包含在mIds当前活跃事务列表中，则说明该事务还没提交，不可访问,反之表示数据所属事务已经提交了，可以访问
            return !mIds.contains(dataTrxId);
        }
        return false;
    }

    public List<Integer> getmIds() {
        return mIds;
    }

    public void setmIds(List<Integer> mIds) {
        this.mIds = mIds;
    }

    public Integer getMinTrxId() {
        return minTrxId;
    }

    public void setMinTrxId(Integer minTrxId) {
        this.minTrxId = minTrxId;
    }

    public Integer getMaxTrxId() {
        return maxTrxId;
    }

    public void setMaxTrxId(Integer maxTrxId) {
        this.maxTrxId = maxTrxId;
    }

    public Integer getCurrTrxId() {
        return currTrxId;
    }

    public void setCurrTrxId(Integer currTrxId) {
        this.currTrxId = currTrxId;
    }

    @Override
    public String toString() {
        return "ReadView{" +
                "mIds=" + mIds +
                ", minTrxId=" + minTrxId +
                ", maxTrxId=" + maxTrxId +
                ", currTrxId=" + currTrxId +
                '}';
    }
}
