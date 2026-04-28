package com.yupi.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.yupi.yuaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类（提供文件读写功能）
 * 已修复：路径遍历漏洞（fileName 规范化校验）
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String safeFileName = sanitizeFileName(fileName);
        if (safeFileName == null) {
            return "Error: Invalid file name - path traversal detected";
        }
        try {
            return FileUtil.readUtf8String(safeFileName);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content
    ) {
        String safePath = sanitizeFileName(fileName);
        if (safePath == null) {
            return "Error: Invalid file name - path traversal detected";
        }
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, safePath);
            return "File written successfully to: " + safePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }

    /**
     * 文件名安全校验：防止路径遍历攻击（如 ../../etc/passwd）
     * 规范化路径后验证是否仍在允许的目录内
     *
     * @return 安全的绝对路径，或 null 表示不合法
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // 禁止路径分隔符和特殊前缀
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")
                || fileName.startsWith("~") || fileName.startsWith(".")) {
            return null;
        }
        Path baseDir = Paths.get(FILE_DIR).toAbsolutePath().normalize();
        Path resolved = baseDir.resolve(fileName).normalize();
        // 确保解析后的路径仍在基础目录内
        if (!resolved.startsWith(baseDir)) {
            return null;
        }
        return resolved.toString();
    }
}
