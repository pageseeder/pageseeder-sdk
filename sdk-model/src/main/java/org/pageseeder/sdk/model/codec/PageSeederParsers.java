package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.model.Authenticator;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.CommentContext;
import org.pageseeder.sdk.model.CommentUser;
import org.pageseeder.sdk.model.ConfiguredGroup;
import org.pageseeder.sdk.model.Content;
import org.pageseeder.sdk.model.DocumentVersion;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupFolder;
import org.pageseeder.sdk.model.GroupFolderPublicAccess;
import org.pageseeder.sdk.model.GroupFolderSharing;
import org.pageseeder.sdk.model.GroupRelationListing;
import org.pageseeder.sdk.model.GroupSettings;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.MemberData;
import org.pageseeder.sdk.model.MemberStatus;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.model.MembershipOverride;
import org.pageseeder.sdk.model.MembershipStatus;
import org.pageseeder.sdk.model.NotificationPreference;
import org.pageseeder.sdk.model.OAuthClient;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.model.StampedCommentUser;
import org.pageseeder.sdk.model.Subgroup;
import org.pageseeder.sdk.model.Supergroup;
import org.pageseeder.sdk.model.GroupRole;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.Version;
import org.pageseeder.sdk.model.Webhook;
import org.pageseeder.sdk.model.Workflow;
import org.pageseeder.sdk.oauth.GrantType;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@SuppressWarnings("java:S1192") // PageSeeder payload field names are clearer inline in this schema mapper.
final class PageSeederParsers {

  private static final Map<Class<?>, NodeMapper> NODE_MAPPERS = Map.ofEntries(
      Map.entry(Member.class, (node, rootElementName, context) -> toMember(node)),
      Map.entry(Authenticator.class, (node, rootElementName, context) -> toAuthenticator(node)),
      Map.entry(MemberData.class, (node, rootElementName, context) -> toMemberData(node)),
      Map.entry(Comment.class, (node, rootElementName, context) -> toComment(node)),
      Map.entry(Group.class, (node, rootElementName, context) -> toGroup(node, rootElementName)),
      Map.entry(ConfiguredGroup.class, (node, rootElementName, context) -> toConfiguredGroup(node, rootElementName)),
      Map.entry(DocumentVersion.class, (node, rootElementName, context) -> toDocumentVersion(node)),
      Map.entry(GroupFolder.class, (node, rootElementName, context) -> toGroupFolder(node)),
      Map.entry(Membership.class, (node, rootElementName, context) -> toMembership(node, context)),
      Map.entry(Subgroup.class, (node, rootElementName, context) -> toSubgroup(node)),
      Map.entry(Supergroup.class, (node, rootElementName, context) -> toSupergroup(node)),
      Map.entry(OAuthClient.class, (node, rootElementName, context) -> toOAuthClient(node)),
      Map.entry(ResourceUri.class, (node, rootElementName, context) -> toResourceUri(node)),
      Map.entry(Version.class, (node, rootElementName, context) -> toVersion(node)),
      Map.entry(Webhook.class, (node, rootElementName, context) -> toWebhook(node)),
      Map.entry(Workflow.class, (node, rootElementName, context) -> toWorkflow(node))
  );

  private static final Map<Class<?>, List<String>> NODE_ALIASES = Map.ofEntries(
      Map.entry(Authenticator.class, List.of("authenticator")),
      Map.entry(Member.class, List.of("member")),
      Map.entry(MemberData.class, List.of("memberdata")),
      Map.entry(Comment.class, List.of("comment")),
      Map.entry(DocumentVersion.class, List.of("version")),
      Map.entry(Group.class, List.of("group", "project")),
      Map.entry(ConfiguredGroup.class, List.of("group", "project")),
      Map.entry(GroupFolder.class, List.of("groupfolder")),
      Map.entry(Membership.class, List.of("membership")),
      Map.entry(Subgroup.class, List.of("subgroup")),
      Map.entry(Supergroup.class, List.of("supergroup")),
      Map.entry(OAuthClient.class, List.of("client")),
      Map.entry(ResourceUri.class, List.of("uri")),
      Map.entry(Version.class, List.of("version")),
      Map.entry(Webhook.class, List.of("webhook")),
      Map.entry(Workflow.class, List.of("workflow"))
  );

  private PageSeederParsers() {
  }

