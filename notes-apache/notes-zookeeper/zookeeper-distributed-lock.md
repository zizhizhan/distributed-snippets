# Zookeeper 分布式锁

实现代码参考 [DistributedLock](src/main/java/me/jameszhan/notes/zk/locks/v1/DistributedLock.java)

如果需要在多线程环境中使用，则需要在每个线程中创建新实例。

```java
DistributedLock lock = new DistributedLock(connectString, "test-locks");
lock.lock();
try {
    // safe code
} finally {
    lock.unlock();
}
```

