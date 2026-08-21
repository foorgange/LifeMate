package com.lifemate.service.impl;

import com.lifemate.entity.BlogComments;
import com.lifemate.mapper.BlogCommentsMapper;
import com.lifemate.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 博客评论服务实现（基础 CRUD）。 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
