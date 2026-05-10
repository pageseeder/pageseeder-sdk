package org.pageseeder.sdk.model.codec;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class PageSeederParserTest {

  private final XmlPageSeederParser xml = new XmlPageSeederParser();
  private final JsonPageSeederParser json = new JsonPageSeederParser();

  @Test
  public void shouldParseMemberFromXmlAndJson() throws IOException {
    Member xmlMember = this.xml.parse(read("fixtures/member.xml"), Member.class);
    Member jsonMember = this.json.parse(read("fixtures/member.json"), Member.class);

    assertEquals("jdoe", xmlMember.getUsername());
    assertEquals("John Doe", xmlMember.getFullname());
    assertTrue(xmlMember.isLocked());
    assertEquals("jdoe@example.com", jsonMember.getEmail());
  }

  @Test
  public void shouldParseMemberMembershipResultsFromXml() throws IOException {
    List<Membership> xmlPage = this.xml.parseList(
        read("api/get_list-member-memberships/200_memberships-formember.xml"), Membership.class);

    assertEquals(3, xmlPage.size());
    assertNull(xmlPage.get(0).getMember());
    assertNull(xmlPage.get(1).getMember());
    assertNull(xmlPage.get(2).getMember());
    assertEquals("acme-info", xmlPage.get(1).getGroup().getName());
    assertEquals("product-support", xmlPage.get(2).getGroup().getName());
  }

  @Test
  public void shouldParseSingleMembershipFromXmlAndJson() throws IOException {
    Membership xmlMembership = this.xml.parse(read("api/get_membership/200_membership-group.xml"), Membership.class);
    Membership jsonMembership = this.json.parse(read("api/get_membership/200_membership-group.json"), Membership.class);

    assertEquals("jsmith", xmlMembership.getMember().getUsername());
    assertEquals("product-support", xmlMembership.getGroup().getName());
    assertEquals("jsmith", jsonMembership.getMember().getUsername());
    assertEquals("product-support", jsonMembership.getGroup().getName());
  }

  @Test
  public void shouldParseMembershipDetailsFromXmlAndJson() throws IOException {
    Membership xmlMembership = this.xml.parse(read("api/get_membership/200_membership-group-details.xml"), Membership.class);
    Membership jsonMembership = this.json.parse(read("api/get_membership/200_membership-group-details.json"), Membership.class);

    assertEquals(4, xmlMembership.getDetails().size());
    assertEquals(4, jsonMembership.getDetails().size());

    MembershipDetail xmlDetail = xmlMembership.getDetails().get(3);
    assertEquals(5, xmlDetail.getPosition());
    assertEquals("shape", xmlDetail.getName());
    assertEquals("square", xmlDetail.getValue());
    assertEquals("Shape or form", xmlDetail.getTitle());
    assertEquals("geometric", xmlDetail.getType());

    MembershipDetail jsonDetail = jsonMembership.getDetails().get(1);
    assertEquals(2, jsonDetail.getPosition());
    assertEquals("title", jsonDetail.getName());
    assertEquals("Mr", jsonDetail.getValue());
    assertTrue(jsonDetail.isEditable());
    assertEquals("title", jsonDetail.getTitle());
    assertEquals("text", jsonDetail.getType());
  }

  @Test
  public void shouldParseGroupsAndProjectsFromXmlAndJson() throws IOException {
    Group xmlGroup = this.xml.parse(read("api/get_group/200_group-extended.xml"), Group.class);
    Group jsonGroup = this.json.parse(read("api/get_group/200_group-extended.json"), Group.class);
    Group xmlProject = this.xml.parse(read("api/get_group/200_project-extended.xml"), Group.class);
    Group jsonProject = this.json.parse(read("api/get_group/200_project-extended.json"), Group.class);

    assertEquals(GroupType.GROUP, xmlGroup.getType());
    assertEquals(GroupType.GROUP, jsonGroup.getType());
    assertEquals("australia-nsw-sydney", xmlGroup.getName());
    assertEquals(GroupType.PROJECT, xmlProject.getType());
    assertEquals(GroupType.PROJECT, jsonProject.getType());
    assertTrue(xmlProject.isProject());
    assertEquals("australia-nsw", jsonProject.getName());
  }

  @Test
  public void shouldParseCommentsFromXmlAndJson() throws IOException {
    Comment xmlTask = this.xml.parse(read("api/get_comment/200_comment-task-type-uri.xml"), Comment.class);
    Comment jsonTask = this.json.parse(read("api/get_comment/200_comment-task-type-uri.json"), Comment.class);
    Comment xmlMarkup = this.xml.parse(read("api/get_comment/200_comment-type-xhtml.xml"), Comment.class);
    Comment jsonMarkup = this.json.parse(read("api/get_comment/200_comment-type-xhtml.json"), Comment.class);

    assertEquals(25068L, xmlTask.getId());
    assertEquals("mytype1", xmlTask.getType());
    assertEquals("Open", xmlTask.getStatus());
    assertEquals("High", jsonTask.getPriority());
    assertEquals("unit-admin", jsonTask.getAssignedTo().getUser().getMember().getUsername());
    assertEquals("/ps/unittest/comment/findcomment/documents/finddoc.psml", jsonTask.getContext().getUri().getPath());
    assertEquals("Unit Test", jsonTask.getAssignedTo().getUser().getFullname());

    assertEquals("forum", xmlMarkup.getType());
    assertEquals("application/xhtml+xml", xmlMarkup.getContent().get(0).getType());
    assertTrue(xmlMarkup.getContent().get(0).getValue().contains("<p>"));
    assertEquals(xmlMarkup.getContent().get(0).getValue(), jsonMarkup.getContent().get(0).getValue());
  }

  @Test
  public void shouldParseResourceUriAndError() throws IOException {
    ResourceUri xmlUri = this.xml.parse(read("fixtures/resource-uri.xml"), ResourceUri.class);
    ResourceUri jsonUri = this.json.parse(read("fixtures/resource-uri.json"), ResourceUri.class);
    ServiceError xmlError = this.xml.parseError(read("fixtures/service-error.xml"));
    ServiceError jsonError = this.json.parseError(read("fixtures/service-error.json"));

    assertEquals("/docs/guide.psml", xmlUri.getPath());
    assertEquals(List.of("guide", "published"), jsonUri.getLabels());
    assertEquals("deadbeef", xmlError.getId());
    assertEquals("Missing member", jsonError.getMessage());
  }

  @Test
  public void shouldParseVersionFromXmlAndJsonIntoSdkAndBridgeModels() throws IOException {
    Version xmlVersion = this.xml.parse(read("fixtures/version.xml"), Version.class);
    Version jsonVersion = this.json.parse(read("fixtures/version.json"), Version.class);

    assertEquals(5, xmlVersion.getMajor());
    assertEquals(9804, jsonVersion.getBuild());
    assertEquals("5.9804", xmlVersion.getString());
  }

  @Test
  public void shouldFailOnMalformedPayload() {
    assertThrows(ParsingException.class,
        () -> this.xml.parse("<member".getBytes(StandardCharsets.UTF_8), Member.class));
  }

  @Test
  public void shouldRejectExternalXmlEntities() {
    byte[] payload = (
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE member [\n"
            + "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n"
            + "]>\n"
            + "<member id=\"1\" username=\"&xxe;\" status=\"activated\" locked=\"false\" "
            + "onvacation=\"false\" attachments=\"false\"/>\n"
    ).getBytes(StandardCharsets.UTF_8);

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
}
