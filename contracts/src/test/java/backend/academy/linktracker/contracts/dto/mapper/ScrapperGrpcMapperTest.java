package backend.academy.linktracker.contracts.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcChatRequest;
import backend.academy.linktracker.generated.grpc.GrpcDeleteLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcListLinksResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScrapperGrpcMapper")
class ScrapperGrpcMapperTest {

    private static final URI URI_LINK = URI.create("https://github.com/user/repo");

    @Test
    @DisplayName("toGrpcChatRequest — устанавливает chatId")
    void toGrpcChatRequest() {
        GrpcChatRequest result = ScrapperGrpcMapper.toGrpcChatRequest(42L);
        assertThat(result.getChatId()).isEqualTo(42L);
    }

    // --- AddLinkCommand ---

    @Test
    @DisplayName("toGrpcAddLinkCommand — содержит chatId и request с uri/tags/filters")
    void toGrpcAddLinkCommand() {
        CommonAddLinkRequest req = new CommonAddLinkRequest(URI_LINK, List.of("java"), List.of("f1"));

        GrpcAddLinkCommand result = ScrapperGrpcMapper.toGrpcAddLinkCommand(1L, req);

        assertThat(result.getChatId()).isEqualTo(1L);
        assertThat(result.getRequest().getUri()).isEqualTo(URI_LINK.toString());
        assertThat(result.getRequest().getTagsList()).containsExactly("java");
        assertThat(result.getRequest().getFiltersList()).containsExactly("f1");
    }

    @Test
    @DisplayName("fromGrpcAddLinkCommand → roundtrip")
    void addLinkCommandRoundtrip() {
        CommonAddLinkRequest original = new CommonAddLinkRequest(URI_LINK, List.of("t1"), List.of("f1"));

        CommonAddLinkRequest result =
                ScrapperGrpcMapper.fromGrpcAddLinkCommand(ScrapperGrpcMapper.toGrpcAddLinkCommand(10L, original));

        assertThat(result).isEqualTo(original);
    }

    // --- DeleteLinkCommand ---

    @Test
    @DisplayName("toGrpcDeleteLinkCommand — содержит chatId и uri")
    void toGrpcDeleteLinkCommand() {
        CommonRemoveLinkRequest req = new CommonRemoveLinkRequest(URI_LINK);

        GrpcDeleteLinkCommand result = ScrapperGrpcMapper.toGrpcDeleteLinkCommand(5L, req);

        assertThat(result.getChatId()).isEqualTo(5L);
        assertThat(result.getRequest().getUri()).isEqualTo(URI_LINK.toString());
    }

    @Test
    @DisplayName("fromGrpcDeleteLinkCommand → roundtrip")
    void deleteLinkCommandRoundtrip() {
        CommonRemoveLinkRequest original = new CommonRemoveLinkRequest(URI_LINK);

        CommonRemoveLinkRequest result =
                ScrapperGrpcMapper.fromGrpcDeleteLinkCommand(ScrapperGrpcMapper.toGrpcDeleteLinkCommand(5L, original));

        assertThat(result).isEqualTo(original);
    }

    // --- LinkResponse ---

    @Test
    @DisplayName("toGrpcLinkResponse → fromGrpcLinkResponse — roundtrip")
    void linkResponseRoundtrip() {
        CommonLinkResponse original = new CommonLinkResponse(3L, URI_LINK, List.of("java"), List.of("filter"));

        CommonLinkResponse result =
                ScrapperGrpcMapper.fromGrpcLinkResponse(ScrapperGrpcMapper.toGrpcLinkResponse(original));

        assertThat(result).isEqualTo(original);
    }

    // --- ListLinksResponse ---

    @Test
    @DisplayName("toGrpcListLinksResponse → fromGrpcListLinksResponse — roundtrip")
    void listLinksResponseRoundtrip() {
        CommonLinkResponse link = new CommonLinkResponse(1L, URI_LINK, List.of("t"), List.of("f"));
        CommonListLinksResponse original = new CommonListLinksResponse(List.of(link), 1);

        CommonListLinksResponse result =
                ScrapperGrpcMapper.fromGrpcListLinksResponse(ScrapperGrpcMapper.toGrpcListLinksResponse(original));

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.links()).isEqualTo(original.links());
    }

    @Test
    @DisplayName("toGrpcListLinksResponse — null links не падает")
    void toGrpcListLinksResponseNullLinks() {
        CommonListLinksResponse common = new CommonListLinksResponse(null, 0);

        GrpcListLinksResponse result = ScrapperGrpcMapper.toGrpcListLinksResponse(common);

        assertThat(result.getSize()).isZero();
        assertThat(result.getLinksList()).isEmpty();
    }

    @Test
    @DisplayName("toGrpcListLinksResponse — пустой список")
    void toGrpcListLinksResponseEmpty() {
        CommonListLinksResponse common = new CommonListLinksResponse(List.of(), 0);

        GrpcListLinksResponse result = ScrapperGrpcMapper.toGrpcListLinksResponse(common);

        assertThat(result.getLinksList()).isEmpty();
    }
}
