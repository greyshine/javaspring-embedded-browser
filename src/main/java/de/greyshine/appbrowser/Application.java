package de.greyshine.appbrowser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class Application implements ApplicationListener<ContextRefreshedEvent> {

    private BrowserAdaptor browserAdaptor;

    public Application(BrowserAdaptor browserAdaptor) {
        this.browserAdaptor = browserAdaptor;
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        browserAdaptor.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
