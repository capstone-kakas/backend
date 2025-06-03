package com.capstone.kakas.global.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @Bean(destroyMethod = "quit")
    public WebDriver chromeDriver() {
        // "136" 주 버전만 지정하면, WDM이 맞는 세부 버전(예: 136.xxxx) 드라이버를 찾아 내려받습니다.
        WebDriverManager.chromedriver()
                .browserVersion("136")
                .setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        );
        return new ChromeDriver(options);
    }
}