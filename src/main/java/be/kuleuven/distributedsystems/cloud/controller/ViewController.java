package be.kuleuven.distributedsystems.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/trains/*/*", "/trains/*/*/*", "/cart", "/account", "/manager", "/login"})
    public String spa() {
        return "forward:/";
    }

    @GetMapping("/_ah/warmup")
    public void warmup() {
    }
}
