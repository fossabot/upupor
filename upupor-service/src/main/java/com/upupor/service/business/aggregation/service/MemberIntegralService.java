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

import com.upupor.service.common.IntegralEnum;
import com.upupor.service.business.aggregation.dao.entity.MemberIntegral;
import com.upupor.service.dto.page.common.ListIntegralDto;
import com.upupor.service.outer.req.GetMemberIntegralReq;

import java.util.List;

/**
 * 用户积分服务
 *
 * @author YangRunkang(cruise)
 * @date 2020/03/07 23:47
 */
public interface MemberIntegralService {

    /**
     * 检查是否存在
     *
     * @return
     */
    Boolean checkExists(GetMemberIntegralReq memberIntegralReq);

    Boolean addMemberIntegral(MemberIntegral memberIntegral);

    /**
     * 获取用户积分值
     *
     * @return
     */
    Integer getUserIntegral(String userId);

    ListIntegralDto list(String userId, Integer pageNum, Integer pageSize);

    /**
     * @param userId
     * @param ruleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ListIntegralDto list(String userId, Integer ruleId, Integer pageNum, Integer pageSize);

    boolean addIntegral(IntegralEnum integralEnum, String desc, String userId, String targetId);

    boolean addIntegral(IntegralEnum integralEnum, String userId, String targetId);

    boolean reduceIntegral(IntegralEnum integralEnum, String desc, String userId, String targetId);

    List<MemberIntegral> getByGetMemberIntegralReq(GetMemberIntegralReq getMemberIntegralReq);

    void update(MemberIntegral memberIntegral);
}
