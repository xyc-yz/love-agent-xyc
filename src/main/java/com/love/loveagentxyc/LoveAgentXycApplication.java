package com.love.loveagentxyc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class LoveAgentXycApplication {
	public static void main(String[] args) {
        SpringApplication.run(LoveAgentXycApplication.class, args);
        log.info("LoveAgentXycApplication启动成功，这是一个伟大的开始");
	}

}
