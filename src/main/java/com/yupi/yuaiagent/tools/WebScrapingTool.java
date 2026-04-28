package com.yupi.yuaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.InetAddress;
import java.net.URI;
import java.util.Set;

/**
 * 网页抓取工具
 * 已修复：SSRF 防护、超时控制、精简 HTML 输出（提取文本而非全量 HTML）
 */
public class WebScrapingTool {

    private static final int TIMEOUT_MS = 15000;

    /** 禁止访问的内网 IP 前缀（SSRF 防护） */
    private static final Set<String> BLOCKED_IP_PREFIXES = Set.of(
            "127.", "10.", "192.168.", "0.",
            "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.",
            "172.24.", "172.25.", "172.26.", "172.27.",
            "172.28.", "172.29.", "172.30.", "172.31.",
            "169.254.", "::1", "0.0.0.0"
    );

    @Tool(description = "Scrape the content of a web page and return cleaned text")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        // SSRF 防护
        if (!isUrlSafe(url)) {
            return "Error: URL points to internal/reserved network address, blocked for security";
        }
        try {
            Document document = Jsoup.connect(url)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .maxBodySize(2 * 1024 * 1024) // 限制 2MB
                    .get();
            // 精简输出：提取文本内容而非完整 HTML，节省 Token
            String text = document.text();
            // 如果文本过长，截断保留核心内容
            if (text.length() > 8000) {
                return text.substring(0, 8000) + "\n... [content truncated, total length: " + text.length() + "]";
            }
            return text;
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }

    /**
     * SSRF 防护：解析 URL 主机地址，禁止内网访问
     */
    private boolean isUrlSafe(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            InetAddress address = InetAddress.getByName(host);
            String ip = address.getHostAddress();
            for (String prefix : BLOCKED_IP_PREFIXES) {
                if (ip.startsWith(prefix)) {
                    return false;
                }
            }
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
