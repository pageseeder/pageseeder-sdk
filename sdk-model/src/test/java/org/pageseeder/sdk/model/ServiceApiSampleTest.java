package org.pageseeder.sdk.model;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.pageseeder.sdk.client.PageSeederClient;
import org.pageseeder.sdk.model.codec.Decoders;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.exception.ServiceErrorException;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.MemberStatus;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.model.Version;
import org.pageseeder.sdk.service.PayloadFormat;
import org.pageseeder.sdk.service.ServiceCall;
import org.pageseeder.sdk.service.ServiceCatalog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Fixture-driven service API decoding tests.
 *
 * <p>Add new samples under {@code src/test/resources/api/[service-id]/[http_code]_[test_name].[extension]}
 * and register the corresponding service contract in {@link ServiceContracts}.
 */
public final class ServiceApiSampleTest {

  private static final Path API_FIXTURES_DIRECTORY = Paths.get("src/test/resources/api");

  private HttpServer server;
  private URI baseUri;

  static Stream<ApiSample> parameters() {
    return ApiSamples.load().stream();
  }

  @BeforeEach
  public void setUp() throws Exception {
    this.server = HttpServer.create(new InetSocketAddress(0), 0);
    this.server.start();
    this.baseUri = URI.create("http://localhost:" + this.server.getAddress().getPort());
  }

  @AfterEach
  public void tearDown() {
    this.server.stop(0);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("parameters")
  public void shouldDecodeRecordedServiceOutput(ApiSample sample) {
    ApiContract contract = ServiceContracts.contractFor(sample.serviceId());
    PageSeederClient client = PageSeederClient.builder()
        .apiOrigin(this.baseUri)
        .defaultFormat(PayloadFormat.XML)
        .build();

    ServiceCall call = contract.newCall(sample);
    String path = client.toRequest(call).uri().getPath();
    this.server.createContext(path, exchange ->
        reply(exchange, sample.statusCode(), sample.format().mediaType(), sample.readBody()));

    contract.verify(client, call, sample);
  }

  private static void reply(HttpExchange exchange, int statusCode, String mediaType, byte[] body) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", mediaType);
    exchange.sendResponseHeaders(statusCode, body.length);
    exchange.getResponseBody().write(body);
    exchange.close();
  }

  private static void assertMember(ApiSample sample, Member member) {
    switch (sample.testName()) {
      case "admin":
        assertEquals(123L, member.getId());
        assertEquals("vvega", member.getUsername());
        assertEquals("Vincent Vega", member.getFullname());
        assertEquals("vvega@example.org", member.getEmail());
        assertEquals(MemberStatus.ACTIVATED, member.getStatus());
        break;
      case "disabled":
        assertEquals(333L, member.getId());
        assertEquals("jwinnfield@example.org", member.getUsername());
        assertEquals("Jules Winnfield", member.getFullname());
        assertEquals(MemberStatus.DISABLED, member.getStatus());
        break;
      default:
        fail("No member assertions registered for sample " + sample);
    }
  }

