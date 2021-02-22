package com.example.demo.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminUserInfoVO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Description:用户信息 API Controller
 * date: 2021/2/19 18:10
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    @Autowired
    private UserService userService;

    /**
     * 获取用户列表
     * @param id 用户ID
     * @param isReal 是否实名认证
     * @param isEnable 是否启用
     * @param isLock 是否冻结
     * @param username 用户名
     * @param tel 手机号
     */
    @RequestMapping("/list")
    /*
      前台表格开启分页后，并不代表着可以进行分页了，需要和后台进行交互，
      开启分页只是默认给后台的方法传入两个参数（page和limit）
      后台接收前台的分页参数，注意，这里的page和limit参数并没有显性的展示出来，
      只是表格开启分页之后默认就传给后台了，参数名是固定的。
      */
    public LayuiTableVO<AdminUserInfoVO> listUser(@RequestParam Integer page, //当前页
                                                  @RequestParam Integer limit,  //每页显示行数
                                                  String isReal, String isEnable, String isLock, String role,
                                                  String id, String username, String tel) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        System.out.println("当前页："+page+"\t每页显示行数："+limit);
        System.out.println("isReal："+isReal+'\t'+"isEnable："+isEnable+'\t'+"isLock："+isLock+'\t'+"role："+role+'\t'+"id："+id+'\t'+"username："+username+'\t'+"tel："+tel);
        Integer enable = StringUtils.toInteger(isEnable, -1);
        if (enable != -1) {
            wrapper.eq("has_enable", enable);
        }

        Integer roleType = StringUtils.toInteger(role, -1);
        UserRoleEnum roleEnum = UserRoleEnum.getByType(roleType);
        if (roleEnum != null) {
            wrapper.eq("role_id", roleType);
        }

        Integer userId = StringUtils.toInteger(id);

        if (StringUtils.isNotBlank(id)) {
            wrapper.eq("uid", userId);
        }

        if (StringUtils.isNotBlank(username)) {
            wrapper.like("username", username);
        }

        if (StringUtils.isNotBlank(tel)) {
            wrapper.eq("tel", tel);
        }

        Integer lock = StringUtils.toInteger(isLock, -1);
        if (lock == 1) {
            wrapper.ge("lock_date", LocalDateTime.now());
        } else if (lock == 0) {
            wrapper.isNull("lock_date");
        }

        Integer real = StringUtils.toInteger(isReal, -1);
        if (real == 1) {
            wrapper.isNotNull("real_name").isNotNull("id_card");
        } else if (real == 0) {
            wrapper.isNull("real_name").isNull("id_card");
        }

        System.out.println(userService.pageAdminUserInfoVO(new Page<>(page, limit), wrapper));

        return userService.pageAdminUserInfoVO(new Page<>(page, limit), wrapper);
    }

    /**
     * 改变用户状态
     * @param type 1. 禁用；2：启用；3：冻结；4：解冻
     * @param hour 冻结小时数
     * @author jitwxs
     * @date 2019/5/2 13:50
     */
    @PostMapping("/{id}/status")
    public ResponseResult changeStatus(@PathVariable String id, String type, String hour) {
        Integer op = StringUtils.toInteger(type, -1);
        System.out.println(id+'\t'+type);
        User user = userService.getById(id);
        if(user.getRole() == UserRoleEnum.ADMIN) {
            return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        switch (op) {
            case 1:
                user.setHasEnable(0);
                break;
            case 2:
                user.setHasEnable(1);
                break;
            case 3:
                // 必须为正整数
                Integer lockHour = StringUtils.toInteger(hour, -1);
                if(lockHour == -1 || lockHour < 0) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.MUST_POSITIVE_INTEGER);
                }
                user.setLockDate(LocalDateTime.now().plusHours(lockHour));
                break;
            case 4:
                user.setLockDate(LocalDateTime.now());
                break;
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        if(userService.updateById(user)) {
            return ResponseResult.success();
        } else {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
    }
}
