/*
 * MIT License
 *
 * Copyright (c) 2021-2022 yangrunkang
 *
 * Author: yangrunkang
 * Email: yangrunkang53@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.upupor.service.business.aggregation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upupor.service.business.aggregation.dao.entity.Attention;
import com.upupor.service.dto.page.common.ListAttentionDto;
import com.upupor.service.outer.req.AddAttentionReq;
import com.upupor.service.outer.req.DelAttentionReq;

/**
 * 关注服务
 *
 * @author YangRunkang(cruise)
 * @date 2020/02/01 21:18
 */
public interface AttentionService {

    /**
     * 添加关注
     *
     * @param attention
     * @return
     */
    Integer add(Attention attention);

    Attention getAttentionByAttentionId(String attentionId);

    Attention getAttention(String attentionUserId,String userId);


    /**
     * 获取关注者
     *
     * @param userId
     * @return
     */
    ListAttentionDto getAttentions(String userId, Integer pageNum, Integer pageSize);

    /**
     * 获取关注者
     *
     * @param userId
     * @return
     */
    Integer getAttentionNum(String userId);

    Attention select(LambdaQueryWrapper<Attention> queryAttention);

    Boolean attention(AddAttentionReq addAttentionReq);

    Boolean delAttention(DelAttentionReq delAttentionReq);
}
