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

package com.upupor.service.business.aggregation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.upupor.service.business.aggregation.dao.entity.Member;
import com.upupor.service.business.aggregation.dao.entity.ViewHistory;
import com.upupor.service.business.aggregation.dao.entity.Viewer;
import com.upupor.service.business.aggregation.dao.mapper.ViewHistoryMapper;
import com.upupor.service.business.aggregation.dao.mapper.ViewerMapper;
import com.upupor.service.business.aggregation.service.MemberService;
import com.upupor.service.business.aggregation.service.ViewerService;
import com.upupor.service.business.viewhistory.AbstractViewHistory;
import com.upupor.service.common.CcTemplateConstant;
import com.upupor.service.dto.page.common.ListViewHistoryDto;
import com.upupor.service.listener.event.ViewerEvent;
import com.upupor.service.types.ViewTargetType;
import com.upupor.service.utils.HtmlTemplateUtils;
import com.upupor.service.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YangRunkang(cruise)
 * @date 2021/01/27 23:25
 */
@Service
@RequiredArgsConstructor
public class ViewerServiceImpl implements ViewerService {

    private final ViewerMapper viewerMapper;
    private final ViewHistoryMapper viewHistoryMapper;
    private final MemberService memberService;
    private final ApplicationEventPublisher eventPublisher;
    private final List<AbstractViewHistory> abstractViewHistoryList;

    @Override
    public void addViewer(String targetId, ViewTargetType targetType) {
        String userId = null;
        try {
            userId = ServletUtils.getUserId();
        } catch (Exception ignore) {
        }

        if (Objects.isNull(userId)) {
            return;
        }
        ViewerEvent viewerEvent = new ViewerEvent();
        viewerEvent.setTargetId(targetId);
        viewerEvent.setTargetType(targetType);
        viewerEvent.setViewerUserId(userId);
        eventPublisher.publishEvent(viewerEvent);
    }

    @Override
    public List<Viewer> listViewerByTargetIdAndType(String targetId, ViewTargetType targetType) {
        List<Viewer> viewerList = viewerMapper.listByTargetId(targetId, targetType.getType());
        if (CollectionUtils.isEmpty(viewerList)) {
            return new ArrayList<>();
        }

        List<String> userIdList = viewerList.stream().map(Viewer::getViewerUserId).distinct().collect(Collectors.toList());
        List<Member> members = memberService.listByUserIdList(userIdList);
        if (CollectionUtils.isEmpty(members)) {
            return viewerList;
        }

        viewerList.forEach(viewer -> {
            members.forEach(member -> {
                if (member.getUserId().equals(viewer.getViewerUserId())) {
                    Integer fanNum = memberService.fansNum(member.getUserId());
                    Integer totalIntegral = memberService.sumIntegral(member.getUserId());

                    Map<String, Object> params = new HashMap<>();
                    params.put(CcTemplateConstant.USER_ID, member.getUserId());
                    params.put(CcTemplateConstant.USER_NAME, member.getUserName());
                    params.put(CcTemplateConstant.USER_FAN_NUM, fanNum);
                    params.put(CcTemplateConstant.USER_INTEGRAL_NUM, totalIntegral);

                    viewer.setViewerUserVia(member.getVia());
                    viewer.setViewerUserName(member.getUserName());
                    viewer.setCardHtml(HtmlTemplateUtils.renderMemberCardHtml(CcTemplateConstant.MEMBER_CARD_HTML, params));
                }
            });
        });
        return viewerList;
    }

    @Override
    public ListViewHistoryDto listViewHistoryByUserId(String userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ViewHistory> query = new LambdaQueryWrapper<ViewHistory>()
                .eq(ViewHistory::getViewerUserId, userId)
                .orderByDesc(ViewHistory::getCreateTime);
        PageHelper.startPage(pageNum, pageSize);
        List<ViewHistory> viewHistories = viewHistoryMapper.selectList(query);
        PageInfo<ViewHistory> pageInfo = new PageInfo<>(viewHistories);

        ListViewHistoryDto listViewHistoryDto = new ListViewHistoryDto(pageInfo);
        setViewHistoryTitle(pageInfo.getList());

        listViewHistoryDto.setTotal(pageInfo.getTotal());
        listViewHistoryDto.setViewHistoryList(pageInfo.getList());
        return listViewHistoryDto;
    }

    /**
     * 设置浏览记录的title
     *
     * @param viewHistoryList
     */
    private void setViewHistoryTitle(List<ViewHistory> viewHistoryList) {
        if (CollectionUtils.isEmpty(viewHistoryList)) {
            return;
        }

        Map<ViewTargetType, List<ViewHistory>> map = viewHistoryList.stream().collect(Collectors.groupingBy(ViewHistory::getTargetType));

        for (ViewTargetType targetType : map.keySet()) {
            for (AbstractViewHistory abstractViewHistory : abstractViewHistoryList) {
                if (abstractViewHistory.viewTargetType().equals(targetType)) {
                    abstractViewHistory.initData(map.get(targetType));
                    abstractViewHistory.setViewHistoryTitleAndUrl();
                }
            }
        }


    }


}
