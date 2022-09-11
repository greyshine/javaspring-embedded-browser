package de.greyshine.appbrowser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
@RestController
public class BrowserAdaptor {

    private final String uuid = UUID.randomUUID().toString();

    private final String[] commandLineArgs;
    private final String url = "http://localhost:8080";

    private final int pingTimeoutSecs = 1;

    private final AtomicLong pingCount = new AtomicLong(0);
    private LocalDateTime lastPing = null;

    private final ApplicationContext applicationContext;

    public BrowserAdaptor(ApplicationContext applicationContext, @Value("${cmd.browser}") String commandLine) {

        Assert.isTrue(commandLine != null && !commandLine.isEmpty(), " cmd.browser must be set");

        this.applicationContext = applicationContext;

        log.info("commandline: {}", commandLine);

        final List<String> args = getSplits(commandLine).stream()
                .peek(item -> log.debug("P {}", item))
                .collect(Collectors.toList())
                .stream()
                .map(item -> item.replaceAll("\\\\ ", " "))
                .map(item -> item.replaceAll("\\$address", this.url))
                .collect(Collectors.toList());

        commandLineArgs = args.toArray(new String[args.size()]);
    }

    private static List<String> getSplits(String s) {

        final List<String> splits = new ArrayList<>(0);

        if (s == null) {
            return splits;
        }

        var start = 0;

        for (int i = 0, l = s.length(); i < l; i++) {

            if (String.valueOf(s.charAt(i)).matches("\s")) {

                var extract = s.substring(start, i);

                if (s.charAt(i) == ' ' && i > 0 && s.charAt(i - 1) == '\\') {
                    continue;
                }

                if (!extract.isBlank()) {
                    splits.add(extract);
                }

                start = i + 1;
            }
        }

        if (start < s.length()) {

            final String value = s.substring(start);
            if (!value.isBlank()) {
                splits.add(value);
            }
        }

        return splits;
    }

    public void start() throws IOException {

        log.info("browser cmd ({} args): {}", commandLineArgs.length, commandLineArgs);

        Runtime.getRuntime().exec(commandLineArgs);

        log.info("started Browser");
    }

    @Scheduled(fixedRate = 3000)
    public void checkAlive() {

        if ( lastPing == null ) { return; }

        final var now = LocalDateTime.now();
        final boolean isInTime = pingCount.get() == 0 || this.lastPing.plusSeconds(pingTimeoutSecs).isAfter( now );

        log.debug( "last Ping: {}", this.lastPing.format(DateTimeFormatter.ISO_LOCAL_TIME) );
        log.debug( "last Ping +: {}", this.lastPing.plusSeconds(pingTimeoutSecs).format(DateTimeFormatter.ISO_LOCAL_TIME) );
        log.debug( "now: {}", now.format( DateTimeFormatter.ISO_LOCAL_TIME ) );
        log.debug( "in time: {} (ping-count={})", isInTime, pingCount );

        if (!isInTime) {
            log.warn("timeout by alive-pings. shutting down...");
            SpringApplication.exit( applicationContext, ()->0 );
        }
    }

    @GetMapping("/api/alive/{uuid}")
    public PingResponse getAlive(@PathVariable(required = false) String uuid) {

        pingCount.incrementAndGet();
        final var isValid = this.uuid.equals(uuid) || pingCount.get() == 1;

        if (!isValid) {

            log.warn("[{}] unmatched uuid [needed={}, received={}]", pingCount.get(), this.uuid, uuid);

        } else {

            lastPing = LocalDateTime.now();
        }

        return new PingResponse(isValid);
    }

    @Data
    public class PingResponse {
        final String uuid = BrowserAdaptor.this.uuid;
        final String time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        final long count = pingCount.addAndGet(1);
        final boolean valid;

        private PingResponse(boolean isValid) {
            this.valid = isValid;
        }
    }

}
