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
$(function () {
    // 优先加载回复框
    $.cvLoadBootstrapRichText(true);
    $.cvLoadShowImg();
});

function cancel() {
    let commentContent = $.cvGetEditorData();
    let mdCommentContent = $.cvGetEditorDataMd();
    if (cvIsNull(commentContent) || cvIsNull(mdCommentContent)) {
        $.cvWarn("内容为空,无需清空");
        return;
    }

    swal({
        title: '确定清空内容?',
        text: "当前输入内容将会被清除",
        icon: "warning",
        buttons: [{
            text: "确认",
            value: true,
            visible: true,
        }, {
            text: "取消",
            value: false,
            visible: true,
        }],
        closeOnClickOutside: false,
        closeOnEsc: false,
    }).then((confirmed) => {
        if (confirmed) {
            $.cvSetEditorEmpty();
        }
    });
}


/**
 * 评论
 * @param contentId 目标Id
 * @param commentSource 评论来源
 */
function comment(contentId,commentSource,desc) {
    if (cvIsNull(contentId)) {
        $.cvWarn(desc+"目标为空,禁止"+desc);
        return;
    }
    if (cvIsNull(commentSource)) {
        $.cvWarn("来源异常,禁止"+desc);
        return;
    }

    let commentContent = $.cvGetEditorData();
    let mdCommentContent = $.cvGetEditorDataMd();
    if (cvIsNull(commentContent) || cvIsNull(mdCommentContent)) {
        $.cvWarn(desc+"内容为空");
        return;
    }
    let userId = $("#reply_to_user").val();
    let comment = {
        targetId: contentId,
        commentSource: commentSource,
        commentContent: commentContent,
        mdCommentContent: mdCommentContent,
        replyToUserId: userId
    };

    $.cvPost('/comment/add', comment, function (data) {
        if (respSuccess(data)) {
            $.cvSuccess(desc+"成功");
            setTimeout(function () {
                history.go()
            }, 1500);
        } else {
            $.cvError(desc+"失败")
        }
    });
}

/**
 * 渲染回复用户的名字
 */
function renderReplayUserName(userName,userId) {
    try {
        let replayUser = '[**!!#7D8B99 @'+userName+'!!**](/profile/'+userId+') ';
        $.cvSetEditorContent(replayUser);
        $("#reply_to_user").val(userId);
        //滚动到锚点位置
        $('html,body').animate({scrollTop: $(".btn-cv-comment").offset().top - 240}, 800);
    } catch (e) {
        window.location.href = '/login';
    }
}
