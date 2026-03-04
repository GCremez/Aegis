package com.aegis.Aegis.ingress;

import com.aegis.Aegis.model.ProductState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RingBufferWrapper {
    private final ProductState[] buffer;
    private final int bufferSize;
    private final AtomicLong writeSequence = new AtomicLong(0);
    private final AtomicLong readSequence = new AtomicLong(0);
    private final long mask;
    
    public RingBufferWrapper(int bufferSize) {
        if ((bufferSize & (bufferSize - 1)) != 0) {
            throw new IllegalArgumentException("Buffer size must be a power of 2");
        }
        this.bufferSize = bufferSize;
        this.buffer = new ProductState[bufferSize];
        this.mask = bufferSize - 1;
    }
    
    public boolean publish(ProductState event) {
        long writeSeq = writeSequence.get();
        long readSeq = readSequence.get();
        
        if (writeSeq - readSeq >= bufferSize) {
            log.warn("Ring buffer full, dropping event for product: {}", event.getId());
            return false;
        }
        
        long index = writeSeq & mask;
        buffer[(int) index] = event;
        writeSequence.incrementAndGet();
        
        return true;
    }
    
    public ProductState consume() {
        long readSeq = readSequence.get();
        long writeSeq = writeSequence.get();
        
        if (readSeq >= writeSeq) {
            return null;
        }
        
        long index = readSeq & mask;
        ProductState event = buffer[(int) index];
        buffer[(int) index] = null; // Help GC
        readSequence.incrementAndGet();
        
        return event;
    }
    
    public int size() {
        return (int) (writeSequence.get() - readSequence.get());
    }
    
    public boolean isEmpty() {
        return writeSequence.get() == readSequence.get();
    }
    
    public boolean isFull() {
        return writeSequence.get() - readSequence.get() >= bufferSize;
    }
    
    public long getWriteSequence() {
        return writeSequence.get();
    }
    
    public long getReadSequence() {
        return readSequence.get();
    }
}