  private static void assertComment(ApiSample sample, Comment comment) {
    switch (sample.testName()) {
      case "comment-group":
        assertEquals(25061L, comment.getId());
        assertEquals("findSimpleComment", comment.getTitle());
        assertEquals("unit-admin", comment.getAuthor().getMember().getUsername());
        assertEquals("Unit Test", comment.getAuthor().getFullname());
        assertEquals("unittest-comment-findcomment", comment.getContext().getGroup().getName());
        break;
      case "comment-uri":
        assertEquals(25062L, comment.getId());
        assertEquals("/ps/unittest/comment/findcomment/documents/finddoc.psml",
            comment.getContext().getUri().getPath());
        break;
      case "comment-type":
        assertEquals("mytype1", comment.getType());
        assertEquals("Comment", comment.getContentRole());
        break;
      case "comment-type-and-uri":
        assertEquals("mytype1", comment.getType());
        assertEquals("finddoc", comment.getContext().getUri().getTitle());
        break;
      case "comment-task":
        assertEquals("Open", comment.getStatus());
        assertEquals("High", comment.getPriority());
        assertEquals("unit-admin", comment.getAssignedTo().getUser().getMember().getUsername());
        assertEquals("unittest-comment-findcomment", comment.getContext().getGroup().getName());
        break;
      case "comment-task-uri":
        assertEquals("Open", comment.getStatus());
        assertEquals("finddoc", comment.getContext().getUri().getTitle());
        break;
      case "comment-task-type":
        assertEquals("mytype1", comment.getType());
        assertEquals("High", comment.getPriority());
        break;
      case "comment-task-type-uri":
        assertEquals("mytype1", comment.getType());
        assertEquals("Open", comment.getStatus());
        assertEquals("/ps/unittest/comment/findcomment/documents/finddoc.psml",
            comment.getContext().getUri().getPath());
        break;
      case "comment-type-xhtml":
        assertEquals("forum", comment.getType());
        assertEquals("File Attachment", comment.getContentRole());
        assertEquals("application/xhtml+xml", comment.getContent().get(0).getType());
        break;
      case "comment-attachments":
        assertEquals("File Attachment", comment.getContentRole());
        assertEquals(2, comment.getAttachments().size());
        assertEquals("/ps/unittest/comment/createcomment_18/doc1PublicUrl.psml",
            comment.getAttachments().get(0).getPath());
        break;
      case "comment-public1":
        assertEquals("Somebody", comment.getAuthor().getFullname());
        assertNull(comment.getAuthor().getMember());
        assertEquals("comment-test", comment.getContext().getGroup().getName());
        break;
      case "comment-nasty":
        assertEquals("華華華華華華華華華華華", comment.getAuthor().getFullname());
        assertNull(comment.getAuthor().getMember());
        assertEquals("unit-admin", comment.getModifiedBy().getUser().getMember().getUsername());
        assertEquals("http", comment.getContext().getUri().getScheme());
        break;
      default:
        fail("No comment assertions registered for sample " + sample);
    }
  }

  private static void assertVersion(ApiSample sample, Version version) {
    switch (sample.testName()) {
      case "version":
        assertEquals(6, version.getMajor());
        assertEquals(2007, version.getBuild());
        assertEquals("6.2007", version.getString());
        break;
      case "version-beta":
        assertEquals(6, version.getMajor());
        assertEquals(3000, version.getBuild());
        assertEquals("6.3000-beta-1", version.getString());
        break;
      default:
        fail("No version assertions registered for sample " + sample);
    }
  }

  private static void assertMembership(ApiSample sample, Membership membership) {
    switch (sample.testName()) {
      case "membership-group":
        assertEquals(102L, membership.getId());
        assertEquals("jsmith", membership.getMember().getUsername());
        assertEquals("product-support", membership.getGroup().getName());
        assertEquals(0, membership.getDetails().size());
        break;
      case "membership-group-details":
        assertEquals(102L, membership.getId());
        assertEquals("jsmith", membership.getMember().getUsername());
        assertEquals("product-support", membership.getGroup().getName());
        assertEquals(4, membership.getDetails().size());
        MembershipDetail detail = membership.getDetails().get(3);
        assertEquals(5, detail.getPosition());
        assertEquals("shape", detail.getName());
        assertEquals("square", detail.getValue());
        assertEquals("Shape or form", detail.getTitle());
        assertEquals("geometric", detail.getType());
        break;
      case "membership-project":
        assertEquals(101L, membership.getId());
        assertEquals("jsmith", membership.getMember().getUsername());
        assertEquals("acme", membership.getGroup().getName());
        assertEquals(0, membership.getDetails().size());
        break;
      default:
        fail("No membership assertions registered for sample " + sample);
    }
  }

  private static void assertGroup(ApiSample sample, Group group) {
    switch (sample.testName()) {
      case "group-extended":
        assertEquals(111L, group.getId());
        assertEquals("australia-nsw-sydney", group.getName());
        assertEquals(GroupType.GROUP, group.getType());
        break;
      case "project-extended":
        assertEquals(222L, group.getId());
        assertEquals("australia-nsw", group.getName());
        assertEquals(GroupType.PROJECT, group.getType());
        break;
      default:
        fail("No group assertions registered for sample " + sample);
    }
  }

  private static void assertError(ApiSample sample, ServiceErrorException error) {
    assertEquals(sample.statusCode(), error.getStatusCode());
    ServiceError payload = error.getError();
    switch (sample.testName()) {
      case "not_found":
        assertEquals("0106", payload.getId());
        assertEquals("Unable to find matching member.", payload.getMessage());
        break;
      default:
        fail("No error assertions registered for sample " + sample);
    }
  }

  private interface ApiContract {

    ServiceCall newCall(ApiSample sample);

    void verify(PageSeederClient client, ServiceCall call, ApiSample sample);
  }

  private static final class ServiceContracts {

    private static final Map<String, ApiContract> CONTRACTS = contracts();

    private ServiceContracts() {
    }

