package com.zulong.web.controller;

import com.zulong.web.entity.Group;
import com.zulong.web.entity.User;
import com.zulong.web.log.LoggerManager;
import com.zulong.web.service.AuthenticationService;
import com.zulong.web.service.GroupService;
import com.zulong.web.service.UserService;
import com.zulong.web.utils.ParamsUtil;
import com.zulong.web.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static com.zulong.web.config.ConstantConfig.*;

@RestController
@RequestMapping(value = "/auth/user")
public class UserController {
    private final UserService userService;
    private final GroupService groupService;
    private final AuthenticationService authenticationService;
    @Autowired
    public UserController(UserService userService, GroupService groupService, AuthenticationService authenticationService){
        this.userService = userService;
        this.groupService = groupService;
        this.authenticationService = authenticationService;
    }
    public static Map<String, Object> errorResponse(final Map<String, Object> response, int errorCode, final String message) {
        response.put("code", errorCode);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> successResponse(final Map<String, Object> response, final Object data) {
        response.put("code", RETURN_SUCCESS);
        response.put("data", data);
        return response;
    }

    public static Object getParam(Map<String, Object> request, String varName, String functionName){
        return ParamsUtil.getParam(request,varName,"UserController",functionName);
    }
    @PostMapping(value = "/listgroup")
    public Map<String, Object> getAllGroups(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        String user_id;
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try{
            user_id = TokenUtils.getCurrUserId(token);
        }catch (Exception e){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.getAllGroups@please login first|"), e);
            return errorResponse(response, RETURN_PARAMS_NULL,e.getMessage());
        }
        try {
            List<Group> groupList = userService.getAllGroups(user_id);
            data.put("items", groupList);
            data.put("user_id", user_id);
            return successResponse(response,data);
        } catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.getAllGroups@operation failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
    }

    @PostMapping(value = "/create")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        if(!authenticationService.isAdmin(TokenUtils.getCurrUserId(token))){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]GroupController.getAllUsers@this user is not admin|"));
            return errorResponse(response,RETURN_NO_AUTHORITY,"this user is not admin, no authority");
        }
        String user_id;
        boolean is_admin;
        try{
            user_id = (String) getParam(request,"user_id","createUser");
            if(user_id == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "user_id is null");
            }
            Object tmp = getParam(request,"is_admin","createUser");
            if(tmp == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "is_admin is null");
            }
            is_admin = (boolean) tmp;
        }catch (Exception e){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.createUser@params are wrong|"), e);
            return errorResponse(response, RETURN_PARAMS_NULL,e.getMessage());
        }

        try {
            userService.createUser(user_id, is_admin);
            response.put("code", RETURN_SUCCESS);
            response.put("message", "success");
        } catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.createUser@operation failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
        try {
            User user = new User();
            user.setAdmin(is_admin);
            user.setUser_id(user_id);
            String user_token = TokenUtils.sign(user);
            //将令牌放置在响应体中返回
            response.put("Authorization",String.format("Bearer %s",user_token));
            return response;
        }catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.createUser@operation failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
    }

    @PostMapping(value = "/logout")
    public Map<String, Object> logoutUser(@RequestHeader("Authorization") String token, HttpServletResponse responseRedirect){
        Map<String, Object> response = new HashMap<>();
        try{
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            TokenUtils.loginOut(token);
            response.put("code", RETURN_SUCCESS);
            response.put("message", "logout success, token is clear");
            //responseRedirect.sendRedirect("https://ztp-sso.zulong.com/");
            return response;
        }catch (Exception e){
            LoggerManager.logger().error(String.format("[com.zulong.web.controller]UserController.logoutUser@please login first|"), e);
            return errorResponse(response, RETURN_PARAMS_NULL,e.getMessage());
        }
    }

    @PostMapping(value = "/login")
    public Map<String, Object> loginUser(@RequestBody Map<String, Object> request) {
        String user_id;
        Map<String, Object> response = new HashMap<>();
        try{
            user_id = (String) getParam(request,"user_id","loginUser");
            if(user_id == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "user_id is null");
            }
        }catch (Exception e){
            LoggerManager.logger().error(String.format("[com.zulong.web.controller]UserController.loginUser@user_id is invalid|"), e);
            return errorResponse(response, RETURN_USER_NOT_EXISTS, e.getMessage());
        }
        User user = null;
        try {
            user = userService.getUserByUserId(user_id);
            if(user == null){
                LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.loginUser@User does not exist|user_id=%s", user_id));
                response.put("code", RETURN_USER_NOT_EXISTS);
                response.put("message","User does not exist" );
                return response;
            }
            response.put("code", RETURN_SUCCESS);

        } catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.loginUser@operation failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
        try {
            String token = TokenUtils.sign(user);
            //将令牌放置在响应体中返回
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            response.put("data", data);
            return response;
        }catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.loginUser@operation failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
    }

    @PostMapping(value = "/addtogroup")
    public Map<String, Object> addToGroup(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        if(!authenticationService.isAdmin(TokenUtils.getCurrUserId(token))){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]GroupController.getAllUsers@this user is not admin|user_id=%s", TokenUtils.getCurrUserId(token)));
            return errorResponse(response,RETURN_NO_AUTHORITY,"this user is not admin, no authority");
        }
        String user_id;
        int group_id;
        int code;
        String message;
        try{
            user_id = (String) getParam(request,"user_id","addtogroup");
            if(user_id == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "user_id is null");
            }
            Object tmp = getParam(request,"group_id","addtogroup");
            if(tmp == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "group_id is null");
            }
            group_id = (int) tmp;
            if( ParamsUtil.isInValidInt(group_id)){
                return errorResponse(response, RETURN_PARAMS_WRONG, "group_id is invalid");
            }
        }catch (Exception e){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.addToGroup@params are wrong|"), e);
            return errorResponse(response, RETURN_PARAMS_NULL,e.getMessage());
        }
        try {
            if(!userService.findByUserID(user_id)) {
                // 用户不存在
                LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.addToGroup@user doesn't exist|user_id=%s|group_id=%d", user_id, group_id));
                return errorResponse(response, RETURN_USER_NOT_EXISTS, "user doesn't exist");
            }
            if(!groupService.findGroup(group_id)) {
                LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.addToGroup@group doesn't exist|user_id=%s|group_id=%d", user_id, group_id));
                return errorResponse(response, RETURN_NO_OBJECT, "group doesn't exist");
            }
            boolean flag = userService.addToGroup(user_id, group_id);
            if(flag) {
                code = RETURN_SUCCESS;
                message = "success";
            }
            else {
                code = RETURN_DATABASE_WRONG;
                message = "database wrong";
            }
            response.put("code", code);
            response.put("message", message);
            return response;
        } catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.addToGroup@operation failed|user_id=%s|group_id=%d", user_id, group_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
    }

    @PostMapping(value = "/removefromgroup")
    public Map<String, Object> removeFromGroup(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        if(!authenticationService.isAdmin(TokenUtils.getCurrUserId(token))){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]GroupController.getAllUsers@this user is not admin|"));
            return errorResponse(response,RETURN_NO_AUTHORITY,"this user is not admin, no authority");
        }
        String user_id;
        int group_id;
        try{
            user_id = (String) getParam(request,"user_id","removeFromGroup");
            if(user_id == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "user_id is null");
            }
            Object tmp = getParam(request,"group_id","removeFromGroup");
            if(tmp == null) {
                return errorResponse(response, RETURN_PARAMS_NULL, "group_id is null");
            }
            group_id = (int) tmp;
            if( ParamsUtil.isInValidInt(group_id)){
                return errorResponse(response, RETURN_PARAMS_WRONG, "group_id is invalid");
            }
        }catch (Exception e){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.removeFromGroup@params are wrong|"), e);
            return errorResponse(response, RETURN_PARAMS_NULL,e.getMessage());
        }
        try {
            boolean flag = userService.removeFromGroup(user_id, group_id);
            int code;
            String message;
            if(flag) {
                code = RETURN_SUCCESS;
                message = "success";
            }
            else {
                LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.removeFromGroup@operation failed|user_id=%s|group_id=%d", user_id, group_id));
                code = RETURN_DATABASE_WRONG;
                message = "failed";
            }
            response.put("code", code);
            response.put("message", message);
            return response;
        } catch (Exception e) {
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.removeFromGroup@operation failed|user_id=%s|group_id=%d", user_id, group_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }
    }

    // 将要废弃，因为仅仅用于返回user_id为admin的用户的token
    @PostMapping(value = "/gettoken")
    public Map<String, Object> getToken(@RequestBody Map<String, String> request){
        String user_id = request.get("user_id");
        User admin_user = new User();
        admin_user.setAdmin(true);
        admin_user.setUser_id(user_id);
        Map<String, Object> response = new HashMap<>();
        if (user_id.equals("admin")) {
            //身份验证成功，生成JWT令牌
            String token = TokenUtils.sign(admin_user);
            //将令牌放置在响应体中返回
            response.put("code",RETURN_SUCCESS);
            response.put("Authentication", String.format("Bearer %s",token));
            return response;
        }else{
            //身份验证失败
            return errorResponse(response,RETURN_NO_AUTHORITY,"this user is not admin, no authority");
        }
    }
    @PostMapping(value = "/returntoken")
    public Map<String, Object> getToken(@RequestParam(value = "user_id", required = false) String user_id){
        User admin_user = new User();
        admin_user.setAdmin(true);
        admin_user.setUser_id(user_id);
        Map<String, Object> response = new HashMap<>();
        try{
            String token = TokenUtils.sign(admin_user);
            //将令牌放置在响应体中返回
            response.put("code",RETURN_SUCCESS);
            response.put("Authentication", String.format("Bearer %s",token));
            return response;
        }
       catch (Exception e){
           return errorResponse(response,RETURN_NO_AUTHORITY,e.getMessage());
       }
    }
    @PostMapping(value = "/info")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        String user_id;
        try{
            user_id = TokenUtils.getCurrUserId(token);
            if(user_id == null) {
                return errorResponse(response, RETURN_TOKEN_EXPIRED, "The token has expired, get user_id by token failed");
            }
        }catch (Exception e){
            LoggerManager.logger().error(String.format("[com.zulong.web.controller]UserController.loginUser@current token is invalid|token=%s", token), e);
            return errorResponse(response, RETURN_USER_NOT_EXISTS, e.getMessage());
        }

        User user;
        try{
            user = userService.getUserByUserId(user_id);
            if(user == null){
                return errorResponse(response, RETURN_USER_NOT_EXISTS, "user not exist");
            }
        }catch (Exception e){
            LoggerManager.logger().warn(String.format("[com.zulong.web.controller]UserController.getUserInfo@get User info failed|user_id=%s", user_id), e);
            return errorResponse(response,RETURN_SERVER_WRONG,e.getMessage());
        }

        return successResponse(response, user);
    }
}