  static ObjectMapper newJsonMapper() {
    ObjectMapper mapper = new ObjectMapper();
    return configure(mapper);
  }

  static XmlMapper newXmlMapper() {
    XmlFactory factory = XmlFactory.builder()
        .xmlInputFactory(newXmlInputFactory())
        .build();
    XmlMapper mapper = XmlMapper.builder(factory).defaultUseWrapper(false).build();
    return configure(mapper);
  }

  private static <T extends ObjectMapper> T configure(T mapper) {
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper;
  }

  static <T> T parse(ObjectMapper mapper, byte[] body, Class<T> type) {
    JsonNode root = readTree(mapper, body);
    return mapNode(root, type, rootElementName(mapper, body));
  }

  static <T> List<T> parseList(ObjectMapper mapper, byte[] body, Class<T> type) {
    JsonNode root = readTree(mapper, body);
    @Nullable ListNode list = findListNode(root, aliases(type));
    MappingContext context = mappingContext(root, type, rootElementName(mapper, body));
    List<T> items = new ArrayList<>();
    if (list == null) {
      return items;
    }
    JsonNode listNode = list.node();
    if (listNode.isMissingNode() || listNode.isNull()) {
      return items;
    }
    if (listNode.isArray()) {
      for (JsonNode itemNode : listNode) {
        items.add(mapNode(itemNode, type, list.rootElementName(), context));
      }
    } else {
      items.add(mapNode(listNode, type, list.rootElementName(), context));
    }
    return items;
  }

  static <T> ResultPage<T> parseResultPage(ObjectMapper mapper, byte[] body, Class<T> type) {
    JsonNode root = readTree(mapper, body);
    JsonNode envelope = root.has("result") ? root.get("result") : root;
    int total = intValue(envelope, "total");
    int start = intValue(envelope, "start");
    return new ResultPage<>(total, start, parseList(mapper, body, type));
  }

  static ServiceError parseError(ObjectMapper mapper, byte[] body) {
    JsonNode root = readTree(mapper, body);
    JsonNode error = root.has("error") ? root.get("error") : root;
    @Nullable String id = nullableText(error, "id");
    @Nullable String message = nullableText(error, "message");
    if (message == null && error.has("description")) {
      message = nullableText(error, "description");
    }
    return new ServiceError(id, message == null ? "Unknown service error" : message);
  }

  static byte[] serialize(ObjectMapper mapper, Object value) {
    try {
      return mapper.writeValueAsBytes(value);
    } catch (JsonProcessingException ex) {
      throw new ParsingException("Unable to serialize request body", ex);
    }
  }

  private static JsonNode readTree(ObjectMapper mapper, byte[] body) {
    try {
      return mapper.readTree(body);
    } catch (IOException ex) {
      throw new ParsingException("Unable to parse PageSeeder payload", ex);
    }
  }

  private static <T> T mapNode(JsonNode node, Class<T> type) {
    return mapNode(node, type, null);
  }

  private static <T> T mapNode(JsonNode node, Class<T> type, @Nullable String rootElementName) {
    return mapNode(node, type, rootElementName, MappingContext.empty());
  }

  @SuppressWarnings("unchecked")
  private static <T> T mapNode(JsonNode node, Class<T> type, @Nullable String rootElementName, MappingContext context) {
    @Nullable NodeMapper mapper = NODE_MAPPERS.get(type);
    if (mapper == null) {
      throw new ParsingException("Unsupported PageSeeder mapping type: " + type.getName());
    }
    return (T) mapper.map(node, rootElementName, context);
  }

  private interface NodeMapper {

    Object map(JsonNode node, @Nullable String rootElementName, MappingContext context);
  }

  private record ListNode(JsonNode node, @Nullable String rootElementName) {
  }

  private record MappingContext(@Nullable Member member, @Nullable Group group) {

    static MappingContext empty() {
      return new MappingContext(null, null);
    }
  }

  private static @Nullable ListNode findListNode(JsonNode root, List<String> aliases) {
    if (root.has("result")) {
      return findListNode(root.get("result"), aliases);
    }
    for (String alias : aliases) {
      if (root.has(alias)) {
        return new ListNode(root.get(alias), alias);
      }
    }
    for (String alias : aliases) {
      if (root.has(alias + "s")) {
        return new ListNode(root.get(alias + "s"), alias);
      }
    }
    if (root.isArray()) {
      return new ListNode(root, null);
    }
    return null;
  }

