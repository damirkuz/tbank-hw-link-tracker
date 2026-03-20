package backend.academy.linktracker.scrapper.controller.http;

import backend.academy.linktracker.contracts.dto.mapper.ScrapperHttpMapper;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.scrapper.generated.api.LinksApi;
import backend.academy.linktracker.scrapper.generated.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.generated.dto.LinkResponse;
import backend.academy.linktracker.scrapper.generated.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.generated.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.BotLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BotLinkController implements LinksApi {

    private final BotLinkService botLinkService;

    @Override
    public ResponseEntity<@NotNull LinkResponse> linksPost(Long tgChatId, AddLinkRequest addLinkRequest) {
        log.atInfo().addKeyValue("chat_id", tgChatId).log("Запрос на добавление ссылки");

        CommonAddLinkRequest commonAddLinkRequest = ScrapperHttpMapper.fromAddLinkRequest(addLinkRequest);

        return ResponseEntity.ok(
                ScrapperHttpMapper.toLinkResponse(botLinkService.addLink(tgChatId, commonAddLinkRequest)));
    }

    @Override
    public ResponseEntity<@NotNull LinkResponse> linksDelete(Long tgChatId, RemoveLinkRequest removeLinkRequest) {
        log.atInfo().addKeyValue("chat_id", tgChatId).log("Запрос на удаление ссылки");

        CommonRemoveLinkRequest commonRemoveLinkRequest = ScrapperHttpMapper.fromRemoveLinkRequest(removeLinkRequest);

        return ResponseEntity.ok(
                ScrapperHttpMapper.toLinkResponse(botLinkService.deleteLink(tgChatId, commonRemoveLinkRequest)));
    }

    @Override
    public ResponseEntity<@NotNull ListLinksResponse> linksGet(Long tgChatId) {
        log.atInfo().addKeyValue("chat_id", tgChatId).log("Запрос на получение списка ссылок");

        return ResponseEntity.ok(ScrapperHttpMapper.toListLinksResponse(botLinkService.getLinks(tgChatId)));
    }
}
