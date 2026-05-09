package com.love.loveagentxyc.tool;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * PDF 生成工具
 */
public class PDFGenerationTool {

    @Tool(description = "没要求则不生成，根据指定内容生成 PDF。返回里「下载URL」开头的 http:// 整行地址可直接复制到浏览器下载，勿改写主机名或改成 https://api/", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "用于保存生成的 PDF 文件的文件名") String fileName,
            @ToolParam(description = "需纳入 PDF 文件的内容") String content) {
        String fileDir = System.getProperty("user.dir") + "/tmp" + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 依赖 font-asian（运行时 classpath）；优先 STSong-Light，与部分环境下 STSongStd-Light 注册名不一致有关
                PdfFont font = createChineseFont();
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            return "PDF已生成。\n" + DownloadLinkBuilder.hintLine("pdf", fileName);
        } catch (IOException e) {
            return "PDF 生成失败: " + e.getMessage();
        }
    }

    private static PdfFont createChineseFont() throws IOException {
        IOException last = null;
        for (String fontProgramName : new String[] {"STSong-Light", "STSongStd-Light"}) {
            try {
                return PdfFontFactory.createFont(fontProgramName, "UniGB-UCS2-H");
            } catch (IOException e) {
                last = e;
            }
        }
        throw last != null
                ? last
                : new IOException("无法加载中文字体，请确认依赖 com.itextpdf:font-asian 已加入运行时 classpath");
    }
}