  private static List<String> aliases(Class<?> type) {
    return NODE_ALIASES.getOrDefault(type, List.of(type.getSimpleName().toLowerCase(Locale.ROOT)));
  }

  private static MappingContext mappingContext(JsonNode root, Class<?> type, @Nullable String rootElementName) {
    if (type != Membership.class) {
      return MappingContext.empty();
    }
    JsonNode source = root.has("result") ? root.get("result") : root;
    @Nullable Member member = source.has("member") ? toMember(source.get("member")) : null;
    @Nullable Group group = null;
    if (source.has("group")) {
      group = toGroup(source.get("group"), "group");
    } else if (source.has("project")) {
      group = toGroup(source.get("project"), "project");
    } else if ("group".equals(rootElementName) || "project".equals(rootElementName)) {
      group = toGroup(source, rootElementName);
    }
    return new MappingContext(member, group);
  }

  private static Member toMember(JsonNode node) {
    JsonNode source = unwrap(node, "member");
    return new Member(
        longValue(source, "id"),
        stringValue(source, "username"),
        nullableText(source, "email"),
        defaultText(source, "firstname", ""),
        defaultText(source, "surname", ""),
        MemberStatus.fromValue(nullableText(source, "status")),
        booleanValue(source, "locked"),
        booleanValue(source, "onvacation"),
        booleanValue(source, "attachments"),
        nullableText(source, "externalid"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "activated"),
        offsetDateTime(source, "lastpasswordchange"),
        offsetDateTime(source, "lastlogin"),
        booleanValue(source, "admin"),
        offsetDateTime(source, "date")
    );
  }

  private static Authenticator toAuthenticator(JsonNode node) {
    JsonNode source = unwrap(node, "authenticator");
    return new Authenticator(
        longValue(source, "id"),
        longValue(source, "member"),
        nullableText(source, "public-id"),
        source.has("data") ? nullableText(source, "data") : null,
        nullableText(source, "name"),
        nullableText(source, "type"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "last-used"),
        booleanValue(source, "verified"),
        stringMap(source.get("parameters"))
    );
  }

  private static MemberData toMemberData(JsonNode node) {
    JsonNode source = unwrap(node, "memberdata");
    return new MemberData(
        longValue(source, "id"),
        stringValue(source, "name"),
        nullableText(source, "title"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "modified"),
        nullableText(source, "mediatype"),
        intValue(source, "length"),
        booleanValue(source, "public"),
        nullableLong(source, "memberid")
    );
  }

  private static Comment toComment(JsonNode node) {
    JsonNode source = unwrap(node, "comment");
    return Comment.fromParsed(
        longValue(source, "id"),
        longValue(source, "discussionid"),
        nullableText(source, "contentrole"),
        nullableText(source, "type"),
        offsetDateTime(source, "created"),
        nullableText(source, "title"),
        toCommentUser(source.get("author")),
        toStampedCommentUser(source.get("modifiedby")),
        toStampedCommentUser(source.get("assignedto")),
        nullableText(source, "status"),
        nullableText(source, "priority"),
        offsetDateTime(source, "due"),
        immutable(contents(source.get("content"))),
        toCommentContext(source.get("context")),
        immutable(attachments(source.get("attachment")))
    );
  }

  private static Workflow toWorkflow(JsonNode node) {
    JsonNode source = unwrap(node, "workflow");
    return new Workflow(
        longValue(source, "id"),
        nullableText(source, "status"),
        nullableText(source, "priority"),
        offsetDateTime(source, "due"),
        offsetDateTime(source, "statuschanged"),
        toStampedCommentUser(source.get("assignedto")),
        source.has("uri") ? toResourceUri(source.get("uri")) : null,
        comments(source.get("comments"))
    );
  }

  private static Group toGroup(JsonNode node) {
    return toGroup(node, null);
  }

  private static DocumentVersion toDocumentVersion(JsonNode node) {
    JsonNode source = unwrap(node, "version");
    return new DocumentVersion(
        longValue(source, "id"),
        nullableText(source, "name"),
        offsetDateTime(source, "created"),
        nullableText(source, "publicationid"),
        documentVersionAuthor(source.get("author")),
        stringList(source.get("description"), "description"),
        labels(source.get("labels"))
    );
  }

