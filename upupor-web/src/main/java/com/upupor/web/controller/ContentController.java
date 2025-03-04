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

package com.upupor.web.controller;

import com.upupor.framework.CcConstant;
import com.upupor.service.business.aggregation.service.ContentService;
import com.upupor.service.business.aggregation.service.MemberIntegralService;
import com.upupor.service.common.*;
import com.upupor.service.business.aggregation.dao.entity.Content;
import com.upupor.service.business.aggregation.dao.entity.ContentData;
import com.upupor.service.business.aggregation.dao.entity.MemberIntegral;
import com.upupor.service.business.aggregation.dao.mapper.MemberIntegralMapper;
import com.upupor.service.listener.event.ContentLikeEvent;
import com.upupor.service.outer.req.*;
import com.upupor.service.types.ContentStatus;
import com.upupor.service.types.MemberIntegralStatus;
import com.upupor.service.types.PinnedStatus;
import com.upupor.service.utils.CcUtils;
import com.upupor.service.utils.RedisUtil;
import com.upupor.service.utils.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.upupor.framework.CcConstant.MsgTemplate.CONTENT_INTEGRAL;
import static com.upupor.service.common.ErrorCode.FORBIDDEN_LIKE_SELF_CONTENT;


/**
 * 内容
 *
 * @author: YangRunkang(cruise)
 * @created: 2019/12/23 01:51
 */
