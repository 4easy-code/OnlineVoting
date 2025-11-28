package com.vote.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.vote.constant.ApiConstant;
import com.vote.dto.ClientConnection;
import com.vote.exceptions.PartyNotFoundException;
import com.vote.exceptions.TokenPassedToSSEIsExpired;
import com.vote.response.VotingResultResponse;
import com.vote.services.VotingResultServices;
import com.vote.util.JwtUtil;

import lombok.RequiredArgsConstructor;


@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.RESULT)
public class VotingResultController {
	private final VotingResultServices votingResultServices;
    private final JwtUtil jwtUtil;
    private final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();
    
    private final Logger logger = LoggerFactory.getLogger(VotingResultController.class);
    
    @GetMapping(value = ApiConstant.PUBLISH_RESULT, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter publishResult(@RequestParam("token") String token) throws TokenPassedToSSEIsExpired {
        String username = jwtUtil.extractUsername(token);
        if(username == null) throw new TokenPassedToSSEIsExpired("token expired");
        
        logger.info("Requesting SSE connection for user: {}", username);

        // Atomic replacement
        ClientConnection existingConn = connections.get(username);
        if (existingConn != null) {
            logger.info("Forcefully cleaning up previous connection for {}", username);
            existingConn.deactivate();
            connections.remove(username);
        }

        SseEmitter newEmitter = new SseEmitter(180_000L); // 3 minutes
        ClientConnection newConn = new ClientConnection(newEmitter);
        connections.put(username, newConn);

        setupEmitterCallbacks(newEmitter, username, newConn);

        try {
            VotingResultResponse response = votingResultServices.publishVotingResult();
            newEmitter.send(SseEmitter.event()
                .name("voteUpdate")
                .data(response)
                .reconnectTime(1000L));
        } catch (Exception e) {
            logger.error("Initial send failed: {}", e.getMessage());
            newConn.deactivate();
        }

        return newEmitter;
    }
    /*
    
    onCompletion: Triggered when the client gracefully closes the connection (e.g., EventSource.close()). 
    Removes the connection from the map.

    onTimeout: Called when the emitter's timeout (3 minutes) expires. Marks the connection as inactive and removes it.

    onError: Fired for any exception during SSE communication (e.g., client abruptly disconnects, network failure). 
    Deactivates and removes the connection.
    
    */
    
    private synchronized void setupEmitterCallbacks(SseEmitter emitter, String username, ClientConnection connection) {
        emitter.onCompletion(() -> {
            logger.info("SSE connection closed normally for {}", username);
            connections.remove(username, connection);
        });
        
        emitter.onTimeout(() -> {
            logger.info("SSE timeout for {}", username);
            connection.deactivate();
            connections.remove(username, connection);
        });
        
        emitter.onError(ex -> {
        	if (ex instanceof IOException) {
                logger.info("Client {} disconnected: {}", username, ex.toString());
            } else {
                logger.error("SSE error for {}: {}", username, ex.getMessage(), ex);
            }
            connection.deactivate();
            connections.remove(username, connection);
        });
    }
    
    
    @Async
    public void sendUpdatedResults() throws PartyNotFoundException {
        try {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "system", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
            );
            SecurityContextHolder.setContext(context);

            VotingResultResponse response = votingResultServices.publishVotingResult();
            System.out.println(response);


            connections.forEach((username, connection) -> {
            	synchronized (connection) {  // ← Add synchronization
                    try {
                        if (connection.isActive()) {
                            connection.getEmitter().send(
                                SseEmitter.event().name("voteUpdate").data(response)
                            );
                            connection.updateActivity();
                            connection.resetSendAttempts();
                        }
                    } catch (Exception e) {
                        connection.recordSendAttempt();
                        logger.error("Error sending to {}: {}", username, e.getMessage());
                        if (connection.getSendAttempts().intValue() >= 3) {
                            connection.deactivate();
                            connections.remove(username);
                        }
                    }
                }
            });
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Scheduled(fixedRate = 45_000)
    @Transactional
    protected void cleanupStaleConnections() {
    	logger.info("doing SSE cleanup");
        connections.entrySet().removeIf(entry -> {
            ClientConnection conn = entry.getValue();
            synchronized (conn) {
                boolean shouldRemove = !conn.isActive();
                if (shouldRemove) {
                    logger.info("Cleaning connection for {}", entry.getKey());
                    try {
                        conn.getEmitter().complete();
                    } catch (Exception e) {
                        logger.debug("Cleanup error: {}", e.getMessage());
                    }
                }
                return shouldRemove;
            }
        });
    }
    
    @Scheduled(fixedRate = 30_000)
    @Transactional
    public void sendHeartbeats() {
        logger.info("trying to send heartbeat");
        connections.forEach((username, conn) -> {
            synchronized (conn) { // Synchronize access to the connection
            	if (!conn.isActive()) { // Skip if already inactive
                    return;
                }
                try {
                    conn.getEmitter().send(SseEmitter.event().name("heartbeat").data("ping"));
                    conn.updateActivity();
                    conn.resetSendAttempts();
                } catch (Exception e) {
                    logger.info("Heartbeat failed for {}: {}", username, e.getMessage());
                    conn.recordSendAttempt();
                    conn.deactivate();
                    connections.remove(username, conn); // Redundant removal
                }
            }
        });
    }

}




/*
Yes, these callbacks handle most connection errors, but you'll still see occasional errors in logs because:

Race Conditions: Scheduled tasks might attempt to write to a connection that was just closed.

Asynchronous Nature: The SSE protocol doesn’t guarantee instant detection of client disconnects.












*/

