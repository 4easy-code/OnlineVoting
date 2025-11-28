package com.vote.dto;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientConnection {
    private SseEmitter emitter;
    private volatile boolean isActive = true;
    private volatile Instant lastActivity;
    private AtomicInteger sendAttempts = new AtomicInteger(0);
    
    public ClientConnection(SseEmitter emitter) {
        this.emitter = emitter;
        this.isActive = true;
        this.lastActivity = Instant.now();
        this.sendAttempts = new AtomicInteger(0);
    }
       
    public void deactivate() {
        synchronized (this) {
            this.isActive = false;
        }
    }
    
    public void updateActivity() {
        this.lastActivity = Instant.now();
    }
    
    public boolean isActive() {
    	synchronized (this) {
            return this.isActive && 
                   lastActivity.isAfter(Instant.now().minusSeconds(40)) &&
                   sendAttempts.get() < 3;
        }
    }

    public void recordSendAttempt() {
        sendAttempts.incrementAndGet();
    }

    public void resetSendAttempts() {
        sendAttempts.set(0);
    }
}