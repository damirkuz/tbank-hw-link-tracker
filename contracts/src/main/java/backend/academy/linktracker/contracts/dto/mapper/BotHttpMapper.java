package backend.academy.linktracker.contracts.dto.mapper;

import backend.academy.linktracker.bot.generated.dto.LinkUpdate;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;

public final class BotHttpMapper {

    private BotHttpMapper() {}

    public static LinkUpdate toLinkUpdate(CommonLinkUpdate commonLinkUpdate) {
        LinkUpdate linkUpdate = new LinkUpdate();
        linkUpdate.setId(commonLinkUpdate.id());
        linkUpdate.setUrl(commonLinkUpdate.url());
        linkUpdate.setDescription(commonLinkUpdate.description());
        linkUpdate.setTgChatIds(commonLinkUpdate.tgChatIds());
        return linkUpdate;
    }

    public static CommonLinkUpdate fromLinkUpdate(LinkUpdate linkUpdate) {
        return new CommonLinkUpdate(
                linkUpdate.getId(), linkUpdate.getUrl(), linkUpdate.getDescription(), linkUpdate.getTgChatIds());
    }
}
