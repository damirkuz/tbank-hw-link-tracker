package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.BotChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotChatController {

    // можно добавлять все ссылки в репозиторий, постоянно проходиться по ним.
    // если есть изменения, то смотреть у кого привязана данная ссылка и отправлять им
    // получается у каждой ссылки в linkRepository должен быть массив пользователей, у которых она привязана
    // тогда есть проблема, запрос addChat по факту ничего не будет делать
    // здесь можно M-M как-то подцепить
    // однако это будет дорого алгоритмически
    // при этом есть проблема с тегами, которые каждый пользователь задаёт индивидуально на ссылку

    // повтор проверки обновлений можно сделать через @Scheduled

    // получается рядом с ссылкой нужно хранить ещё и время её актуального апдейта

    private final BotChatService botChatService;

    @PostMapping("/tg-chat/{chatId}")
    public void addChat(@PathVariable long chatId) {
        botChatService.registerChat(chatId);
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public void deleteChat(@PathVariable long chatId) {
        botChatService.deleteChat(chatId);
    }
}
