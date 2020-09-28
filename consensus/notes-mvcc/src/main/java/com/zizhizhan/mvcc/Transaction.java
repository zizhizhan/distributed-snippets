package com.zizhizhan.mvcc;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事务类，只是为了方便看懂原理和避免偏离主题，所以这里省略了本应该需要用到的锁
 *
 * @author zizhizhan
 * @date 2020-07-25 20:17
 */
@Slf4j
public class Transaction {

    /**
     * 全局事务id
     */
    private static AtomicInteger globalTrxId = new AtomicInteger();

    /**
     * 当前活跃的事务
     */
    private static Map<Integer, Transaction> currRunningTrxMap = new ConcurrentHashMap<>();

    /**
     * 当前事务id
     */
    private Integer currTrxId = 0;

    /**
     * 事务隔离级别，ru,rc,rr和s
     */
    private String trxMode = "rr";

    /**
     * 只有rc和rr级别非锁定读才需要用到mvcc，这个readView是为了方便判断到底哪个版本的数据可以被
     * 当前事务获取到的视图工具
     */
    private ReadView readView;

    /**
     * 开启事务
     */
    public void begin() {
        // 根据全局事务计数器拿到当前事务ID
        currTrxId = globalTrxId.incrementAndGet();

        // 将当前事务放入当前活跃事务映射中
        currRunningTrxMap.put(currTrxId, this);

        // 构造或者更新当前事务使用的mvcc辅助判断视图ReadView
        updateReadView();
        log.info("Transaction start {} on {}.", currTrxId, Thread.currentThread().getName());
    }

    /**
     * 提交事务
     */
    public void commit() {
        log.info("Transaction commit {} on {}.", currTrxId, Thread.currentThread().getName());
        currRunningTrxMap.remove(currTrxId);
    }

    /**
     * 构造或者更新当前事务使用的mvcc辅助判断视图ReadView
     */
    public void updateReadView() {
        // 构造辅助视图工具ReadView
        readView = new ReadView(currTrxId);

        // 设置当前事务最大值
        readView.setMaxTrxId(globalTrxId.get());
        List<Integer> mIds = new ArrayList<>(currRunningTrxMap.keySet());
        Collections.sort(mIds);

        // 设置当前活跃事务id
        readView.setmIds(new ArrayList<>(currRunningTrxMap.keySet()));

        // 设置mIds中最小事务ID
        readView.setMinTrxId(mIds.isEmpty() ? 0 : mIds.get(0));

        // 设置当前事务ID
        readView.setCurrTrxId(currTrxId);
    }

    public static AtomicInteger getGlobalTrxId() {
        return globalTrxId;
    }

    public static void setGlobalTrxId(AtomicInteger globalTrxId) {
        Transaction.globalTrxId = globalTrxId;
    }

    public static Map<Integer, Transaction> getCurrRunningTrxMap() {
        return currRunningTrxMap;
    }

    public static void setCurrRunningTrxMap(Map<Integer, Transaction> currRunningTrxMap) {
        Transaction.currRunningTrxMap = currRunningTrxMap;
    }

    public Integer getCurrTrxId() {
        return currTrxId;
    }

    public void setCurrTrxId(Integer currTrxId) {
        this.currTrxId = currTrxId;
    }

    public String getTrxMode() {
        return trxMode;
    }

    public void setTrxMode(String trxMode) {
        this.trxMode = trxMode;
    }

    public ReadView getReadView() {
        return readView;
    }
}
