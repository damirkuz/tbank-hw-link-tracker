package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.common.request.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrapperController {
    private final LinkUpdateService linkUpdateService;

    @PostMapping("/updates")
    public void getUpdate(@RequestBody LinkUpdate linkUpdate) {
        linkUpdateService.handleLinkUpdate(linkUpdate);
    }
}
