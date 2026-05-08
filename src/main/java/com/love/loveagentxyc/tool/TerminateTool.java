package com.love.loveagentxyc.tool;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具（作用是让自主规划智能体能够合理地中断）
 */
public class TerminateTool {

    @Tool(description = """
            仅在**全部满足**以下条件时，才可调用此工具：
            1. 已通过具体结果**完整完成**用户的请求
            2. 已提供所有要求的信息/文件/操作
            3. 无待处理的后续任务
            
            以下情况**禁止**调用此工具：
            - 仅提供常规建议或日常对话（直接回复即可）
            - 存在疑问或需要补充更多信息（需先询问用户）
            - 仅执行单一步骤且未完成验证
            
            正确使用示例：
            ✓ 成功生成并保存PDF文件后
            ✓ 完成多步骤搜索并提供完整结果后
            ✓ 用户明确表示“就这些”“谢谢，完成了”时
            
            错误使用示例：
            ✗ 提供情感建议后（直接继续对话即可）
            ✗ 未使用任何工具验证信息时
            ✗ 用户可能存在后续问题时
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
