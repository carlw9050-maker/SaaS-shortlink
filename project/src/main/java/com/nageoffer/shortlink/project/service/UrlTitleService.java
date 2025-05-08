package com.nageoffer.shortlink.project.service;

public interface UrlTitleService {
    /**
     * 根据URL获取标题
     * @param url 目标网址地址
     * @return 网站标题
     */
    String getTitleByUrl(String url);
}
