package com.yupi.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.yupi.yuaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 资源下载工具
 * 已修复：SSRF 防护（禁止内网地址）、路径遍历防护、文件大小限制
 */
public class ResourceDownloadTool {

    /** 最大下载文件大小：50MB */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /** 禁止访问的内网 IP 前缀（SSRF 防护） */
    private static final Set<String> BLOCKED_IP_PREFIXES = Set.of(
            "127.", "10.", "192.168.", "0.",
            "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.",
            "172.24.", "172.25.", "172.26.", "172.27.",
            "172.28.", "172.29.", "172.30.", "172.31.",
            "169.254.", "::1", "0.0.0.0"
    );

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {

        // SSRF 防护：校验 URL 不指向内网
        if (!isUrlSafe(url)) {
            return "Error: URL points to internal/reserved network address, blocked for security";
        }

        // 路径遍历防护
        String safePath = sanitizeFileName(fileName);
        if (safePath == null) {
            return "Error: Invalid file name - path traversal detected";
        }

        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        try {
            FileUtil.mkdir(fileDir);
            File targetFile = new File(safePath);
            // 下载前检查目标文件大小限制
            HttpUtil.downloadFile(url, targetFile);
            // 下载后验证文件大小
            if (targetFile.length() > MAX_FILE_SIZE) {
                targetFile.delete();
                return "Error: Downloaded file exceeds maximum size limit (50MB), file deleted";
            }
            return "Resource downloaded successfully to: " + safePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }

    /**
     * SSRF 防护：解析 URL 的主机地址，禁止访问内网/保留地址
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

            // 禁止内网地址
            for (String prefix : BLOCKED_IP_PREFIXES) {
                if (ip.startsWith(prefix)) {
                    return false;
                }
            }
            // 禁止回环地址
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 文件名安全校验：防止路径遍历攻击
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")
                || fileName.startsWith("~") || fileName.startsWith(".")) {
            return null;
        }
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        Path baseDir = Paths.get(fileDir).toAbsolutePath().normalize();
        Path resolved = baseDir.resolve(fileName).normalize();
        if (!resolved.startsWith(baseDir)) {
            return null;
        }
        return resolved.toString();
    }
}