  private static Group toGroup(JsonNode node, @Nullable String rootElementName) {
    @Nullable String elementName = groupElementName(node, rootElementName);
    JsonNode source = unwrapGroup(node);
    GroupType type = elementName != null
        ? GroupType.fromValue(elementName)
        : GroupType.fromValue(nullableText(source, "type"));
    return new Group(
        longValue(source, "id"),
        stringValue(source, "name"),
        type,
        nullableText(source, "title"),
        nullableText(source, "description"),
        nullableText(source, "owner"),
        nullableText(source, "access"),
        booleanValue(source, "common"),
        nullableText(source, "relatedurl"),
        GroupRole.fromValue(nullableText(source, "defaultrole")),
        NotificationPreference.fromValue(nullableText(source, "defaultnotify"))
    );
  }

  private static ConfiguredGroup toConfiguredGroup(JsonNode node, @Nullable String rootElementName) {
    @Nullable String elementName = groupElementName(node, rootElementName);
    JsonNode source = unwrapGroup(node);
    return new ConfiguredGroup(toGroup(source, elementName), toGroupSettings(source));
  }

  private static GroupFolder toGroupFolder(JsonNode node) {
    JsonNode source = unwrap(node, "groupfolder");
    return new GroupFolder(
        longValue(source, "id"),
        stringValue(source, "scheme"),
        stringValue(source, "host"),
        intValue(source, "port"),
        stringValue(source, "path"),
        booleanValue(source, "external"),
        GroupFolderPublicAccess.fromValue(nullableText(source, "public")),
        GroupFolderSharing.fromValue(nullableText(source, "sharing"))
    );
  }

  private static GroupSettings toGroupSettings(JsonNode source) {
    return new GroupSettings(
        nullableText(source, "visibility"),
        nullableText(source, "template"),
        nullableText(source, "detailstype"),
        nullableBoolean(source, "editurls"),
        nullableText(source, "commenting"),
        nullableText(source, "moderation"),
        nullableText(source, "registration"),
        GroupRole.fromValue(nullableText(source, "defaultrole")),
        NotificationPreference.fromValue(nullableText(source, "defaultnotify")),
        nullableInteger(source, "indexversion"),
        nullableText(source, "message")
    );
  }

  private static Membership toMembership(JsonNode node, MappingContext context) {
    JsonNode source = unwrap(node, "membership");
    @Nullable Member member = source.has("member") ? toMember(source.get("member")) : context.member();
    @Nullable Group group = source.has("group")
        ? toGroup(source.get("group"), "group")
        : source.has("project") ? toGroup(source.get("project"), "project") : context.group();
    return new Membership(
        longValue(source, "id"),
        Objects.requireNonNull(member, "member"),
        Objects.requireNonNull(group, "group"),
        GroupRole.fromValue(nullableText(source, "role")),
        MembershipStatus.fromValue(nullableText(source, "status")),
        NotificationPreference.fromValue(nullableText(source, "notification")),
        booleanValue(source, "email-listed", "emailListed"),
        booleanValue(source, "deleted"),
        tokens(source, "subgroups"),
        membershipOverrides(source, "override"),
        offsetDateTime(source, "created"),
        details(source.get("details"))
    );
  }

  private static Subgroup toSubgroup(JsonNode node) {
    JsonNode source = unwrap(node, "subgroup");
    return new Subgroup(
        longValue(source, "id"),
        GroupRole.fromValue(nullableText(source, "role")),
        NotificationPreference.fromValue(nullableText(source, "notification")),
        GroupRelationListing.fromValue(nullableText(source, "listed")),
        relationGroup(source)
    );
  }

  private static Supergroup toSupergroup(JsonNode node) {
    JsonNode source = unwrap(node, "supergroup");
    return new Supergroup(
        longValue(source, "id"),
        GroupRole.fromValue(nullableText(source, "role")),
        NotificationPreference.fromValue(nullableText(source, "notification")),
        GroupRelationListing.fromValue(nullableText(source, "listed")),
        relationGroup(source)
    );
  }

  private static Group relationGroup(JsonNode source) {
    if (source.has("group")) {
      return toGroup(source.get("group"), "group");
    }
    if (source.has("project")) {
      return toGroup(source.get("project"), "project");
    }
    return toGroup(source);
  }

