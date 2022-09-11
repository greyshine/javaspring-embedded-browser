package de.greyshine.appbrowser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
public class Controller {

    final String html;

    public Controller() throws IOException {

        final URL url = Controller.class.getClassLoader().getResource( "public/index.html" );

        Assert.notNull(url, "cannot find public/index.html");

        try (InputStream is = url.openStream()) {
            html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/")
    public String index() {
        return html;
    }


}
