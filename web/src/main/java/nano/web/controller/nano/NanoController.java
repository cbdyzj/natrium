package nano.web.controller.nano;

import nano.support.Result;
import nano.support.Zx;
import nano.support.validation.Validated;
import nano.web.messageing.Exchanges;
import nano.web.nano.NanoService;
import nano.web.security.Authorized;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static nano.web.security.Privilege.NANO_API;

@CrossOrigin
@RestController
@RequestMapping("/api/nano")
public class NanoController {

    private final RabbitMessagingTemplate messagingTemplate;

    private final NanoService nanoService;

    public NanoController(RabbitMessagingTemplate messagingTemplate, NanoService nanoService) {
        this.messagingTemplate = messagingTemplate;
        this.nanoService = nanoService;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong\n");
    }

    @GetMapping("/system")
    public ResponseEntity<?> system() {
        var nano = this.nanoService.system();
        return ResponseEntity.ok(nano);
    }

    @GetMapping("/beans")
    public ResponseEntity<?> beans(@RequestParam(name = "q", required = false) String q) {
        var beans = this.nanoService.getBeanDefinitionNames(q);
        return ResponseEntity.ok(beans);
    }

    @Validated(NanoMessageValidator.class)
    @PostMapping("/message")
    public ResponseEntity<?> message(@RequestParam("m") String m) {
        this.messagingTemplate.convertAndSend(Exchanges.NANO, "nano", m);
        return ResponseEntity.ok(Result.of("OK"));
    }

    @Authorized(privilege = NANO_API)
    @PostMapping("/$")
    public ResponseEntity<?> $(@RequestBody String[] command) {
        return ResponseEntity.ok(new String(Zx.$(command).join(), StandardCharsets.UTF_8));
    }

    @Authorized(privilege = NANO_API)
    @GetMapping("/screenshot")
    public ResponseEntity<?> screenshot(@RequestParam("url") String url) {
        var NODE = "./client/.gradle/nodejs/node*/bin/node";
        var SCREENSHOT_JS = "./client/scripts/screenshot.js";
        var command = List.of("bash", "-c", "%s %s %s".formatted(NODE, SCREENSHOT_JS, url)).toArray(String[]::new);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Zx.$(command).join());
    }
}
