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

package com.upupor.service.business.apply;

import com.alibaba.fastjson.JSON;
import com.upupor.framework.utils.CcDateUtil;
import com.upupor.service.common.BusinessException;
import com.upupor.service.common.ErrorCode;
import com.upupor.service.business.aggregation.dao.entity.Apply;
import com.upupor.service.dto.page.apply.ApplyContentDto;
import com.upupor.service.outer.req.AddConsultantReq;
import com.upupor.service.types.ApplyStatus;
import com.upupor.service.utils.CcUtils;
import com.upupor.service.utils.ServletUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Yang Runkang (cruise)
 * @date 2022年01月02日 20:08
 * @email: yangrunkang53@gmail.com
 */
@Component
public class ConsultantApply extends AbstractApply<AddConsultantReq> {
    @Override
    protected void notifyAdministrator(Apply apply) {
        // 发送邮件
        String emailContent = "申请id:%s" + "\n" +
                "用户id:%s" + "\n" +
                "申请简介:%s" + "\n" +
                "申请项目:%s" + "\n";
        emailContent = String.format(emailContent, apply.getApplyId(),
                apply.getUserId(),
                apply.getApplyContentDto().getApplyIntro(),
                apply.getApplyContentDto().getApplyProject()
        );

        sendEmail("您有新的咨询服务!!!请尽快处理", emailContent);


    }

    @Override
    protected Boolean apply(AddConsultantReq addConsultantReq) {
        if (CcUtils.isAllEmpty(addConsultantReq.getTopic(), addConsultantReq.getDesc())) {
            throw new BusinessException(ErrorCode.PATH_ERROR);
        }

        Apply apply = new Apply();
        apply.setApplyId(CcUtils.getUuId());
        apply.setUserId(ServletUtils.getUserId());

        ApplyContentDto applyContentDto = new ApplyContentDto();
        applyContentDto.setApplyProject(addConsultantReq.getTopic());
        applyContentDto.setApplyIntro(addConsultantReq.getDesc());

        apply.setApplySource(addConsultantReq.getType());
        apply.setApplyContent(JSON.toJSONString(applyContentDto));
        apply.setApplyStatus(ApplyStatus.WAIT_APPLY);
        apply.setCreateTime(CcDateUtil.getCurrentTime());
        apply.setSysUpdateTime(new Date());
        return apply(apply);
    }
}
