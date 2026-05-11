package org.pageseeder.sdk.model.codec;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.model.Authenticator;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.ConfiguredGroup;
import org.pageseeder.sdk.model.DocumentVersion;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupFolder;
import org.pageseeder.sdk.model.GroupFolderPublicAccess;
import org.pageseeder.sdk.model.GroupFolderSharing;
import org.pageseeder.sdk.model.GroupRelationListing;
import org.pageseeder.sdk.model.GroupRole;
import org.pageseeder.sdk.model.GroupSettings;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.MemberData;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.model.MembershipOverride;
import org.pageseeder.sdk.model.MembershipStatus;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.NotificationPreference;
import org.pageseeder.sdk.model.OAuthClient;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.Subgroup;
import org.pageseeder.sdk.model.Supergroup;
import org.pageseeder.sdk.model.Version;
import org.pageseeder.sdk.model.Webhook;
import org.pageseeder.sdk.model.Workflow;
import org.pageseeder.sdk.oauth.GrantType;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
  void shouldParseMemberDataFromXmlAndJson() {
    byte[] xmlPayload = """
        <memberdata id="501" name="public-avatar" title="Avatar" created="2024-02-01T09:00:00+10:00"
            modified="2024-02-02T09:00:00+10:00" mediatype="image/png" length="2048"
            public="true" memberid="7"/>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "memberdata": {
            "id": "501",
            "name": "public-avatar",
            "title": "Avatar",
            "created": "2024-02-01T09:00:00+10:00",
            "modified": "2024-02-02T09:00:00+10:00",
            "mediatype": "image/png",
            "length": "2048",
            "public": "true",
            "memberid": "7"
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    MemberData xmlData = this.xml.parse(xmlPayload, MemberData.class);
    MemberData jsonData = this.json.parse(jsonPayload, MemberData.class);

    assertMemberData(xmlData);
    assertMemberData(jsonData);
  }

  @Test
  void shouldParseMemberDataListWithMissingContent() {
    byte[] payload = """
        {
          "memberdata": [
            {
              "id": "502",
              "name": "private-note",
              "public": "false"
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    MemberData data = this.json.parseList(payload, MemberData.class).get(0);

    assertEquals(502L, data.id());
    assertEquals("private-note", data.name());
    assertFalse(data.publiclyVisible());
    assertEquals(0, data.length());
    assertNull(data.mediaType());
    assertNull(data.memberId());
  }

  @Test
  void shouldParseAuthenticatorFromXmlAndJson() {
    byte[] xmlPayload = """
        <authenticator id="601" member="7" public-id="public-abc" data="setup-secret" name="Passkey"
            type="webauthn" created="2024-02-01T09:00:00+10:00" last-used="2024-02-02T09:00:00+10:00"
            verified="false">
          <parameters rpId="example.com" credential_id="credential-1"/>
        </authenticator>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "authenticator": {
            "id": "601",
            "member": "7",
            "public-id": "public-abc",
            "data": "setup-secret",
            "name": "Passkey",
            "type": "webauthn",
            "created": "2024-02-01T09:00:00+10:00",
            "last-used": "2024-02-02T09:00:00+10:00",
            "verified": "false",
            "parameters": {
              "rpId": "example.com",
              "credential_id": "credential-1"
            }
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    Authenticator xmlAuthenticator = this.xml.parse(xmlPayload, Authenticator.class);
    Authenticator jsonAuthenticator = this.json.parse(jsonPayload, Authenticator.class);

    assertAuthenticator(xmlAuthenticator);
    assertAuthenticator(jsonAuthenticator);
  }

  @Test
  void shouldParseVerifiedAuthenticatorWithoutData() {
    byte[] payload = """
        {
          "authenticators": [
            {
              "id": "602",
              "member": "7",
              "public-id": "public-def",
              "type": "totp",
              "created": "2024-02-01T09:00:00+10:00",
              "verified": "true"
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    Authenticator authenticator = this.json.parseList(payload, Authenticator.class).get(0);

    assertEquals(602L, authenticator.id());
    assertTrue(authenticator.verified());
    assertNull(authenticator.data());
    assertTrue(authenticator.parameters().isEmpty());
  }

  @Test
  void shouldParseMemberMembershipResultsFromXml() throws IOException {
    List<Membership> xmlPage = this.xml.parseList(
        read("api/get_list-member-memberships/200_memberships-formember.xml"), Membership.class);
    List<Membership> jsonPage = this.json.parseList(
        read("api/get_list-member-memberships/200_memberships-formember.json"), Membership.class);

    assertEquals(3, xmlPage.size());
    assertEquals(3, jsonPage.size());
    assertEquals("jsmith", xmlPage.get(0).member().username());
    assertEquals("jsmith", xmlPage.get(1).member().username());
    assertEquals("jsmith", xmlPage.get(2).member().username());
    assertEquals("jsmith", jsonPage.get(0).member().username());
    assertTrue(xmlPage.get(0).listed());
    assertTrue(jsonPage.get(0).listed());
    assertEquals("acme-info", xmlPage.get(1).group().name());
    assertEquals("product-support", xmlPage.get(2).group().name());
    assertEquals("acme-info", jsonPage.get(1).group().name());
    assertEquals("product-support", jsonPage.get(2).group().name());
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
  void shouldParseMembershipOptionalServerFields() {
    byte[] payload = """
        <membership id="102" email-listed="false" notification="weekly" status="self-invited"
            role="reviewer" deleted="true" subgroups="alpha, beta" override="listed,notification,role"
            created="2018-01-01T01:01:01+10:00">
          <member id="123" firstname="John" surname="Smith" username="jsmith"/>
          <group id="203" name="product-support" description="Support group for product" owner="Product"
              access="member" common="false"/>
        </membership>
        """.getBytes(StandardCharsets.UTF_8);

    Membership membership = this.xml.parse(payload, Membership.class);

    assertFalse(membership.listed());
    assertTrue(membership.deleted());
    assertEquals(MembershipStatus.SELF_INVITED, membership.status());
    assertEquals(List.of("alpha", "beta"), membership.subgroups());
    assertEquals(Set.of(MembershipOverride.LISTED, MembershipOverride.NOTIFICATION, MembershipOverride.ROLE),
        membership.overrides());
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
    assertSame(GroupType.PROJECT, xmlProject.type());
    assertEquals("australia-nsw", jsonProject.name());
    assertEquals("member", xmlProject.access());
    assertEquals("member", jsonProject.access());
    assertFalse(xmlProject.common());
    assertFalse(jsonProject.common());
  }

  @Test
  void shouldParseGroupFolderFromXmlAndJson() {
    byte[] xmlPayload = """
        <groupfolder id="88" scheme="https" host="example.com" port="443" path="/ps/acme/documents"
            external="false" public="true" sharing="shared"/>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "groupfolder": {
            "id": "88",
            "scheme": "https",
            "host": "example.com",
            "port": "443",
            "path": "/ps/acme/documents",
            "external": "false",
            "public": "true",
            "sharing": "shared"
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    GroupFolder xmlFolder = this.xml.parse(xmlPayload, GroupFolder.class);
    GroupFolder jsonFolder = this.json.parse(jsonPayload, GroupFolder.class);

    assertGroupFolder(xmlFolder);
    assertGroupFolder(jsonFolder);
  }

  @Test
  void shouldParseGroupFolderWithoutSharing() {
    byte[] payload = """
        {
          "groupfolder": {
            "id": "89",
            "scheme": "https",
            "host": "example.com",
            "port": "443",
            "path": "/ps/acme/private",
            "external": "false",
            "public": "false"
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    GroupFolder folder = this.json.parse(payload, GroupFolder.class);

    assertEquals(89L, folder.id());
    assertEquals(GroupFolderPublicAccess.NOT_PUBLIC, folder.publicAccess());
    assertEquals(GroupFolderSharing.UNKNOWN, folder.sharing());
  }

  @Test
  void shouldParseGroupFolderListFromJson() {
    byte[] payload = """
        {
          "result": {
            "total": "2",
            "start": "0",
            "groupfolder": [
              {
                "id": "88",
                "scheme": "https",
                "host": "example.com",
                "port": "443",
                "path": "/ps/acme/documents",
                "external": "false",
                "public": "true",
                "sharing": "shared"
              },
              {
                "id": "89",
                "scheme": "https",
                "host": "example.com",
                "port": "443",
                "path": "/ps/acme/private",
                "external": "false",
                "public": "false",
                "sharing": "private"
              }
            ]
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    List<GroupFolder> folders = this.json.parseList(payload, GroupFolder.class);

    assertEquals(2, folders.size());
    assertEquals("/ps/acme/documents", folders.get(0).path());
    assertEquals(GroupFolderSharing.SHARED, folders.get(0).sharing());
    assertEquals(GroupFolderSharing.PRIVATE, folders.get(1).sharing());
  }

  @Test
  void shouldParseSubgroupsFromXmlAndJson() {
    byte[] xmlPayload = """
        <subgroups>
          <subgroup id="301" role="manager" notification="immediate" listed="true">
            <group id="201" name="acme-docs" description="Documentation" owner="acme" access="member" common="false"/>
          </subgroup>
          <subgroup id="302" role="inherit" notification="inherit" listed="inherit">
            <group id="202" name="acme-info" description="Information" owner="acme" access="member" common="false"/>
          </subgroup>
        </subgroups>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "subgroups": [
            {
              "id": "301",
              "role": "manager",
              "notification": "immediate",
              "listed": "true",
              "group": {
                "id": "201",
                "name": "acme-docs",
                "description": "Documentation",
                "owner": "acme",
                "access": "member",
                "common": "false"
              }
            },
            {
              "id": "302",
              "role": "inherit",
              "notification": "inherit",
              "listed": "inherit",
              "group": {
                "id": "202",
                "name": "acme-info",
                "description": "Information",
                "owner": "acme",
                "access": "member",
                "common": "false"
              }
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    List<Subgroup> xmlSubgroups = this.xml.parseList(xmlPayload, Subgroup.class);
    List<Subgroup> jsonSubgroups = this.json.parseList(jsonPayload, Subgroup.class);

    assertEquals(2, xmlSubgroups.size());
    assertEquals(2, jsonSubgroups.size());
    assertEquals(301L, xmlSubgroups.get(0).id());
    assertEquals(GroupRole.MANAGER, xmlSubgroups.get(0).role());
    assertEquals(NotificationPreference.IMMEDIATE, xmlSubgroups.get(0).notification());
    assertEquals(GroupRelationListing.LISTED, xmlSubgroups.get(0).listed());
    assertEquals("acme-docs", xmlSubgroups.get(0).group().name());
    assertEquals(GroupRole.INHERIT, jsonSubgroups.get(1).role());
    assertEquals(NotificationPreference.INHERIT, jsonSubgroups.get(1).notification());
    assertEquals(GroupRelationListing.INHERIT, jsonSubgroups.get(1).listed());
    assertEquals("acme-info", jsonSubgroups.get(1).group().name());
  }

  @Test
  void shouldParseSupergroupsFromXmlAndJson() {
    byte[] xmlPayload = """
        <supergroups>
          <supergroup id="401" role="reviewer" notification="daily" listed="false">
            <group id="101" name="acme" description="ACME" owner="acme" access="member" common="false"/>
          </supergroup>
        </supergroups>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "supergroups": [
            {
              "id": "401",
              "role": "reviewer",
              "notification": "daily",
              "listed": "false",
              "group": {
                "id": "101",
                "name": "acme",
                "description": "ACME",
                "owner": "acme",
                "access": "member",
                "common": "false"
              }
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    Supergroup xmlSupergroup = this.xml.parseList(xmlPayload, Supergroup.class).get(0);
    Supergroup jsonSupergroup = this.json.parseList(jsonPayload, Supergroup.class).get(0);

    assertEquals(401L, xmlSupergroup.id());
    assertEquals(GroupRole.REVIEWER, xmlSupergroup.role());
    assertEquals(NotificationPreference.DAILY, xmlSupergroup.notification());
    assertEquals(GroupRelationListing.NOT_LISTED, xmlSupergroup.listed());
    assertEquals("acme", xmlSupergroup.group().name());
    assertEquals(xmlSupergroup, jsonSupergroup);
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
  void shouldParseCompleteServerUriShape() {
    byte[] folderPayload = """
        <uri id="88" scheme="https" host="example.com" port="443" path="/docs/My%20Folder"
            decodedpath="/docs/My Folder" external="false" sharing="shared" docid="FOLDER-1"
            mediatype="folder" documenttype="folder" created="2024-03-05T12:00:00+10:00"
            modified="2024-03-06T13:00:00+10:00" title="My Folder" size="2048">
          <displaytitle>My Folder</displaytitle>
          <description>Shared documents</description>
          <labels>
            <label>guide</label>
            <label>published</label>
          </labels>
        </uri>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] externalPayload = """
        {
          "uri": {
            "id": "89",
            "scheme": "https",
            "host": "external.example.com",
            "port": "443",
            "path": "/resource",
            "decodedpath": "/resource",
            "external": "true",
            "mediatype": "url",
            "urltype": "website",
            "displaytitle": "External resource",
            "size": "512"
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    ResourceUri folder = this.xml.parse(folderPayload, ResourceUri.class);
    ResourceUri external = this.json.parse(externalPayload, ResourceUri.class);

    assertEquals("/docs/My%20Folder", folder.path());
    assertEquals("/docs/My Folder", folder.decodedPath());
    assertEquals("My Folder", folder.displayTitle());
    assertEquals("folder", folder.documentType());
    assertTrue(folder.folder());
    assertEquals(2048L, folder.size());
    assertEquals(GroupFolderSharing.SHARED, folder.sharing());
    assertEquals(List.of("guide", "published"), folder.labels());

    assertTrue(external.external());
    assertFalse(external.folder());
    assertEquals("website", external.urlType());
    assertEquals("External resource", external.displayTitle());
    assertEquals(512L, external.size());
  }

  @Test
  void shouldParseOAuthClientFromXmlAndJson() {
    byte[] xmlPayload = """
        <client id="42" identifier="example-client" requires-consent="true" confidential="true"
            name="Example client" grant-type="authorization_code"
            created="2024-01-02T03:04:05+10:00" modified="2024-01-03T03:04:05+10:00"
            last-token="2024-01-04T03:04:05+10:00" app="demo" webhook-secret="secret"
            redirect-uri="https://client.example.com/callback" description="OAuth client"
            client-uri="https://client.example.com" scope="openid email profile"
            access-token-max-age="3600" refresh-token-max-age="2592000">
          <member id="7" username="jdoe" firstname="John" surname="Doe" email="jdoe@example.com"/>
        </client>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "client": {
            "id": "42",
            "identifier": "example-client",
            "requires-consent": "true",
            "confidential": "true",
            "name": "Example client",
            "grant-type": "authorization_code",
            "created": "2024-01-02T03:04:05+10:00",
            "modified": "2024-01-03T03:04:05+10:00",
            "last-token": "2024-01-04T03:04:05+10:00",
            "app": "demo",
            "webhook-secret": "secret",
            "redirect-uri": "https://client.example.com/callback",
            "description": "OAuth client",
            "client-uri": "https://client.example.com",
            "scope": "openid email profile",
            "access-token-max-age": "3600",
            "refresh-token-max-age": "2592000",
            "member": {
              "id": "7",
              "username": "jdoe",
              "firstname": "John",
              "surname": "Doe",
              "email": "jdoe@example.com"
            }
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    OAuthClient xmlClient = this.xml.parse(xmlPayload, OAuthClient.class);
    OAuthClient jsonClient = this.json.parse(jsonPayload, OAuthClient.class);

    assertOAuthClient(xmlClient);
    assertOAuthClient(jsonClient);
  }

  @Test
  void shouldParseOAuthClientListFromJson() {
    byte[] payload = """
        {
          "result": {
            "total": "2",
            "start": "0",
            "client": [
              {
                "identifier": "first-client",
                "requires-consent": "false",
                "confidential": "true",
                "name": "First client",
                "grant-type": "client_credentials",
                "access-token-max-age": "600",
                "refresh-token-max-age": "0"
              },
              {
                "identifier": "second-client",
                "requires-consent": "true",
                "confidential": "false",
                "name": "Second client",
                "grant-type": "implicit",
                "access-token-max-age": "1200",
                "refresh-token-max-age": "0"
              }
            ]
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    List<OAuthClient> clients = this.json.parseList(payload, OAuthClient.class);

    assertEquals(2, clients.size());
    assertEquals("first-client", clients.get(0).identifier());
    assertEquals(GrantType.CLIENT_CREDENTIALS, clients.get(0).grantType());
    assertEquals(Duration.ofSeconds(1200), clients.get(1).accessTokenMaxAge());
  }

  @Test
  void shouldParseWebhookFromXmlAndJson() {
    byte[] xmlPayload = """
        <webhook id="77" created="2024-01-02T03:04:05+10:00" modified="2024-01-03T03:04:05+10:00"
            url="https://hooks.example.com/pageseeder" server="dev" object="uri" format="json"
            insecuressl="true" status="active" name="Content sync" projects="acme"
            groups="acme-info,product-support" events="uri-created,uri-modified">
          <client identifier="example-client" requires-consent="false" confidential="true"
              name="Example client" grant-type="client_credentials" access-token-max-age="3600"
              refresh-token-max-age="0"/>
        </webhook>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "webhook": {
            "id": "77",
            "created": "2024-01-02T03:04:05+10:00",
            "modified": "2024-01-03T03:04:05+10:00",
            "url": "https://hooks.example.com/pageseeder",
            "server": "dev",
            "object": "uri",
            "format": "json",
            "insecuressl": "true",
            "status": "active",
            "name": "Content sync",
            "projects": {
              "project": ["acme"]
            },
            "groups": {
              "group": ["acme-info", "product-support"]
            },
            "events": {
              "event": ["uri-created", "uri-modified"]
            },
            "client": {
              "identifier": "example-client",
              "requires-consent": "false",
              "confidential": "true",
              "name": "Example client",
              "grant-type": "client_credentials",
              "access-token-max-age": "3600",
              "refresh-token-max-age": "0"
            }
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    Webhook xmlWebhook = this.xml.parse(xmlPayload, Webhook.class);
    Webhook jsonWebhook = this.json.parse(jsonPayload, Webhook.class);

    assertWebhook(xmlWebhook);
    assertWebhook(jsonWebhook);
  }

  @Test
  void shouldParseWebhookListFromJson() {
    byte[] payload = """
        {
          "webhooks": [
            {
              "id": "77",
              "created": "2024-01-02T03:04:05+10:00",
              "modified": "2024-01-03T03:04:05+10:00",
              "url": "https://hooks.example.com/one",
              "server": "dev",
              "object": "uri",
              "format": "json",
              "insecuressl": "false",
              "status": "active"
            },
            {
              "id": "78",
              "created": "2024-01-04T03:04:05+10:00",
              "modified": "2024-01-05T03:04:05+10:00",
              "url": "https://hooks.example.com/two",
              "server": "dev",
              "object": "comment",
              "format": "xml",
              "insecuressl": "true",
              "status": "paused",
              "events": "comment-created,comment-modified"
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    List<Webhook> webhooks = this.json.parseList(payload, Webhook.class);

    assertEquals(2, webhooks.size());
    assertEquals(77L, webhooks.get(0).id());
    assertFalse(webhooks.get(0).insecureSsl());
    assertEquals(List.of("comment-created", "comment-modified"), webhooks.get(1).events());
  }

  @Test
  void shouldParseWorkflowFromXmlAndJson() {
    byte[] xmlPayload = """
        <workflow id="25068" status="Initiated" priority="Medium" due="2024-05-01T09:30:00+10:00"
            statuschanged="2024-04-01T09:30:00+10:00">
          <assignedto id="92" firstname="Unit" surname="Test" username="unit-admin" date="2024-04-02T10:00:00+10:00">
            <fullname>Unit Test</fullname>
          </assignedto>
          <uri id="88" scheme="https" host="example.com" port="443" path="/docs/guide.psml"
              external="false" mediatype="application/vnd.pageseeder.psml+xml">
            <displaytitle>Guide</displaytitle>
          </uri>
        </workflow>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "workflow": {
            "id": "25068",
            "status": "Initiated",
            "priority": "Medium",
            "due": "2024-05-01T09:30:00+10:00",
            "statuschanged": "2024-04-01T09:30:00+10:00",
            "assignedto": {
              "id": "92",
              "firstname": "Unit",
              "surname": "Test",
              "username": "unit-admin",
              "fullname": "Unit Test",
              "date": "2024-04-02T10:00:00+10:00"
            },
            "uri": {
              "id": "88",
              "scheme": "https",
              "host": "example.com",
              "port": "443",
              "path": "/docs/guide.psml",
              "external": "false",
              "mediatype": "application/vnd.pageseeder.psml+xml",
              "displaytitle": "Guide"
            },
            "comments": [
              {
                "id": "25068",
                "discussionid": "300",
                "type": "task",
                "created": "2024-04-01T09:30:00+10:00",
                "title": "Review guide",
                "author": {
                  "id": "92",
                  "firstname": "Unit",
                  "surname": "Test",
                  "username": "unit-admin",
                  "fullname": "Unit Test"
                },
                "status": "Initiated",
                "priority": "Medium",
                "content": {
                  "type": "text/plain",
                  "value": "Please review"
                }
              }
            ]
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    Workflow xmlWorkflow = this.xml.parse(xmlPayload, Workflow.class);
    Workflow jsonWorkflow = this.json.parse(jsonPayload, Workflow.class);

    assertWorkflow(xmlWorkflow, false);
    assertWorkflow(jsonWorkflow, true);
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
  void shouldParseDocumentVersionFromXmlAndJson() {
    byte[] xmlPayload = """
        <version id="1201" name="Initial draft" created="2024-04-01T09:30:00+10:00" publicationid="PUB-1">
          <author id="7" firstname="John" surname="Doe">
            <fullname>John Doe</fullname>
          </author>
          <description>First published version</description>
          <labels>
            <label>draft</label>
            <label>approved</label>
          </labels>
        </version>
        """.getBytes(StandardCharsets.UTF_8);
    byte[] jsonPayload = """
        {
          "version": {
            "id": "1201",
            "name": "Initial draft",
            "created": "2024-04-01T09:30:00+10:00",
            "publicationid": "PUB-1",
            "author": {
              "id": "7",
              "firstname": "John",
              "surname": "Doe",
              "fullname": "John Doe"
            },
            "description": "First published version",
            "labels": {
              "label": ["draft", "approved"]
            }
          }
        }
        """.getBytes(StandardCharsets.UTF_8);

    DocumentVersion xmlVersion = this.xml.parse(xmlPayload, DocumentVersion.class);
    DocumentVersion jsonVersion = this.json.parse(jsonPayload, DocumentVersion.class);

    assertDocumentVersion(xmlVersion);
    assertDocumentVersion(jsonVersion);
  }

  @Test
  void shouldParseDocumentVersionListWithAuthorName() {
    byte[] payload = """
        {
          "versions": [
            {
              "id": "1201",
              "name": "Initial draft",
              "created": "2024-04-01T09:30:00+10:00",
              "author": {
                "fullname": "System Import"
              },
              "description": ["Imported version", "Checked"]
            }
          ]
        }
        """.getBytes(StandardCharsets.UTF_8);

    DocumentVersion version = this.json.parseList(payload, DocumentVersion.class).get(0);

    assertEquals(1201L, version.id());
    assertEquals("System Import", version.author().fullname());
    assertNull(version.author().member());
    assertEquals(List.of("Imported version", "Checked"), version.descriptions());
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

  private static void assertGroupFolder(GroupFolder folder) {
    assertEquals(88L, folder.id());
    assertEquals("https", folder.scheme());
    assertEquals("example.com", folder.host());
    assertEquals(443, folder.port());
    assertEquals("/ps/acme/documents", folder.path());
    assertFalse(folder.external());
    assertEquals(GroupFolderPublicAccess.PUBLIC, folder.publicAccess());
    assertEquals(GroupFolderSharing.SHARED, folder.sharing());
  }

  private static void assertMemberData(MemberData data) {
    assertEquals(501L, data.id());
    assertEquals("public-avatar", data.name());
    assertEquals("Avatar", data.title());
    assertEquals(OffsetDateTime.parse("2024-02-01T09:00:00+10:00"), data.created());
    assertEquals(OffsetDateTime.parse("2024-02-02T09:00:00+10:00"), data.modified());
    assertEquals("image/png", data.mediaType());
    assertEquals(2048, data.length());
    assertTrue(data.publiclyVisible());
    assertEquals(7L, data.memberId());
  }

  private static void assertAuthenticator(Authenticator authenticator) {
    assertEquals(601L, authenticator.id());
    assertEquals(7L, authenticator.memberId());
    assertEquals("public-abc", authenticator.publicId());
    assertEquals("setup-secret", authenticator.data());
    assertEquals("Passkey", authenticator.name());
    assertEquals("webauthn", authenticator.type());
    assertEquals(OffsetDateTime.parse("2024-02-01T09:00:00+10:00"), authenticator.created());
    assertEquals(OffsetDateTime.parse("2024-02-02T09:00:00+10:00"), authenticator.lastUsed());
    assertFalse(authenticator.verified());
    assertEquals(Map.of("rpId", "example.com", "credential_id", "credential-1"), authenticator.parameters());
  }

  private static void assertOAuthClient(OAuthClient client) {
    assertEquals(42L, client.id());
    assertEquals("example-client", client.identifier());
    assertTrue(client.requiresConsent());
    assertTrue(client.confidential());
    assertEquals("Example client", client.name());
    assertEquals(GrantType.AUTHORIZATION_CODE, client.grantType());
    assertEquals(OffsetDateTime.parse("2024-01-02T03:04:05+10:00"), client.created());
    assertEquals(OffsetDateTime.parse("2024-01-03T03:04:05+10:00"), client.modified());
    assertEquals(OffsetDateTime.parse("2024-01-04T03:04:05+10:00"), client.lastToken());
    assertEquals("demo", client.appName());
    assertEquals("secret", client.webhookSecret());
    assertEquals("https://client.example.com/callback", client.redirectUri().toString());
    assertEquals("OAuth client", client.description());
    assertEquals("https://client.example.com", client.clientUri().toString());
    assertEquals("openid email profile", client.scope());
    assertEquals(Duration.ofSeconds(3600), client.accessTokenMaxAge());
    assertEquals(2592000L, client.refreshTokenMaxAgeSeconds());
    assertEquals("jdoe", client.member().username());
  }

  private static void assertDocumentVersion(DocumentVersion version) {
    assertEquals(1201L, version.id());
    assertEquals("Initial draft", version.name());
    assertEquals(OffsetDateTime.parse("2024-04-01T09:30:00+10:00"), version.created());
    assertEquals("PUB-1", version.publicationId());
    assertEquals("John Doe", version.author().fullname());
    assertEquals(7L, version.author().member().id());
    assertEquals(List.of("First published version"), version.descriptions());
    assertEquals(List.of("draft", "approved"), version.labels());
  }

  private static void assertWebhook(Webhook webhook) {
    assertEquals(77L, webhook.id());
    assertEquals(OffsetDateTime.parse("2024-01-02T03:04:05+10:00"), webhook.created());
    assertEquals(OffsetDateTime.parse("2024-01-03T03:04:05+10:00"), webhook.modified());
    assertEquals("https://hooks.example.com/pageseeder", webhook.url().toString());
    assertEquals("dev", webhook.server());
    assertEquals("uri", webhook.object());
    assertEquals("json", webhook.format());
    assertTrue(webhook.insecureSsl());
    assertEquals("active", webhook.status());
    assertEquals("Content sync", webhook.name());
    assertEquals(List.of("acme"), webhook.projects());
    assertEquals(List.of("acme-info", "product-support"), webhook.groups());
    assertEquals(List.of("uri-created", "uri-modified"), webhook.events());
    assertEquals("example-client", webhook.client().identifier());
  }

  private static void assertWorkflow(Workflow workflow, boolean includesComments) {
    assertEquals(25068L, workflow.id());
    assertEquals("Initiated", workflow.status());
    assertEquals("Medium", workflow.priority());
    assertEquals(OffsetDateTime.parse("2024-05-01T09:30:00+10:00"), workflow.due());
    assertEquals(OffsetDateTime.parse("2024-04-01T09:30:00+10:00"), workflow.statusChanged());
    assertEquals("unit-admin", workflow.assignedTo().user().member().username());
    assertEquals("Unit Test", workflow.assignedTo().user().fullname());
    assertEquals(OffsetDateTime.parse("2024-04-02T10:00:00+10:00"), workflow.assignedTo().date());
    assertEquals("/docs/guide.psml", workflow.uri().path());
    assertEquals("Guide", workflow.uri().displayTitle());
    if (includesComments) {
      assertEquals(1, workflow.comments().size());
      assertEquals("Review guide", workflow.comments().get(0).title());
      assertEquals("Please review", workflow.comments().get(0).content().get(0).value());
    } else {
      assertTrue(workflow.comments().isEmpty());
    }
  }
}
