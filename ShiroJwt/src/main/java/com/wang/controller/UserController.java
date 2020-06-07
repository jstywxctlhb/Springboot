package com.wang.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wang.model.common.BaseDto;
import com.wang.model.valid.group.UserEditValidGroup;
import com.wang.model.valid.group.UserLoginValidGroup;
import com.wang.util.JedisUtil;
import com.wang.exception.CustomException;
import com.wang.exception.CustomUnauthorizedException;
import com.wang.model.UserDto;
import com.wang.model.common.Constant;
import com.wang.model.common.ResponseBean;
import com.wang.service.IUserService;
import com.wang.util.AesCipherUtil;
import com.wang.util.JwtUtil;
import com.wang.util.UserUtil;
import com.wang.util.common.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * UserController
 */
@RestController
@RequestMapping("/user")
@PropertySource("classpath:config.properties")
public class UserController {

    /**
     * RefreshToken过期时间
     */
    @Value("${refreshTokenExpireTime}")
    private String refreshTokenExpireTime;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private IUserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping
    @RequiresPermissions(logical = Logical.AND, value = {"user:view"})
    public ResponseBean user(@Validated BaseDto baseDto) {
        if (baseDto.getPage() == null
                || baseDto.getRows() == null) {
            baseDto.setPage(1);
            baseDto.setRows(10);
        }
        PageHelper.startPage(baseDto.getPage(), baseDto.getRows());
        List<UserDto> userDtos = userService.selectAll();
        PageInfo<UserDto> selectPage = new PageInfo<>(userDtos);
        if (userDtos == null) {
            throw new CustomException("Query Failure");
        }
        Map<String, Object> result = new HashMap<>(16);
        result.put("count", selectPage.getTotal());
        result.put("data", selectPage.getList());
        return new ResponseBean(HttpStatus.OK.value(), "Query was successful", result);
    }

    /**
     * 获取在线用户(查询Redis中的RefreshToken)
     */
    @GetMapping("/online")
    @RequiresPermissions(logical = Logical.AND, value = {"user:view"})
    public ResponseBean online() {
        List<Object> userDtos = new ArrayList<>();
        // 查询所有Redis键
        Set<String> keys = JedisUtil.keysS(Constant.PREFIX_SHIRO_REFRESH_TOKEN + "*");
        for (String key : keys) {
            if (JedisUtil.exists(key)) {
                // 根据:分割key，获取最后一个字符(帐号)
                String[] strArray = key.split(":");
                UserDto userDto = new UserDto();
                userDto.setAccount(strArray[strArray.length - 1]);
                userDto = userService.selectOne(userDto);
                // 设置登录时间
                userDto.setLoginTime(new Date(Long.parseLong(JedisUtil.getObject(key).toString())));
                userDtos.add(userDto);
            }
        }
        return new ResponseBean(HttpStatus.OK.value(), "Query was successful", userDtos);
    }

