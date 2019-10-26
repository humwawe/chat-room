package hum.impl;

import hum.core.IoProvider;
import hum.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hum
 */
public class IoSelectorProvider implements IoProvider {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AtomicBoolean inRegInput = new AtomicBoolean(false);
    private final AtomicBoolean inRegOutput = new AtomicBoolean(false);

    private final Selector readSelector;
    private final Selector writeSelector;

    private final ExecutorService inputHandlePool;
    private final ExecutorService outputHandlePool;

    private final Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();
    private final Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    public IoSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();

        inputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Output-Thread-"));

        // start
        startRead();
        startWrite();
    }

    private void startRead() {
        Thread thread = new Thread("IoSelectorProvider ReadSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() == 0) {
                            waitSelectionRegister(inRegInput);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_READ, inputCallbackMap, inputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        Thread thread = new Thread("IoSelectorProvider WriteSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitSelectionRegister(inRegOutput);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_WRITE, outputCallbackMap, outputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private static void waitSelectionRegister(final AtomicBoolean locker) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            if (locker.get()) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handleSelection(SelectionKey key, int keyOps, Map<SelectionKey, Runnable> map, ExecutorService pool) {
        // cancel current key
        key.interestOps(key.readyOps() & ~keyOps);

        Runnable runnable = map.get(key);
        if (runnable != null && !pool.isShutdown()) {
            pool.execute(runnable);
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
        return registerSelection(channel, readSelector, SelectionKey.OP_READ, inRegInput,
                inputCallbackMap, callback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        return registerSelection(channel, writeSelector, SelectionKey.OP_WRITE, inRegOutput,
                outputCallbackMap, callback) != null;
    }

    private static SelectionKey registerSelection(SocketChannel channel, Selector selector, int registerOps, AtomicBoolean locker,
                                                  Map<SelectionKey, Runnable> map, Runnable runnable) {
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            // in reg status
            locker.set(true);
            try {
                selector.wakeup();
                SelectionKey key = null;
                if (channel.isRegistered()) {
                    key = channel.keyFor(selector);
                    if (key != null) {
                        key.interestOps(key.readyOps() | registerOps);
                    }
                }

                if (key == null) {
                    key = channel.register(selector, registerOps);
                    map.put(key, runnable);
                }

                return key;
            } catch (ClosedChannelException e) {
                return null;
            } finally {
                // out reg status
                locker.set(false);
                try {
                    // notify wait
                    locker.notify();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel, readSelector, inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel, writeSelector, outputCallbackMap);
    }

    private static void unRegisterSelection(SocketChannel channel, Selector selector, Map<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            inputHandlePool.shutdown();
            outputHandlePool.shutdown();

            inputCallbackMap.clear();
            outputCallbackMap.clear();

            readSelector.wakeup();
            writeSelector.wakeup();

            CloseUtils.close(readSelector, writeSelector);
        }
    }


    /**
     * thread factory
     */
    static class IoProviderThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
