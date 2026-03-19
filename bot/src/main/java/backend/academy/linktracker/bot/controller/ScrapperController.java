package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.generated.api.UpdatesApi;
import backend.academy.linktracker.bot.generated.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.contracts.dto.mapper.BotHttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrapperController implements UpdatesApi {

    private final LinkUpdateService linkUpdateService;

    @Override
    public ResponseEntity<Void> updatesPost(LinkUpdate linkUpdate) {
        linkUpdateService.handleLinkUpdate(BotHttpMapper.fromLinkUpdate(linkUpdate));
        return ResponseEntity.ok().build();
    }
}
