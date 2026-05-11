package org.pageseeder.sdk.model.codec;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.ConfiguredGroup;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupRole;
import org.pageseeder.sdk.model.GroupSettings;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.NotificationPreference;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PageSeederParserTest {

  private final XmlPageSeederParser xml = new XmlPageSeederParser();
  private final JsonPageSeederParser json = new JsonPageSeederParser();

  @Test
  void shouldParseMemberFromXmlAndJson() throws IOException {
    Member xmlMember = this.xml.parse(read("fixtures/member.xml"), Member.class);
    Member jsonMember = this.json.parse(read("fixtures/member.json"), Member.class);

    assertEquals("jdoe", xmlMember.username());
    assertEquals("John Doe", xmlMember.fullname());
    assertTrue(xmlMember.locked());
    assertEquals("jdoe@example.com", jsonMember.email());
    assertEquals(OffsetDateTime.parse("2024-02-03T10:15:30+10:00"), xmlMember.lastLogin());
  }

  @Test
  void shouldParseExtendedMemberFieldsFromXmlAndJson() throws IOException {
    Member xmlMember = this.xml.parse(read("api/get_member/200_admin.xml"), Member.class);
    Member jsonMember = this.json.parse(read("api/get_member/200_admin.json"), Member.class);

    assertEquals("Vincent Vega", xmlMember.fullname());
    assertEquals("Vincent Vega", jsonMember.fullname());
    assertEquals(OffsetDateTime.parse("2016-02-25T13:48:12+11:00"), xmlMember.lastPasswordChange());
    assertEquals(OffsetDateTime.parse("2026-04-19T11:25:47+10:00"), jsonMember.lastLogin());
    assertTrue(xmlMember.admin());
    assertTrue(jsonMember.admin());
  }

  @Test
  void shouldParseCompleteServerMemberShape() {
    byte[] payload = """
        <member id="77" firstname="A" surname="User" username="auser" externalid="ext-77"
            created="2024-01-02T03:04:05+10:00" activated="2024-01-03T03:04:05+10:00"
            lastpasswordchange="2024-01-04T03:04:05+10:00" lastlogin="2024-01-05T03:04:05+10:00"
            status="set-password" locked="true" onvacation="true" attachments="true" admin="true"
            date="2024-01-06T03:04:05+10:00">
          <fullname>A User</fullname>
        </member>
        """.getBytes(StandardCharsets.UTF_8);

    Member member = this.xml.parse(payload, Member.class);

    assertEquals("ext-77", member.externalId());
    assertEquals("A User", member.fullname());
    assertEquals(OffsetDateTime.parse("2024-01-02T03:04:05+10:00"), member.created());
    assertEquals(OffsetDateTime.parse("2024-01-03T03:04:05+10:00"), member.activated());
    assertEquals(OffsetDateTime.parse("2024-01-04T03:04:05+10:00"), member.lastPasswordChange());
    assertEquals(OffsetDateTime.parse("2024-01-06T03:04:05+10:00"), member.date());
    assertTrue(member.onVacation());
    assertTrue(member.attachments());
    assertTrue(member.locked());
    assertTrue(member.admin());
  }

  @Test
  void shouldParseMemberMembershipResultsFromXml() throws IOException {
    List<Membership> xmlPage = this.xml.parseList(
        read("api/get_list-member-memberships/200_memberships-formember.xml"), Membership.class);

    assertEquals(3, xmlPage.size());
    assertNull(xmlPage.get(0).member());
    assertNull(xmlPage.get(1).member());
    assertNull(xmlPage.get(2).member());
    assertEquals("acme-info", xmlPage.get(1).group().name());
    assertEquals("product-support", xmlPage.get(2).group().name());
  }

  @Test
  void shouldParseSingleMembershipFromXmlAndJson() throws IOException {
    Membership xmlMembership = this.xml.parse(read("api/get_membership/200_membership-group.xml"), Membership.class);
    Membership jsonMembership = this.json.parse(read("api/get_membership/200_membership-group.json"), Membership.class);

    assertEquals("jsmith", xmlMembership.member().username());
    assertEquals("product-support", xmlMembership.group().name());
    assertEquals("jsmith", jsonMembership.member().username());
    assertEquals("product-support", jsonMembership.group().name());
  }

  @Test
  void shouldParseMembershipDetailsFromXmlAndJson() throws IOException {
    Membership xmlMembership = this.xml.parse(read("api/get_membership/200_membership-group-details.xml"), Membership.class);
    Membership jsonMembership = this.json.parse(read("api/get_membership/200_membership-group-details.json"), Membership.class);

    assertEquals(4, xmlMembership.details().size());
    assertEquals(4, jsonMembership.details().size());

    MembershipDetail xmlDetail = xmlMembership.details().get(3);
    assertEquals(5, xmlDetail.position());
    assertEquals("shape", xmlDetail.name());
    assertEquals("square", xmlDetail.value());
    assertEquals("Shape or form", xmlDetail.title());
    assertEquals("geometric", xmlDetail.type());

    MembershipDetail jsonDetail = jsonMembership.details().get(1);
    assertEquals(2, jsonDetail.position());
    assertEquals("title", jsonDetail.name());
    assertEquals("Mr", jsonDetail.value());
    assertTrue(jsonDetail.editable());
    assertEquals("title", jsonDetail.title());
    assertEquals("text", jsonDetail.type());
  }

  @Test
  void shouldParseGroupsAndProjectsFromXmlAndJson() throws IOException {
    Group xmlGroup = this.xml.parse(read("api/get_group/200_group-extended.xml"), Group.class);
    Group jsonGroup = this.json.parse(read("api/get_group/200_group-extended.json"), Group.class);
    Group xmlProject = this.xml.parse(read("api/get_group/200_project-extended.xml"), Group.class);
    Group jsonProject = this.json.parse(read("api/get_group/200_project-extended.json"), Group.class);

    assertEquals(GroupType.GROUP, xmlGroup.type());
    assertEquals(GroupType.GROUP, jsonGroup.type());
    assertEquals("australia-nsw-sydney", xmlGroup.name());
    assertEquals("public", xmlGroup.access());
    assertEquals("public", jsonGroup.access());
    assertTrue(xmlGroup.common());
    assertTrue(jsonGroup.common());
    assertEquals("https://example.org/hello.html", xmlGroup.relatedUrl());
    assertEquals("https://example.org/hello.html", jsonGroup.relatedUrl());
    assertEquals(GroupType.PROJECT, xmlProject.type());
    assertEquals(GroupType.PROJECT, jsonProject.type());
    assertTrue(xmlProject.type() == GroupType.PROJECT);
    assertEquals("australia-nsw", jsonProject.name());
    assertEquals("member", xmlProject.access());
    assertEquals("member", jsonProject.access());
    assertFalse(xmlProject.common());
    assertFalse(jsonProject.common());
  }

  @Test
  void shouldParseConfiguredGroupsFromXmlAndJson() throws IOException {
    ConfiguredGroup xmlGroup = this.xml.parse(read("api/get_group/200_group-extended.xml"), ConfiguredGroup.class);
    ConfiguredGroup jsonGroup = this.json.parse(read("api/get_group/200_group-extended.json"), ConfiguredGroup.class);
    ConfiguredGroup xmlProject = this.xml.parse(read("api/get_group/200_project-extended.xml"), ConfiguredGroup.class);
    ConfiguredGroup jsonProject = this.json.parse(read("api/get_group/200_project-extended.json"), ConfiguredGroup.class);

    assertConfiguredGroup(xmlGroup, GroupType.GROUP, "australia-nsw-sydney", "public", true,
        "australia-nsw-sydney", "australia-nsw", "city", 91100);
    assertConfiguredGroup(jsonGroup, GroupType.GROUP, "australia-nsw-sydney", "public", true,
        "australia-nsw-sydney", "australia-nsw", "city", 5003);
    assertConfiguredGroup(xmlProject, GroupType.PROJECT, "australia-nsw", "member", false,
        "australia-nsw", "australia-nsw", "state", 5002);
    assertConfiguredGroup(jsonProject, GroupType.PROJECT, "australia-nsw", "member", false,
        "australia-nsw", "australia-nsw", "state", 5002);
  }

  @Test
  void shouldParseCommentsFromXmlAndJson() throws IOException {
    Comment xmlTask = this.xml.parse(read("api/get_comment/200_comment-task-type-uri.xml"), Comment.class);
    Comment jsonTask = this.json.parse(read("api/get_comment/200_comment-task-type-uri.json"), Comment.class);
    Comment xmlMarkup = this.xml.parse(read("api/get_comment/200_comment-type-xhtml.xml"), Comment.class);
    Comment jsonMarkup = this.json.parse(read("api/get_comment/200_comment-type-xhtml.json"), Comment.class);

    assertEquals(25068L, xmlTask.id());
    assertEquals("mytype1", xmlTask.type());
    assertEquals("Open", xmlTask.status());
    assertEquals("High", jsonTask.priority());
    assertEquals("unit-admin", jsonTask.assignedTo().user().member().username());
    assertEquals("/ps/unittest/comment/findcomment/documents/finddoc.psml", jsonTask.context().uri().path());
    assertEquals("Unit Test", jsonTask.assignedTo().user().fullname());

    assertEquals("forum", xmlMarkup.type());
    assertEquals("application/xhtml+xml", xmlMarkup.content().get(0).type());
    assertTrue(xmlMarkup.content().get(0).value().contains("<p>"));
    assertEquals(xmlMarkup.content().get(0).value(), jsonMarkup.content().get(0).value());
  }

  @Test
  void shouldParseResourceUriAndError() throws IOException {
    ResourceUri xmlUri = this.xml.parse(read("fixtures/resource-uri.xml"), ResourceUri.class);
    ResourceUri jsonUri = this.json.parse(read("fixtures/resource-uri.json"), ResourceUri.class);
    ServiceError xmlError = this.xml.parseError(read("fixtures/service-error.xml"));
    ServiceError jsonError = this.json.parseError(read("fixtures/service-error.json"));

    assertEquals("/docs/guide.psml", xmlUri.path());
    assertEquals(List.of("guide", "published"), jsonUri.labels());
    assertEquals("deadbeef", xmlError.id());
    assertEquals("Missing member", jsonError.message());
  }

  @Test
  void shouldParseVersionFromXmlAndJsonIntoSdkAndBridgeModels() throws IOException {
    Version xmlVersion = this.xml.parse(read("fixtures/version.xml"), Version.class);
    Version jsonVersion = this.json.parse(read("fixtures/version.json"), Version.class);

    assertEquals(5, xmlVersion.major());
    assertEquals(9804, jsonVersion.build());
    assertEquals("5.9804", xmlVersion.string());
  }

  @Test
  void shouldFailOnMalformedPayload() {
    assertThrows(ParsingException.class,
        () -> this.xml.parse("<member".getBytes(StandardCharsets.UTF_8), Member.class));
  }

  @Test
  void shouldRejectExternalXmlEntities() {
    byte[] payload = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE member [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <member id="1" username="&xxe;" status="activated" locked="false" onvacation="false" attachments="false"/>
        """.getBytes(StandardCharsets.UTF_8);

    assertThrows(ParsingException.class, () -> this.xml.parse(payload, Member.class));
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = PageSeederParserTest.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new IOException("Missing fixture " + path);
      }
      return in.readAllBytes();
    }
  }

  private static void assertConfiguredGroup(ConfiguredGroup configured, GroupType type, String name, String access,
                                            boolean common, String visibility, String template, String detailsType,
                                            int indexVersion) {
    Group group = configured.group();
    GroupSettings settings = configured.settings();

    assertEquals(type, group.type());
    assertEquals(name, group.name());
    assertEquals(access, group.access());
    assertEquals(common, group.common());
    assertEquals("https://example.org/hello.html", group.relatedUrl());
    assertEquals("For unit testing", group.description());
    assertEquals("Australia", group.owner());
    assertEquals(GroupRole.REVIEWER, group.defaultRole());
    assertEquals(NotificationPreference.IMMEDIATE, group.defaultNotification());

    assertEquals(visibility, settings.visibility());
    assertEquals(template, settings.template());
    assertEquals(detailsType, settings.detailsType());
    assertFalse(settings.editUrls());
    assertEquals("public", settings.commenting());
    assertEquals("reviewer", settings.moderation());
    assertEquals("normal", settings.registration());
    assertEquals(GroupRole.REVIEWER, settings.defaultRole());
    assertEquals(NotificationPreference.IMMEDIATE, settings.defaultNotification());
    assertEquals(indexVersion, settings.indexVersion());
    assertNull(settings.message());
  }
}