  private static ResourceUri toResourceUri(@Nullable JsonNode node) {
    JsonNode source = unwrap(node, "uri");
    return new ResourceUri(
        longValue(source, "id"),
        nullableText(source, "scheme"),
        nullableText(source, "host"),
        intValue(source, "port"),
        nullableText(source, "path"),
        nullableText(source, "decodedpath"),
        nullableText(source, "title"),
        nullableText(source, "displaytitle"),
        nullableText(source, "docid"),
        nullableText(source, "description"),
        nullableText(source, "mediatype"),
        nullableText(source, "documenttype"),
        nullableText(source, "urltype"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "modified"),
        longValue(source, "size"),
        labels(source.get("labels")),
        booleanValue(source, "external"),
        GroupFolderSharing.fromValue(nullableText(source, "sharing"))
    );
  }

  private static OAuthClient toOAuthClient(JsonNode node) {
    JsonNode source = unwrap(node, "client");
    @Nullable Member member = source.has("member") ? toMember(source.get("member")) : null;
    return new OAuthClient(
        nullableLong(source, "id"),
        stringValue(source, "identifier"),
        booleanValue(source, "requires-consent"),
        booleanValue(source, "confidential"),
        stringValue(source, "name"),
        grantType(source, "grant-type"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "modified"),
        offsetDateTime(source, "last-token"),
        nullableText(source, "app"),
        nullableText(source, "webhook-secret"),
        uri(source, "redirect-uri"),
        nullableText(source, "description"),
        uri(source, "client-uri"),
        nullableText(source, "scope"),
        secondsDuration(source, "access-token-max-age"),
        secondsDuration(source, "refresh-token-max-age"),
        member
    );
  }

  private static Webhook toWebhook(JsonNode node) {
    JsonNode source = unwrap(node, "webhook");
    @Nullable OAuthClient client = source.has("client") ? toOAuthClient(source.get("client")) : null;
    return new Webhook(
        nullableLong(source, "id"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "modified"),
        uri(source, "url"),
        nullableText(source, "server"),
        nullableText(source, "object"),
        nullableText(source, "format"),
        booleanValue(source, "insecuressl"),
        nullableText(source, "status"),
        nullableText(source, "name"),
        stringList(source.get("projects"), "project"),
        stringList(source.get("groups"), "group"),
        stringList(source.get("events"), "event"),
        client
    );
  }

  private static Version toVersion(JsonNode node) {
    JsonNode source = unwrap(node, "version");
    int major = intValue(source, "major");
    int build = intValue(source, "build");
    @Nullable String version = nullableText(source, "string");
    return new Version(
        major,
        build,
        version == null ? major + "." + String.format("%04d", build) : version
    );
  }

  private static JsonNode unwrap(@Nullable JsonNode node, String alias) {
    if (node == null || node.isNull()) {
      return MissingNode.getInstance();
    }
    if (node.has(alias)) {
      return node.get(alias);
    }
    if (node.isObject() && node.size() == 1) {
      Iterator<JsonNode> values = node.elements();
      if (values.hasNext()) {
        JsonNode child = values.next();
        if (child != null && child.has(alias)) {
          return child.get(alias);
        }
      }
    }
    return node.has(alias) ? node.get(alias) : node;
  }

  private static JsonNode unwrapGroup(JsonNode node) {
    JsonNode source = unwrap(node, "group");
    return source == node ? unwrap(node, "project") : source;
  }

  private static @Nullable String groupElementName(@Nullable JsonNode node, @Nullable String rootElementName) {
    if (rootElementName != null) {
      return rootElementName;
    }
    if (node != null && node.has("project")) {
      return "project";
    }
    if (node != null && node.has("group")) {
      return "group";
    }
    return null;
  }

  private static @Nullable String rootElementName(ObjectMapper mapper, byte[] body) {
    if (!(mapper instanceof XmlMapper)) {
      return null;
    }
    XMLInputFactory factory = newXmlInputFactory();
    try (ByteArrayInputStream in = new ByteArrayInputStream(body)) {
      XMLStreamReader xml = factory.createXMLStreamReader(in);
      while (xml.hasNext()) {
        if (xml.next() == XMLStreamConstants.START_ELEMENT) {
          return xml.getLocalName();
        }
      }
      return null;
    } catch (XMLStreamException | IOException ex) {
      throw new ParsingException("Unable to determine XML root element", ex);
    }
  }

