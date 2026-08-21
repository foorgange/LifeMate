package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/** 博客服务接口：热门/详情/点赞/发布/关注流。实现见 BlogServiceImpl。 */
public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result updateLike(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);
}
