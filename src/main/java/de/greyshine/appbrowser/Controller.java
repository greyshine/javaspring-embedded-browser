package de.greyshine.appbrowser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class Controller {

    private final BrowserAdaptor browserAdaptor;

    private final AtomicInteger indexCalls = new AtomicInteger(0);
    private final String html;

    public Controller(BrowserAdaptor browserAdaptor) throws IOException {

        this.browserAdaptor = browserAdaptor;

        final URL url = Controller.class.getClassLoader().getResource( "public/index.html" );
        Assert.notNull(url, "cannot find public/index.html");

        try (InputStream is = url.openStream()) {
            html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final int call = indexCalls.incrementAndGet();

        log.info("call '/' no.: {} ({})", call, request.getRemoteAddr());

        if ( call > 1 ) {

            log.warn("preceeding call: {}; will block.", call);
            response.sendError( HttpStatus.SERVICE_UNAVAILABLE.value(), "Disallowed request." );
            return null;
        }

        return this.html.replaceAll("\\{\\{uuid}}", browserAdaptor.getUuid() );
    }
}