    private static ApiContract contractFor(String serviceId) {
      ApiContract contract = CONTRACTS.get(serviceId);
      if (contract == null) {
        throw new IllegalStateException("No API sample contract registered for service '" + serviceId + "'");
      }
      return contract;
    }

    private static Map<String, ApiContract> contracts() {
      Map<String, ApiContract> contracts = new LinkedHashMap<>();
      contracts.put("get_member", success(
          sample -> ServiceCall.of(ServiceCatalog.MEMBER)
              .pathVariable("member", sampleMember(sample))
              .accept(sample.format()),
          Member.class,
          ServiceApiSampleTest::assertMember,
          ServiceApiSampleTest::assertError));
      contracts.put("get_version", success(
          sample -> ServiceCall.of(ServiceCatalog.VERSION).accept(sample.format()),
          Version.class,
          ServiceApiSampleTest::assertVersion,
          null));
      contracts.put("get_membership", success(
          sample -> ServiceCall.of(ServiceCatalog.endpoint("GET", "/groups/{group}/members/{member}"))
              .pathVariable("group", sampleGroup(sample))
              .pathVariable("member", "jsmith")
              .accept(sample.format()),
          Membership.class,
          ServiceApiSampleTest::assertMembership,
          null));
      contracts.put("get_group", success(
          sample -> ServiceCall.of(sample.testName().startsWith("project")
                  ? ServiceCatalog.endpoint("GET", "/projects/{group}")
                  : ServiceCatalog.GROUP)
              .pathVariable("group", sampleEntityGroup(sample))
              .accept(sample.format()),
          Group.class,
          ServiceApiSampleTest::assertGroup,
          null));
      contracts.put("get_comment", success(
          sample -> ServiceCall.of(ServiceCatalog.endpoint("GET", "/comments/{commentid}"))
              .pathVariable("commentid", sampleCommentId(sample))
              .accept(sample.format()),
          Comment.class,
          ServiceApiSampleTest::assertComment,
          null));
      contracts.put("get_list-member-memberships", new ApiContract() {
        @Override
        public ServiceCall newCall(ApiSample sample) {
          return ServiceCall.of(ServiceCatalog.MEMBER_MEMBERSHIPS)
              .pathVariable("member", "jsmith")
              .accept(sample.format());
        }

        @Override
        public void verify(PageSeederClient client, ServiceCall call, ApiSample sample) {
          List<Membership> memberships = client.execute(call, Decoders.list(Membership.class));
          assertEquals(3, memberships.size());
          assertNull(memberships.get(0).getMember());
          assertEquals("acme", memberships.get(0).getGroup().getName());
          assertNull(memberships.get(1).getMember());
          assertEquals("acme-info", memberships.get(1).getGroup().getName());
          assertNull(memberships.get(2).getMember());
          assertEquals("product-support", memberships.get(2).getGroup().getName());
        }
      });
      contracts.put("self", success(
          sample -> ServiceCall.of(ServiceCatalog.endpoint("GET", "/self")).accept(sample.format()),
          Member.class,
          ServiceApiSampleTest::assertMember,
          null));
      return contracts;
    }

    private static String sampleMember(ApiSample sample) {
      switch (sample.testName()) {
        case "admin":
          return "vvega";
        case "disabled":
          return "jwinnfield";
        case "not_found":
          return "999999";
        default:
          throw new IllegalStateException("No member path variable registered for sample " + sample);
      }
    }

    private static String sampleGroup(ApiSample sample) {
      switch (sample.testName()) {
        case "membership-group":
        case "membership-group-details":
          return "product-support";
        case "membership-project":
          return "acme";
        default:
          throw new IllegalStateException("No group path variable registered for sample " + sample);
      }
    }

    private static String sampleEntityGroup(ApiSample sample) {
      switch (sample.testName()) {
        case "group-extended":
          return "australia-nsw-sydney";
        case "project-extended":
          return "australia-nsw";
        default:
          throw new IllegalStateException("No group entity path variable registered for sample " + sample);
      }
    }

    private static String sampleCommentId(ApiSample sample) {
      switch (sample.testName()) {
        case "comment-group":
          return "25061";
        case "comment-uri":
          return "25062";
        case "comment-type":
          return "25063";
        case "comment-type-and-uri":
          return "25064";
        case "comment-task":
          return "25065";
        case "comment-task-uri":
          return "25066";
        case "comment-task-type":
          return "25067";
        case "comment-task-type-uri":
          return "25068";
        case "comment-type-xhtml":
          return "2674355";
        case "comment-attachments":
          return "25091";
        case "comment-public1":
          return "23504";
        case "comment-nasty":
          return "25093";
        default:
          throw new IllegalStateException("No comment path variable registered for sample " + sample);
      }
    }