  static XMLInputFactory newXmlInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    return factory;
  }

  private static List<String> labels(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    if (node.isArray()) {
      List<String> labels = new ArrayList<>();
      for (JsonNode child : node) {
        labels.add(child.asText());
      }
      return labels;
    }
    if (node.has("label")) {
      JsonNode child = node.get("label");
      if (child.isArray()) {
        List<String> labels = new ArrayList<>();
        for (JsonNode item : child) {
          labels.add(item.asText());
        }
        return labels;
      }
      return List.of(child.asText());
    }
    if (node.isObject()) {
      List<String> labels = new ArrayList<>();
      Iterator<JsonNode> values = node.elements();
      while (values.hasNext()) {
        labels.add(values.next().asText());
      }
      return labels;
    }
    return List.of(node.asText());
  }

  private static List<MembershipDetail> details(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    JsonNode fields = node.has("field") ? node.get("field") : node;
    if (fields == null || fields.isNull()) {
      return List.of();
    }
    List<MembershipDetail> details = new ArrayList<>();
    if (fields.isArray()) {
      for (JsonNode field : fields) {
        details.add(toMembershipDetail(field));
      }
    } else {
      details.add(toMembershipDetail(fields));
    }
    return details;
  }

  private static List<String> tokens(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return List.of();
    }
    List<String> tokens = new ArrayList<>();
    for (String token : value.split(",")) {
      String normalized = token.trim();
      if (!normalized.isEmpty()) {
        tokens.add(normalized);
      }
    }
    return tokens.isEmpty() ? List.of() : List.copyOf(tokens);
  }

  private static Set<MembershipOverride> membershipOverrides(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return Set.of();
    }
    Set<MembershipOverride> overrides = new LinkedHashSet<>();
    for (String token : value.split(",")) {
      String normalized = token.trim();
      if (!normalized.isEmpty()) {
        overrides.add(MembershipOverride.fromValue(normalized));
      }
    }
    return overrides.isEmpty() ? Set.of() : Set.copyOf(overrides);
  }

  private static List<String> stringList(@Nullable JsonNode node, String itemField) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    if (node.isArray()) {
      List<String> values = new ArrayList<>();
      for (JsonNode item : node) {
        addStringListValue(values, item, itemField);
      }
      return values.isEmpty() ? List.of() : List.copyOf(values);
    }
    if (node.has(itemField)) {
      return stringList(node.get(itemField), itemField);
    }
    if (node.isObject()) {
      List<String> values = new ArrayList<>();
      Iterator<JsonNode> children = node.elements();
      while (children.hasNext()) {
        addStringListValue(values, children.next(), itemField);
      }
      return values.isEmpty() ? List.of() : List.copyOf(values);
    }
    return tokens(node.asText(""), ",");
  }

  private static void addStringListValue(List<String> values, @Nullable JsonNode node, String itemField) {
    if (node == null || node.isNull()) {
      return;
    }
    if (node.has(itemField)) {
      addStringListValue(values, node.get(itemField), itemField);
      return;
    }
    if (node.isArray()) {
      for (JsonNode item : node) {
        addStringListValue(values, item, itemField);
      }
      return;
    }
    values.add(node.asText());
  }

  private static Map<String, String> stringMap(@Nullable JsonNode node) {
    if (node == null || node.isNull() || !node.isObject()) {
      return Map.of();
    }
    Map<String, String> values = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> field : node.properties()) {
      JsonNode value = field.getValue();
      if (value != null && !value.isNull()) {
        values.put(field.getKey(), value.asText());
      }
    }
    return values.isEmpty() ? Map.of() : Map.copyOf(values);
  }

  private static List<String> tokens(@Nullable String value, String delimiter) {
    if (value == null || value.isBlank()) {
      return List.of();
    }
    List<String> tokens = new ArrayList<>();
    for (String token : value.split(delimiter)) {
      String normalized = token.trim();
      if (!normalized.isEmpty()) {
        tokens.add(normalized);
      }
    }
    return tokens.isEmpty() ? List.of() : List.copyOf(tokens);
  }

  private static List<Content> contents(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    List<Content> content = new ArrayList<>();
    if (node.isArray()) {
      for (JsonNode item : node) {
        content.add(toCommentContent(item));
      }
    } else {
      content.add(toCommentContent(node));
    }
    return content;
  }

  private static List<Comment> comments(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    JsonNode comments = node.has("comment") ? node.get("comment") : node;
    if (comments == null || comments.isNull()) {
      return List.of();
    }
    List<Comment> values = new ArrayList<>();
    if (comments.isArray()) {
      for (JsonNode comment : comments) {
        values.add(toComment(comment));
      }
    } else {
      values.add(toComment(comments));
    }
    return values.isEmpty() ? List.of() : List.copyOf(values);
  }

  private static Content toCommentContent(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return new Content("text/plain", "");
    }
    if (node.isValueNode()) {
      return new Content("text/plain", node.asText(""));
    }
    return new Content(defaultText(node, "type", "text/plain"), commentContentValue(node));
  }

  private static String commentContentValue(JsonNode node) {
    String value = nullableText(node, "value");
    if (value != null) {
      return value;
    }
    value = nullableText(node, "");
    if (value != null) {
      return value;
    }
    String markup = markup(node, List.of("type", "value"));
    return markup.isBlank() ? node.asText("") : markup;
  }

  private static @Nullable CommentContext toCommentContext(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    @Nullable Group group = node.has("group") ? toGroup(node.get("group"), "group") : null;
    @Nullable ResourceUri uri = node.has("uri") ? toResourceUri(node.get("uri")) : null;
    @Nullable String fragmentId = node.has("fragment") ? commentFragmentId(node.get("fragment")) : null;
    return new CommentContext(group, uri, fragmentId);
  }

  private static @Nullable String commentFragmentId(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isValueNode()) {
      return node.asText();
    }
    return nullableText(node, "id");
  }

  private static @Nullable CommentUser toCommentUser(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    @Nullable String firstname = nullableText(node, "firstname");
    @Nullable String surname = nullableText(node, "surname");
    @Nullable String fullname = nullableText(node, "fullname");
    if (fullname == null || fullname.isBlank()) {
      String combined = ((firstname == null ? "" : firstname) + " " + (surname == null ? "" : surname)).trim();
      fullname = combined;
    }
    long id = longValue(node, "id");
    Member member = id > 0L ? new Member(
        id,
        defaultText(node, "username", ""),
        nullableText(node, "email"),
        defaultText(node, "firstname", ""),
        defaultText(node, "surname", ""),
        MemberStatus.fromValue(nullableText(node, "status")),
        false,
        false,
        false,
        nullableText(node, "externalid"),
        offsetDateTime(node, "created"),
        offsetDateTime(node, "activated"),
        offsetDateTime(node, "lastpasswordchange"),
        offsetDateTime(node, "lastlogin"),
        false,
        offsetDateTime(node, "date")
    ) : null;
    return new CommentUser(member, fullname == null ? "" : fullname);
  }

  private static @Nullable CommentUser documentVersionAuthor(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    @Nullable String firstname = nullableText(node, "firstname");
    @Nullable String surname = nullableText(node, "surname");
    @Nullable String fullname = nullableText(node, "fullname");
    if (fullname == null || fullname.isBlank()) {
      fullname = ((firstname == null ? "" : firstname) + " " + (surname == null ? "" : surname)).trim();
    }
    long id = longValue(node, "id");
    @Nullable Member member = id > 0L
        ? new Member(id, "", null, defaultText(node, "firstname", ""), defaultText(node, "surname", ""))
        : null;
    return new CommentUser(member, fullname == null ? "" : fullname);
  }

  private static @Nullable StampedCommentUser toStampedCommentUser(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    CommentUser user = toCommentUser(node);
    if (user == null) {
      return null;
    }
    return new StampedCommentUser(
        user,
        offsetDateTime(node, "date")
    );
  }

  private static List<ResourceUri> attachments(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return List.of();
    }
    List<ResourceUri> attachments = new ArrayList<>();
    if (node.isArray()) {
      for (JsonNode item : node) {
        attachments.add(toAttachment(item));
      }
    } else {
      attachments.add(toAttachment(node));
    }
    return attachments;
  }

  private static <T> List<T> immutable(List<T> values) {
    return values.isEmpty() ? List.of() : List.copyOf(values);
  }

  private static ResourceUri toAttachment(@Nullable JsonNode node) {
    if (node != null && node.has("uri")) {
      return toResourceUri(node.get("uri"));
    }
    return toResourceUri(node);
  }

  private static MembershipDetail toMembershipDetail(JsonNode node) {
    String name = nullableText(node, "name");
    return new MembershipDetail(
        intValue(node, "position"),
        name == null ? "" : name,
        detailValue(node),
        booleanValue(node, "editable"),
        defaultText(node, "title", name),
        defaultText(node, "type", "text")
    );
  }

  private static String detailValue(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return "";
    }
    if (node.isValueNode()) {
      return node.asText("");
    }
    String value = nullableText(node, "value");
    if (value != null) {
      return value;
    }
    value = nullableText(node, "");
    return value == null ? node.asText("") : value;
  }

  private static String markup(@Nullable JsonNode node, List<String> ignoredFields) {
    if (node == null || node.isNull()) {
      return "";
    }
    if (node.isValueNode()) {
      return escapeXml(node.asText(""));
    }
    if (node.isArray()) {
      StringBuilder xml = new StringBuilder();
      for (JsonNode item : node) {
        xml.append(markup(item, ignoredFields));
      }
      return xml.toString();
    }
    StringBuilder xml = new StringBuilder();
    for (Map.Entry<String, JsonNode> field : node.properties()) {
      String name = field.getKey();
      if (ignoredFields.contains(name)) {
        continue;
      }
      if (name.isEmpty()) {
        xml.append(escapeXml(field.getValue().asText("")));
        continue;
      }
      appendMarkupElement(xml, name, field.getValue());
    }
    return xml.toString();
  }

  private static void appendMarkupElement(StringBuilder xml, String name, @Nullable JsonNode value) {
    if (value == null || value.isNull()) {
      return;
    }
    if (value.isArray()) {
      for (JsonNode item : value) {
        appendMarkupElement(xml, name, item);
      }
      return;
    }
    xml.append('<').append(name).append('>');
    if (value.isValueNode()) {
      xml.append(escapeXml(value.asText("")));
    } else {
      xml.append(markup(value, List.of()));
    }
    xml.append("</").append(name).append('>');
  }

  private static String escapeXml(String text) {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  private static String defaultText(JsonNode node, String field, String fallback) {
    String value = nullableText(node, field);
    return value == null ? fallback : value;
  }

  private static String stringValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      throw new ParsingException("Missing required PageSeeder field '" + field + "'");
    }
    return value;
  }

  private static @Nullable String nullableText(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value == null || value.isNull() ? null : value.asText();
  }

  private static long longValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return 0L;
    }
    return Long.parseLong(value);
  }

  private static @Nullable Long nullableLong(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return null;
    }
    return Long.parseLong(value);
  }

  private static int intValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return 0;
    }
    return Integer.parseInt(value);
  }

  private static @Nullable Integer nullableInteger(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return null;
    }
    return Integer.parseInt(value);
  }

  private static boolean booleanValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    return "true".equalsIgnoreCase(value);
  }

  private static boolean booleanValue(JsonNode node, String field, String alternativeField) {
    String value = nullableText(node, field);
    if (value == null) {
      value = nullableText(node, alternativeField);
    }
    return "true".equalsIgnoreCase(value);
  }
  private static @Nullable Boolean nullableBoolean(JsonNode node, String field) {
    String value = nullableText(node, field);
    return value == null || value.isBlank() ? null : Boolean.parseBoolean(value);
  }

  private static @Nullable OffsetDateTime offsetDateTime(JsonNode node, String field) {
    String value = nullableText(node, field);
    return value == null || value.isBlank() ? null : OffsetDateTime.parse(value);
  }

  private static @Nullable URI uri(JsonNode node, String field) {
    String value = nullableText(node, field);
    return value == null || value.isBlank() ? null : URI.create(value);
  }

  private static Duration secondsDuration(JsonNode node, String field) {
    String value = nullableText(node, field);
    return value == null || value.isBlank() ? Duration.ZERO : Duration.ofSeconds(Long.parseLong(value));
  }

  private static @Nullable GrantType grantType(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return GrantType.fromParameterValue(value);
    } catch (IllegalArgumentException ex) {
      return GrantType.valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
    }
  }
}
