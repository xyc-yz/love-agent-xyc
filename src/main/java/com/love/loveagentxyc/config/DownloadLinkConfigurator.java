package com.love.loveagentxyc.config;

import com.love.loveagentxyc.tool.DownloadLinkBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 将可配置的公网/本机访问域名交给 {@link DownloadLinkBuilder}，工具返回完整下载 URL。
 */
@Component
public class DownloadLinkConfigurator {

    @Value("${app.download-base-url:http://localhost:8123}")
    private String downloadBaseUrl;

    @PostConstruct
    void apply() {
        DownloadLinkBuilder.setPublicOrigin(downloadBaseUrl);
    }
}
