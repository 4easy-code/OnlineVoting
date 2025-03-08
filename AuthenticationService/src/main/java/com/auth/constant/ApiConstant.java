package com.auth.constant;

public class ApiConstant {
	public static final String AUTH = "/auth";
	public static final String CREATE_USER = "/createUser";
	public static final String LOGIN = "/login";
	public static final String CREATE_OTP = "/createOtp/{userToken}";
	public static final String VALIDATE_USER = "/validateUser";
	
	
	public static final String USERS = "/users";
	public static final String GET_USER_DETAILS = "/getUserDetails/{usernameOrEmail}";
	public static final String CHANGE_PASSWORD = "/changePassword";
	public static final String UPDATE = "/update";
	public static final String DELETE = "/delete";
	
	
	public static final String REFRESH = "/refresh";
	public static final String CREATE_ACCESSTOKEN = "/createAccessToken";
}
