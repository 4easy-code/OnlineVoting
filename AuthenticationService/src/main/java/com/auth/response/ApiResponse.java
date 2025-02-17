package com.auth.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
	private T data;
	private String status;
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
}
