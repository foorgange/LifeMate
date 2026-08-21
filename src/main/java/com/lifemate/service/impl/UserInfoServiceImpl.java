package com.lifemate.service.impl;

import com.lifemate.entity.UserInfo;
import com.lifemate.mapper.UserInfoMapper;
import com.lifemate.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 用户详情服务实现（基础 CRUD）。 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
