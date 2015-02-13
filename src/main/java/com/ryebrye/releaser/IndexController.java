package com.ryebrye.releaser;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Ryan Gardner
 * @date 1/20/15
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    @RequestMapping("/app/**")
    public String app() {
        return "index.html";
    }
}
