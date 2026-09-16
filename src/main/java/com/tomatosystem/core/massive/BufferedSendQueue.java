package com.tomatosystem.core.massive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.cleopatra.protocol.data.DataResponse;

public class BufferedSendQueue {
    private final List<LinkedHashMap<String, Object>> buffer = new ArrayList<>();
    private final int flushThreshold;
    private final long flushIntervalMillis;
    private final Object lock = new Object();
    private long lastFlushTime;
    private final DataResponse dataResponse;

    public BufferedSendQueue(DataResponse dataResponse, int flushThreshold, long flushIntervalMillis) {
        this.dataResponse = dataResponse;
        this.flushThreshold = flushThreshold;
        this.flushIntervalMillis = flushIntervalMillis;
        this.lastFlushTime = System.currentTimeMillis();
    }

    public void add(LinkedHashMap<String, Object> rowData) {
        synchronized (lock) {
            buffer.add(rowData);
            long now = System.currentTimeMillis();

            if (buffer.size() >= flushThreshold || now - lastFlushTime >= flushIntervalMillis) {
                flushInternal();
                lastFlushTime = now;
            }
        }
    }

    public void flushRemaining() {
        synchronized (lock) {
            flushInternal();
        }
    }

    private void flushInternal() {
        if (buffer.isEmpty()) return;

        try {
            for (LinkedHashMap<String, Object> row : buffer) {
                dataResponse.send(row);  // 버퍼 내 개별 row 전송
            }
            dataResponse.flush();
        } catch (IOException e) {
            throw new RuntimeException("전송 중 오류", e);
        } finally {
            buffer.clear();
        }
    }
}