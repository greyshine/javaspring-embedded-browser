package de.greyshine.appbrowser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {

        final Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        log.warn("error; status={}", status);

        return "error.html";
    }
}
