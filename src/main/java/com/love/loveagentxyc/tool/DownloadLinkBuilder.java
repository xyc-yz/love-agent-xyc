package com.love.loveagentxyc.tool;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 生成下载路径：相对路径用于服务端路由；对外返回「完整 URL」，避免被模型误写成 https://api/... 这类无效主机名。
 */
public final class DownloadLinkBuilder {

    private static final String PREFIX = "/api/files/download";

    /** 浏览器可访问的后端根地址，不含末尾斜杠，例如 http://localhost:8123 */
    private static volatile String publicOrigin = "http://localhost:8123";

    private DownloadLinkBuilder() {
    }

    public static void setPublicOrigin(String origin) {
        if (origin != null && !origin.isBlank()) {
            publicOrigin = origin.trim().replaceAll("/+$", "");
        }
    }

    public static String getPublicOrigin() {
        return publicOrigin;
    }

    /**
     * 站点相对路径，仅供内部使用。
     */
    public static String browserDownloadUrl(String category, String fileName) {
        String enc = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return PREFIX + "/" + category + "/" + enc;
    }

    /**
     * 完整 URL，可复制到浏览器地址栏下载。
     */
    public static String absoluteDownloadUrl(String category, String fileName) {
        return publicOrigin + browserDownloadUrl(category, fileName);
    }

    /**
     * 工具返回给模型/用户的单行说明（仅含完整 URL，勿再改写主机名）。
     */
    public static String hintLine(String category, String fileName) {
        return "下载URL（整行复制到浏览器打开）：\n" + absoluteDownloadUrl(category, fileName);
    }
}
