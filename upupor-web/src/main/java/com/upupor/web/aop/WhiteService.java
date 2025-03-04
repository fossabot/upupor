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

package com.upupor.web.aop;

import com.upupor.service.common.BusinessException;
import com.upupor.framework.CcConstant;
import com.upupor.service.common.ErrorCode;
import com.upupor.framework.config.UpuporConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.upupor.service.utils.ServletUtils.getSession;

/**
 * @author Yang Runkang (cruise)
 * @date 2021年12月12日 23:16
 */
@Service
@RequiredArgsConstructor
public class WhiteService {
    private final UpuporConfig upuporConfig;

    public void interfaceWhiteCheck(String servletPath) {
        if (!upuporConfig.getInterfaceWhiteUrlList().contains(servletPath)) {
            checkIsLogin();
        }
    }

    private void checkIsLogin() {
        Object userId = getSession().getAttribute(CcConstant.Session.USER_ID);
        if (Objects.isNull(userId) || CcConstant.Session.UNKNOWN_USER_ID.equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
    }

    public void pageCheck(String servletPath) {
        upuporConfig.getPageCheckUrlList().forEach(page -> {
            if (servletPath.startsWith(page)) {
                checkIsLogin();
            }
        });
    }
}
