export default class Link extends SyntaxBase {
    constructor({ config, globalConfig }: {
        config: any;
        globalConfig: any;
    });
    urlProcessor: any;
    openNewPage: any;
    /**
     *
     * @param {string} match 匹配的完整字符串
     * @param {string} leadingChar 正则分组一：前置字符
     * @param {string} text 正则分组二：链接文字
     * @param {string|undefined} link 正则分组三：链接URL
     * @param {string|undefined} title 正则分组四：链接title
     * @param {string|undefined} ref 正则分组五：链接引用
     * @param {string|undefined} target 正则分组六：新窗口打开
     * @returns
     */
    toHtml(match: string, leadingChar: string, text: string, link: string | undefined, title: string | undefined, ref: string | undefined, target: string | undefined): string;
    toStdMarkdown(match: any): any;
}
import SyntaxBase from "@/core/SyntaxBase";
