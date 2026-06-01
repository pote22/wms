import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import OpenAI from "openai";
import { z } from "zod";

const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
if (!OPENAI_API_KEY) {
  console.error("[openai-mcp] ERROR: OPENAI_API_KEY environment variable is not set");
  process.exit(1);
}

const openai = new OpenAI({ apiKey: OPENAI_API_KEY });

async function chat(system, user) {
  const res = await openai.chat.completions.create({
    model: "gpt-4o",
    messages: [{ role: "system", content: system }, { role: "user", content: user }],
    temperature: 0.2,
  });
  return res.choices[0].message.content;
}

const server = new McpServer({ name: "openai-coding", version: "1.0.0" });

server.registerTool(
  "codex_generate",
  {
    description: "OpenAI에게 코드 생성을 요청합니다.",
    inputSchema: {
      task: z.string().describe("구현할 코딩 작업 설명"),
      language: z.string().optional().describe("프로그래밍 언어"),
      context: z.string().optional().describe("기존 코드 또는 관련 컨텍스트"),
    },
  },
  async ({ task, language, context }) => {
    const system = `You are an expert software engineer. Write clean, production-ready code.${language ? ` Use ${language}.` : ""} Return only code with minimal explanation unless asked.`;
    const user = context ? `Task: ${task}\n\nContext:\n${context}` : `Task: ${task}`;
    return { content: [{ type: "text", text: await chat(system, user) }] };
  }
);

server.registerTool(
  "codex_refactor",
  {
    description: "기존 코드를 리팩토링합니다.",
    inputSchema: {
      code: z.string().describe("리팩토링할 코드"),
      instructions: z.string().describe("리팩토링 지시사항"),
    },
  },
  async ({ code, instructions }) => {
    const system = "You are an expert software engineer specializing in code refactoring. Improve code quality while preserving functionality.";
    const user = `Instructions: ${instructions}\n\nCode:\n\`\`\`\n${code}\n\`\`\``;
    return { content: [{ type: "text", text: await chat(system, user) }] };
  }
);

server.registerTool(
  "codex_fix_bug",
  {
    description: "코드의 버그를 분석하고 수정합니다.",
    inputSchema: {
      code: z.string().describe("버그가 있는 코드"),
      error: z.string().optional().describe("에러 메시지 또는 버그 증상"),
    },
  },
  async ({ code, error }) => {
    const system = "You are an expert debugger. Identify and fix bugs. Explain what was wrong and provide the fixed version.";
    const user = error ? `Error: ${error}\n\nCode:\n\`\`\`\n${code}\n\`\`\`` : `Fix bugs in:\n\`\`\`\n${code}\n\`\`\``;
    return { content: [{ type: "text", text: await chat(system, user) }] };
  }
);

server.registerTool(
  "codex_write_tests",
  {
    description: "주어진 코드에 대한 테스트 코드를 생성합니다.",
    inputSchema: {
      code: z.string().describe("테스트할 대상 코드"),
      framework: z.string().optional().describe("테스트 프레임워크"),
    },
  },
  async ({ code, framework }) => {
    const system = `You are an expert in test-driven development. Write comprehensive tests.${framework ? ` Use ${framework}.` : ""}`;
    const user = `Write tests for:\n\`\`\`\n${code}\n\`\`\``;
    return { content: [{ type: "text", text: await chat(system, user) }] };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
console.error("[openai-mcp] Server started successfully");