@Api(tags = "内容服务")
@RestController
@RequiredArgsConstructor
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;
    private final MemberIntegralService memberIntegralService;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberIntegralMapper memberIntegralMapper;

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation("创建内容")
    public CcResponse add(AddContentDetailReq addContentDetailReq) {
        CcResponse cc = new CcResponse();

        Boolean addSuccess = contentService.addContent(addContentDetailReq);
        if (addSuccess) {
            // 发布成功,清除Key
            RedisUtil.remove(CcConstant.CvCache.CONTENT_CACHE_KEY + ServletUtils.getUserId());
        }
        cc.setData(addSuccess);
        return cc;
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation("更新内容")
    public CcResponse edit(UpdateContentReq updateContentReq) {
        ServletUtils.checkOperatePermission(updateContentReq.getUserId());
        CcResponse cc = new CcResponse();
        Boolean editSuccess = contentService.updateContent(updateContentReq);
        if (editSuccess) {
            // 发布成功,清除Key
            RedisUtil.remove(CcConstant.CvCache.CONTENT_CACHE_KEY + ServletUtils.getUserId());
        }
        cc.setData(editSuccess);
        return cc;
    }


    @PostMapping("/status")
    @ResponseBody
    @ApiOperation("更新内容状态")
    public CcResponse status(UpdateContentReq updateContentReq) {
        CcResponse cc = new CcResponse();
        Boolean editSuccess = contentService.updateContentStatus(updateContentReq);
        if (editSuccess) {
            // 发布成功,清除Key
            RedisUtil.remove(CcConstant.CvCache.CONTENT_CACHE_KEY + ServletUtils.getUserId());
        }
        cc.setData(editSuccess);
        return cc;
    }

    @PostMapping("/like")
    @ResponseBody
    @ApiOperation("喜欢")
    public CcResponse like(UpdateLikeReq updateLikeReq) {
        String clickUserId = ServletUtils.getUserId();
        CcResponse cc = new CcResponse();
        String contentId = updateLikeReq.getContentId();
        // 获取文章
        Content content = contentService.getNormalContent(contentId);

        // 获取文章数据
        ContentData contentData = content.getContentData();

        // 检查是否已经点过赞
        GetMemberIntegralReq getMemberIntegralReq = new GetMemberIntegralReq();
        getMemberIntegralReq.setUserId(clickUserId);
        getMemberIntegralReq.setRuleId(IntegralEnum.CLICK_LIKE.getRuleId());
        getMemberIntegralReq.setTargetId(contentId);
        getMemberIntegralReq.setStatus(MemberIntegralStatus.NORMAL);
        Boolean exists = memberIntegralService.checkExists(getMemberIntegralReq);
        if (exists) {
            // 不加状态
            List<MemberIntegral> memberIntegralList = memberIntegralService.getByGetMemberIntegralReq(getMemberIntegralReq);
            int count = 0;
            for (MemberIntegral memberIntegral : memberIntegralList) {
                count = count + memberIntegralMapper.deleteById(memberIntegral);
                minusLikeNum(contentData);
            }
            cc.setData(count > 0);
        } else {
            if (content.getUserId().equals(clickUserId)) {
                throw new BusinessException(FORBIDDEN_LIKE_SELF_CONTENT);
            }

            // 添加积分数据
            String text = String.format("您点赞了《%s》,赠送积分", String.format(CONTENT_INTEGRAL, content.getContentId(), CcUtils.getUuId(), content.getTitle()));
            memberIntegralService.addIntegral(IntegralEnum.CLICK_LIKE, text, clickUserId, contentId);
            // 增加点赞数
            addLikeNum(contentData);
            // 通知作者有点赞消息
            ContentLikeEvent contentLikeEvent = new ContentLikeEvent();
            contentLikeEvent.setContent(content);
            contentLikeEvent.setClickUserId(clickUserId);
            eventPublisher.publishEvent(contentLikeEvent);
        }

        contentService.updateContentData(contentData);
        cc.setData(true);
        return cc;
    }

    private void addLikeNum(ContentData contentData) {
        Integer likeNum = contentData.getLikeNum();
        contentData.setLikeNum(likeNum + 1);
    }


    private void minusLikeNum(ContentData contentData) {
        Integer likeNum = contentData.getLikeNum();
        contentData.setLikeNum(likeNum - 1);
    }


    @PostMapping("/check")
    @ResponseBody
    @ApiOperation("检查文章状态是否正常")
    public CcResponse check(GetContentStatusReq getContentStatusReq) {
        CcResponse cc = new CcResponse();

        String contentId = getContentStatusReq.getContentId();

        Boolean isOk = contentService.checkStatusIsOk(contentId);
        cc.setData(isOk);
        return cc;
    }

    @PostMapping("/pinned")
    @ResponseBody
    @ApiOperation("文章置顶")
    public CcResponse pinned(PinnedReq pinnedReq) {

        String userId = ServletUtils.getUserId();

        String contentId = pinnedReq.getContentId();
        Content content = contentService.getContentByContentIdNoStatus(contentId);
        if (Objects.isNull(content)) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_EXISTS);
        }

        if (!content.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_BELONG_TO_YOU);
        }

        if (!content.getStatus().equals(ContentStatus.NORMAL)) {
            throw new BusinessException(ErrorCode.CONTENT_STATUS_NOT_NORMAL, "不能置顶");
        }

        if (PinnedStatus.PINNED.equals(pinnedReq.getPinnedStatus())) {
            cancelBeforePinnedContent(userId);

            content.setContentId(contentId);
            content.setUserId(userId);
            content.setPinnedStatus(PinnedStatus.PINNED);
            contentService.updateContent(content);
        } else if (PinnedStatus.UN_PINNED.equals(pinnedReq.getPinnedStatus())) {
            cancelBeforePinnedContent(userId);
        }

        return new CcResponse();
    }

    private void cancelBeforePinnedContent(String userId) {
        List<Content> pinnedContentList = contentService.getContentListByPinned(PinnedStatus.PINNED, userId);
        if (!CollectionUtils.isEmpty(pinnedContentList)) {
            // 先将之前的置顶文章取消置顶
            pinnedContentList.forEach(c -> {
                c.setUserId(userId);
                c.setContentId(c.getContentId());
                c.setPinnedStatus(PinnedStatus.UN_PINNED);
                contentService.updateContent(c);
            });
        }
    }


}
