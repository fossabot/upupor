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

package com.upupor.service.business.profile;

import com.upupor.service.business.aggregation.dao.entity.Content;
import com.upupor.service.business.aggregation.dao.entity.Member;
import com.upupor.service.business.aggregation.dao.entity.Tag;
import com.upupor.service.business.aggregation.service.*;
import com.upupor.service.common.BusinessException;
import com.upupor.framework.CcConstant;
import com.upupor.service.common.ErrorCode;
import com.upupor.service.dto.page.MemberIndexDto;
import com.upupor.service.types.ViewTargetType;
import com.upupor.service.utils.CcUtils;
import com.upupor.service.utils.ServletUtils;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.ArrayUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人主页抽象
 *
 * @author Yang Runkang (cruise)
 * @date 2021年12月27日 21:00
 * @email: yangrunkang53@gmail.com
 */

public abstract class AbstractProfile {
    @Resource
    private ViewerService viewerService;
    @Resource
    private MemberService memberService;
    @Resource
    private FanService fanService;
    @Resource
    private AttentionService attentionService;
    @Resource
    private ContentService contentService;
    @Resource
    private TagService tagService;

    private final MemberIndexDto memberIndexDto = new MemberIndexDto();

    public MemberIndexDto getMemberIndexDto() {
        return memberIndexDto;
    }

    public MemberIndexDto getBusinessData(String userId, Integer pageNum, Integer pageSize) {
        setMember(userId);
        setSpecifyData(userId, pageNum, pageSize);
        recordViewer(userId);
        addAd();
        return memberIndexDto;
    }

    private void setMember(String userId) {
        Member member = memberService.memberInfo(userId);
        if (Objects.isNull(member)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_EXISTS);
        }
        // 设置用户粉丝数
        int fansNum = fanService.getFansNum(member.getUserId());
        member.setFansNum(fansNum);
        // 设置用户关注数
        Integer attentionNum = attentionService.getAttentionNum(member.getUserId());
        member.setAttentionNum(attentionNum);
        // 设置文章总数
        Integer totalContentNum = contentService.getUserTotalContentNum(member.getUserId());
        member.setTotalContentNum(totalContentNum);
        // 获取用户总积分值
        Integer totalIntegral = memberService.totalIntegral(member.getUserId());
        member.setTotalIntegral(totalIntegral);

        memberIndexDto.setTagList(getUserTagList(userId));
        memberIndexDto.setCurrUserIsAttention(contentService.currentUserIsAttentionAuthor(userId));

        // 设置用户声明
        memberService.bindStatement(member);

        try {
            if (member.getUserId().equals(ServletUtils.getUserId())) {
                memberIndexDto.setIsThatYouEnterYourProfile(true);
            }
        } catch (Exception e) {

        }
        // 获取用户属性
        memberIndexDto.setMember(member);
    }

    /**
     * 记录浏览者
     *
     * @param userId
     */
    private void recordViewer(String userId) {
        // 记录访问者
        viewerService.addViewer(userId, viewTargetType());
        // 设置访问者
        memberIndexDto.setViewerList(viewerService.listViewerByTargetIdAndType(userId, viewTargetType()));
    }

    /**
     * 添加广告
     */
    protected abstract void addAd();

    /**
     * 浏览者类型
     *
     * @return
     */
    public abstract ViewTargetType viewTargetType();

    /**
     * 获取数据
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    protected abstract void setSpecifyData(String userId, Integer pageNum, Integer pageSize);


    private List<Tag> getUserTagList(String userId) {

        // 获取用户标签
        List<Content> contentList = contentService.listAllByUserId(userId);

        if (CollectionUtils.isEmpty(contentList)) {
            return new ArrayList<>();
        }

        List<String> tagIdList = contentList.stream()
                .map(Content::getTagIds).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tagIdList)) {
            return new ArrayList<>();
        }
        String s = CcUtils.convertListToStr(tagIdList);
        String[] split = s.split(CcConstant.COMMA);
        if (ArrayUtils.isEmpty(split)) {
            return new ArrayList<>();
        }

        List<Tag> tags = tagService.listByTagIdList(Arrays.asList(split));
        if (CollectionUtils.isEmpty(tags)) {
            return new ArrayList<>();
        }
        return tags;
    }

}
