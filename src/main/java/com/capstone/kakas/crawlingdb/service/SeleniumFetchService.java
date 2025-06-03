package com.capstone.kakas.crawlingdb.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SeleniumFetchService {

    public Document fetchBySelenium(String url) {
        WebDriver driver = null;
        try {
            // Chrome 옵션 설정
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");  // 브라우저 창 없이 실행
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // 페이지 로드
            driver.get(url);

            // 다나와 사이트 특성상 가격 정보 로딩 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // 여러 조건으로 페이지 로딩 완료 확인
            try {
                // 가격 정보가 포함된 요소가 로드될 때까지 대기
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.className("prod_pricelist")),
                        ExpectedConditions.presenceOfElementLocated(By.className("price_list")),
                        ExpectedConditions.presenceOfElementLocated(By.id("productPriceList")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".price_lowest"))
                ));
            } catch (Exception e) {
                System.out.println("특정 요소 대기 실패, 일반 대기로 진행: " + e.getMessage());
                Thread.sleep(3000); // 3초 추가 대기
            }

            // 스크롤하여 동적 콘텐츠 로드 유도
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight/2);");
            Thread.sleep(1000);
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);

            // HTML 소스 가져오기
            String pageSource = driver.getPageSource();

            // 디버깅을 위한 로그 출력
            System.out.println("페이지 제목: " + driver.getTitle());
            System.out.println("페이지 소스 길이: " + pageSource.length());

            // 가격 관련 요소가 있는지 확인
            List<WebElement> priceElements = driver.findElements(By.cssSelector("[class*='price'], [class*='Price']"));
            System.out.println("찾은 가격 관련 요소 수: " + priceElements.size());

            return Jsoup.parse(pageSource);

        } catch (Exception e) {
            System.err.println("Selenium 페이지 로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("페이지 로드 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // 특정 요소가 로드될 때까지 대기하는 헬퍼 메서드
    public Document fetchBySeleniumWithCustomWait(String url, String waitSelector) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            driver = new ChromeDriver(options);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(waitSelector)));

            return Jsoup.parse(driver.getPageSource());

        } catch (Exception e) {
            System.err.println("커스텀 대기 Selenium 실패: " + e.getMessage());
            throw new RuntimeException("페이지 로드 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}