    /**
     * 登录授权
     */
    @PostMapping("/login")
    public ResponseBean login(@Validated(UserLoginValidGroup.class) @RequestBody UserDto userDto, HttpServletResponse httpServletResponse) {
        // 查询数据库中的帐号信息
        UserDto userDtoTemp = new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        userDtoTemp = userService.selectOne(userDtoTemp);
        if (userDtoTemp == null) {
            throw new CustomUnauthorizedException("The account does not exist.");
        }
        // 密码进行AES解密
        String key = AesCipherUtil.deCrypto(userDtoTemp.getPassword());
        // 因为密码加密是以帐号+密码的形式进行加密的，所以解密后的对比是帐号+密码
        if (key.equals(userDto.getAccount() + userDto.getPassword())) {
            // 清除可能存在的Shiro权限信息缓存
            if (JedisUtil.exists(Constant.PREFIX_SHIRO_CACHE + userDto.getAccount())) {
                JedisUtil.delKey(Constant.PREFIX_SHIRO_CACHE + userDto.getAccount());
            }
            // 设置RefreshToken，时间戳为当前时间戳，直接设置即可(不用先删后设，会覆盖已有的RefreshToken)
            String currentTimeMillis = String.valueOf(System.currentTimeMillis());
            JedisUtil.setObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount(), currentTimeMillis, Integer.parseInt(refreshTokenExpireTime));
            // 从Header中Authorization返回AccessToken，时间戳为当前时间戳
            String token = JwtUtil.sign(userDto.getAccount(), currentTimeMillis);
            httpServletResponse.setHeader("Authorization", token);
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
            return new ResponseBean(HttpStatus.OK.value(), "Login Success.", null);
        } else {
            throw new CustomUnauthorizedException("Account or Password Error.");
        }
    }

    /**
     * 测试登录
     */
    @GetMapping("/article")
    public ResponseBean article() {
        Subject subject = SecurityUtils.getSubject();
        // 登录了返回true
        if (subject.isAuthenticated()) {
            return new ResponseBean(HttpStatus.OK.value(), "You are already logged in.", null);
        } else {
            return new ResponseBean(HttpStatus.OK.value(), "You are guest.", null);
        }
    }

    /**
     * 测试登录注解(@RequiresAuthentication和subject.isAuthenticated()返回true一个性质)
     */
    @GetMapping("/article2")
    @RequiresAuthentication
    public ResponseBean requireAuth() {
        return new ResponseBean(HttpStatus.OK.value(), "You are already logged in.", null);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    @RequiresAuthentication
    public ResponseBean info() {
        // 获取当前登录用户
        UserDto userDto = userUtil.getUser();
        // 获取当前登录用户Id
        Integer id = userUtil.getUserId();
        // 获取当前登录用户Token
        String token = userUtil.getToken();
        // 获取当前登录用户Account
        String account = userUtil.getAccount();
        return new ResponseBean(HttpStatus.OK.value(), "You are already logged in.", userDto);
    }

    /**
     * 获取指定用户
     */
    @GetMapping("/{id}")
    @RequiresPermissions(logical = Logical.AND, value = {"user:view"})
    public ResponseBean findById(@PathVariable("id") Integer id) {
        UserDto userDto = userService.selectByPrimaryKey(id);
        if (userDto == null) {
            throw new CustomException("查询失败(Query Failure)");
        }
        return new ResponseBean(HttpStatus.OK.value(), "查询成功(Query was successful)", userDto);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean add(@Validated(UserEditValidGroup.class) @RequestBody UserDto userDto) {
        // 判断当前帐号是否存在
        UserDto userDtoTemp = new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        userDtoTemp = userService.selectOne(userDtoTemp);
        if (userDtoTemp != null && StringUtil.isNotBlank(userDtoTemp.getPassword())) {
            throw new CustomUnauthorizedException("Account exist.");
        }
        userDto.setRegTime(new Date());
        // 密码以帐号+密码的形式进行AES加密
        if (userDto.getPassword().length() > Constant.PASSWORD_MAX_LEN) {
            throw new CustomException("Password up to 8 bits.");
        }
        String key = AesCipherUtil.enCrypto(userDto.getAccount() + userDto.getPassword());
        userDto.setPassword(key);
        int count = userService.insert(userDto);
        if (count <= 0) {
            throw new CustomException("Insert Failure.");
        }
        return new ResponseBean(HttpStatus.OK.value(), "Insert Success.", userDto);
    }

    /**
     * 更新用户
     */
    @PutMapping
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean update(@Validated(UserEditValidGroup.class) @RequestBody UserDto userDto) {
        // 查询数据库密码
        UserDto userDtoTemp = new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        userDtoTemp = userService.selectOne(userDtoTemp);
        if (userDtoTemp == null) {
            throw new CustomUnauthorizedException("Account not exist.");
        } else {
            userDto.setId(userDtoTemp.getId());
        }
        if (!userDtoTemp.getPassword().equals(userDto.getPassword())) {
            // 密码以帐号+密码的形式进行AES加密
            if (userDto.getPassword().length() > Constant.PASSWORD_MAX_LEN) {
                throw new CustomException("Password up to 8 bits.");
            }
            String key = AesCipherUtil.enCrypto(userDto.getAccount() + userDto.getPassword());
            userDto.setPassword(key);
        }
        int count = userService.updateByPrimaryKeySelective(userDto);
        if (count <= 0) {
            throw new CustomException("Update Failure.");
        }
        return new ResponseBean(HttpStatus.OK.value(), "Update Success.", userDto);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean delete(@PathVariable("id") Integer id) {
        int count = userService.deleteByPrimaryKey(id);
        if (count <= 0) {
            throw new CustomException("Deletion Failed. ID does not exist.");
        }
        return new ResponseBean(HttpStatus.OK.value(), "Delete Success.", null);
    }

    /**
     * 剔除在线用户
     */
    @DeleteMapping("/online/{id}")
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean deleteOnline(@PathVariable("id") Integer id) {
        UserDto userDto = userService.selectByPrimaryKey(id);
        if (JedisUtil.exists(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount())) {
            if (JedisUtil.delKey(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount()) > 0) {
                return new ResponseBean(HttpStatus.OK.value(), "Delete Success.", null);
            }
        }
        throw new CustomException("Deletion Failed. Account does not exist.");
    }
}