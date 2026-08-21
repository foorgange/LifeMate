package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/** 关注服务接口：关注/取关/是否关注/共同关注。实现见 FollowServiceImpl。 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);
}
