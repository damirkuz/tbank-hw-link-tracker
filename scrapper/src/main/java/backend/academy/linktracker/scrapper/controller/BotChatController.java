package backend.academy.linktracker.scrapper.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
    @PostMapping("/tg-chat/{?}")
    public void addChat(@RequestParam long chatId) {}
}
