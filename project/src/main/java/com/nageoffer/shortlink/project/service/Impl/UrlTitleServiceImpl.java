package com.nageoffer.shortlink.project.service.Impl;

import com.nageoffer.shortlink.project.service.UrlTitleService;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * URL 标题接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    @SneakyThrows
    @Override
    public String getTitleByUrl(String url){

        URL targetUrl = new URL(url);
        //创建一个java.net.URL对象，将传入的字符串URL转换为URL对象，可能会抛出MalformedURLException
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        //打开到目标URL的连接，返回一个URLConnection对象，并强制转换为HttpURLConnection类型
        connection.setRequestMethod("GET");  //设置HTTP请求方法为GET，表示我们要从服务器获取数据,因此不需要设置apifox请求吧
        connection.connect();  //实际建立与远程服务器的连接
        int responseCode = connection.getResponseCode(); //获取HTTP响应状态码，如200表示成功，404表示未找到等
        if(responseCode == HttpURLConnection.HTTP_OK){
            Document document = Jsoup.connect(url).get();
            return document.title();
        }
        //检查响应码是否为HTTP_OK(即200)，表示请求成功
        //Jsoup.connect(url) - 创建一个新的Jsoup连接，使用Jsoup库连接到给定的URL并获取HTML文档，.get() - 执行GET请求并返回解析后的HTML文档
        //从解析的HTML文档中提取<title>标签的内容并返回
        return "Error while fetching title";
        //如果响应码不是200(HTTP_OK)，则返回错误信息字符串
    }
}