    private static <T> ApiContract success(Function<ApiSample, ServiceCall> factory,
                                           Class<T> type,
                                           BiConsumer<ApiSample, T> successAssertions,
                                           BiConsumer<ApiSample, ServiceErrorException> errorAssertions) {
      return new ApiContract() {
        @Override
        public ServiceCall newCall(ApiSample sample) {
          return factory.apply(sample);
        }

        @Override
        public void verify(PageSeederClient client, ServiceCall call, ApiSample sample) {
          if (sample.isSuccess()) {
            T actual = client.execute(call, Decoders.object(type));
            successAssertions.accept(sample, actual);
            return;
          }
          try {
            client.execute(call);
            fail("Expected ServiceErrorException for " + sample);
          } catch (ServiceErrorException ex) {
            if (errorAssertions == null) {
              throw ex;
            }
            errorAssertions.accept(sample, ex);
          }
        }
      };
    }
  }

  private static final class ApiSamples {

    private static final Pattern SAMPLE_FILE = Pattern.compile("(?<status>\\d{3})_(?<name>.+)\\.(?<ext>xml|json)");

    private ApiSamples() {
    }

    private static List<ApiSample> load() {
      if (!Files.isDirectory(API_FIXTURES_DIRECTORY)) {
        throw new IllegalStateException("Missing API fixtures directory: " + API_FIXTURES_DIRECTORY.toAbsolutePath());
      }
      try (Stream<Path> files = Files.walk(API_FIXTURES_DIRECTORY, 2)) {
        List<ApiSample> samples = files
            .filter(Files::isRegularFile)
            .map(ApiSamples::toSample)
            .sorted(Comparator.comparing(ApiSample::serviceId)
                .thenComparingInt(ApiSample::statusCode)
                .thenComparing(ApiSample::testName)
                .thenComparing(sample -> sample.format().name()))
            .collect(Collectors.toCollection(ArrayList::new));
        if (samples.isEmpty()) {
          throw new IllegalStateException("No API fixtures found in " + API_FIXTURES_DIRECTORY.toAbsolutePath());
        }
        samples.forEach(sample -> ServiceContracts.contractFor(sample.serviceId()));
        return samples;
      } catch (IOException ex) {
        throw new IllegalStateException("Unable to load API fixture samples", ex);
      }
    }

    private static ApiSample toSample(Path path) {
      Path relative = API_FIXTURES_DIRECTORY.relativize(path);
      if (relative.getNameCount() != 2) {
        throw new IllegalStateException("Unexpected API fixture location: " + path);
      }
      String serviceId = relative.getName(0).toString();
      String fileName = relative.getName(1).toString();
      Matcher matcher = SAMPLE_FILE.matcher(fileName);
      if (!matcher.matches()) {
        throw new IllegalStateException("Invalid API fixture name: " + path);
      }
      int statusCode = Integer.parseInt(matcher.group("status"));
      String testName = matcher.group("name");
      PayloadFormat format = "json".equals(matcher.group("ext").toLowerCase(Locale.ROOT))
          ? PayloadFormat.JSON
          : PayloadFormat.XML;
      return new ApiSample(serviceId, statusCode, testName, format, path);
    }
  }

  private static final class ApiSample {

    private final String serviceId;
    private final int statusCode;
    private final String testName;
    private final PayloadFormat format;
    private final Path path;

    private ApiSample(String serviceId, int statusCode, String testName, PayloadFormat format, Path path) {
      this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
      this.statusCode = statusCode;
      this.testName = Objects.requireNonNull(testName, "testName");
      this.format = Objects.requireNonNull(format, "format");
      this.path = Objects.requireNonNull(path, "path");
    }

    private String serviceId() {
      return this.serviceId;
    }

    private int statusCode() {
      return this.statusCode;
    }

    private String testName() {
      return this.testName;
    }

    private PayloadFormat format() {
      return this.format;
    }

    private boolean isSuccess() {
      return this.statusCode >= 200 && this.statusCode < 300;
    }

    private byte[] readBody() throws IOException {
      return Files.readAllBytes(this.path);
    }

    @Override
    public String toString() {
      return this.serviceId + "/" + this.statusCode + "_" + this.testName + this.format.extension();
    }
  }
}
