package backend.academy.linktracker.contracts.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.scrapper.generated.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.generated.dto.LinkResponse;
import backend.academy.linktracker.scrapper.generated.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.generated.dto.RemoveLinkRequest;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScrapperHttpMapper")
class ScrapperHttpMapperTest {

    private static final URI URI_LINK = URI.create("https://github.com/user/repo");

    // --- AddLinkRequest ---

    @Test
    @DisplayName("toAddLinkRequest — маппит uri, tags, filters")
    void toAddLinkRequest() {
        CommonAddLinkRequest common = new CommonAddLinkRequest(URI_LINK, List.of("java"), List.of("filter1"));

        AddLinkRequest result = ScrapperHttpMapper.toAddLinkRequest(common);

        assertThat(result.getLink()).isEqualTo(URI_LINK);
        assertThat(result.getTags()).containsExactly("java");
        assertThat(result.getFilters()).containsExactly("filter1");
    }

    @Test
    @DisplayName("fromAddLinkRequest — маппит обратно в CommonAddLinkRequest")
    void fromAddLinkRequest() {
        AddLinkRequest req = new AddLinkRequest();
        req.setLink(URI_LINK);
        req.setTags(List.of("java"));
        req.setFilters(List.of("filter1"));

        CommonAddLinkRequest result = ScrapperHttpMapper.fromAddLinkRequest(req);

        assertThat(result.uri()).isEqualTo(URI_LINK);
        assertThat(result.tags()).containsExactly("java");
        assertThat(result.filters()).containsExactly("filter1");
    }

    @Test
    @DisplayName("toAddLinkRequest → fromAddLinkRequest — roundtrip")
    void addLinkRequestRoundtrip() {
        CommonAddLinkRequest original = new CommonAddLinkRequest(URI_LINK, List.of("t1", "t2"), List.of());

        CommonAddLinkRequest result =
                ScrapperHttpMapper.fromAddLinkRequest(ScrapperHttpMapper.toAddLinkRequest(original));

        assertThat(result).isEqualTo(original);
    }

    // --- RemoveLinkRequest ---

    @Test
    @DisplayName("toRemoveLinkRequest — маппит uri")
    void toRemoveLinkRequest() {
        CommonRemoveLinkRequest common = new CommonRemoveLinkRequest(URI_LINK);

        RemoveLinkRequest result = ScrapperHttpMapper.toRemoveLinkRequest(common);

        assertThat(result.getLink()).isEqualTo(URI_LINK);
    }

    @Test
    @DisplayName("fromRemoveLinkRequest — маппит обратно")
    void fromRemoveLinkRequest() {
        RemoveLinkRequest req = new RemoveLinkRequest();
        req.setLink(URI_LINK);

        CommonRemoveLinkRequest result = ScrapperHttpMapper.fromRemoveLinkRequest(req);

        assertThat(result.uri()).isEqualTo(URI_LINK);
    }

    @Test
    @DisplayName("toRemoveLinkRequest → fromRemoveLinkRequest — roundtrip")
    void removeLinkRequestRoundtrip() {
        CommonRemoveLinkRequest original = new CommonRemoveLinkRequest(URI_LINK);

        CommonRemoveLinkRequest result =
                ScrapperHttpMapper.fromRemoveLinkRequest(ScrapperHttpMapper.toRemoveLinkRequest(original));

        assertThat(result).isEqualTo(original);
    }

    // --- LinkResponse ---

    @Test
    @DisplayName("toLinkResponse — маппит id, url, tags, filters")
    void toLinkResponse() {
        CommonLinkResponse common = new CommonLinkResponse(1L, URI_LINK, List.of("java"), List.of("filter1"));

        LinkResponse result = ScrapperHttpMapper.toLinkResponse(common);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUrl()).isEqualTo(URI_LINK);
        assertThat(result.getTags()).containsExactly("java");
        assertThat(result.getFilters()).containsExactly("filter1");
    }

    @Test
    @DisplayName("fromLinkResponse — маппит обратно")
    void fromLinkResponse() {
        LinkResponse resp = new LinkResponse();
        resp.setId(1L);
        resp.setUrl(URI_LINK);
        resp.setTags(List.of("java"));
        resp.setFilters(List.of("filter1"));

        CommonLinkResponse result = ScrapperHttpMapper.fromLinkResponse(resp);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.url()).isEqualTo(URI_LINK);
        assertThat(result.tags()).containsExactly("java");
    }

    @Test
    @DisplayName("toLinkResponse → fromLinkResponse — roundtrip")
    void linkResponseRoundtrip() {
        CommonLinkResponse original = new CommonLinkResponse(42L, URI_LINK, List.of("t1"), List.of("f1"));

        CommonLinkResponse result = ScrapperHttpMapper.fromLinkResponse(ScrapperHttpMapper.toLinkResponse(original));

        assertThat(result).isEqualTo(original);
    }

    // --- ListLinksResponse ---

    @Test
    @DisplayName("toListLinksResponse — маппит список и size")
    void toListLinksResponse() {
        CommonLinkResponse link = new CommonLinkResponse(1L, URI_LINK, List.of("java"), List.of());
        CommonListLinksResponse common = new CommonListLinksResponse(List.of(link), 1);

        ListLinksResponse result = ScrapperHttpMapper.toListLinksResponse(common);

        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getLinks()).hasSize(1);
        assertThat(result.getLinks().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("fromListLinksResponse — маппит обратно")
    void fromListLinksResponse() {
        LinkResponse lr = new LinkResponse();
        lr.setId(5L);
        lr.setUrl(URI_LINK);
        lr.setTags(List.of());
        lr.setFilters(List.of());
        ListLinksResponse resp = new ListLinksResponse();
        resp.setLinks(List.of(lr));
        resp.setSize(1);

        CommonListLinksResponse result = ScrapperHttpMapper.fromListLinksResponse(resp);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.links()).hasSize(1);
        assertThat(result.links().get(0).id()).isEqualTo(5L);
    }

    @Test
    @DisplayName("toListLinksResponse → fromListLinksResponse — roundtrip")
    void listLinksResponseRoundtrip() {
        CommonLinkResponse link = new CommonLinkResponse(7L, URI_LINK, List.of("x"), List.of("y"));
        CommonListLinksResponse original = new CommonListLinksResponse(List.of(link), 1);

        CommonListLinksResponse result =
                ScrapperHttpMapper.fromListLinksResponse(ScrapperHttpMapper.toListLinksResponse(original));

        assertThat(result.size()).isEqualTo(original.size());
        assertThat(result.links()).isEqualTo(original.links());
    }

    @Test
    @DisplayName("toListLinksResponse — пустой список")
    void toListLinksResponseEmpty() {
        CommonListLinksResponse common = new CommonListLinksResponse(List.of(), 0);

        ListLinksResponse result = ScrapperHttpMapper.toListLinksResponse(common);

        assertThat(result.getSize()).isZero();
        assertThat(result.getLinks()).isEmpty();
    }
}
