package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.CommentContext;
import org.pageseeder.sdk.model.CommentUser;
import org.pageseeder.sdk.model.Content;
import org.pageseeder.sdk.model.Group;
import org.pageseeder.sdk.model.GroupType;
import org.pageseeder.sdk.model.Member;
import org.pageseeder.sdk.model.MemberStatus;
import org.pageseeder.sdk.model.Membership;
import org.pageseeder.sdk.model.MembershipDetail;
import org.pageseeder.sdk.model.MembershipStatus;
import org.pageseeder.sdk.model.NotificationPreference;
import org.pageseeder.sdk.model.ResourceUri;
import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.model.StampedCommentUser;
import org.pageseeder.sdk.model.GroupRole;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.Version;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

final class PageSeederParsers {

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
    JsonNode listNode = findListNode(root, alias(type));
    List<T> items = new ArrayList<>();
    if (listNode == null || listNode.isMissingNode() || listNode.isNull()) {
      return items;
    }
    if (listNode.isArray()) {
      for (JsonNode itemNode : listNode) {
        items.add(mapNode(itemNode, type));
      }
    } else {
      items.add(mapNode(listNode, type));
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
    String id = text(error, "id");
    String message = text(error, "message");
    if (message == null && error.has("description")) {
      message = text(error, "description");
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

  @SuppressWarnings("unchecked")
  private static <T> T mapNode(JsonNode node, Class<T> type) {
    return mapNode(node, type, null);
  }

  @SuppressWarnings("unchecked")
  private static <T> T mapNode(JsonNode node, Class<T> type, String rootElementName) {
    if (type == Member.class) {
      return (T) toMember(node);
    }
    if (type == Comment.class) {
      return (T) toComment(node);
    }
    if (type == Group.class) {
      return (T) toGroup(node, rootElementName);
    }
    if (type == Membership.class) {
      return (T) toMembership(node);
    }
    if (type == ResourceUri.class) {
      return (T) toResourceUri(node);
    }
    if (type == Version.class) {
      return (T) toVersion(node);
    }
    throw new ParsingException("Unsupported PageSeeder mapping type: " + type.getName());
  }

  private static JsonNode findListNode(JsonNode root, String alias) {
    if (root.has("result")) {
      return findListNode(root.get("result"), alias);
    }
    if (root.has(alias)) {
      return root.get(alias);
    }
    if (root.has(alias + "s")) {
      return root.get(alias + "s");
    }
    if (root.isArray()) {
      return root;
    }
    return root.has(alias) ? root.get(alias) : null;
  }

  private static String alias(Class<?> type) {
    if (type == Member.class) return "member";
    if (type == Comment.class) return "comment";
    if (type == Group.class) return "group";
    if (type == Membership.class) return "membership";
    if (type == ResourceUri.class) return "uri";
    if (type == Version.class) return "version";
    return type.getSimpleName().toLowerCase();
  }

  private static Member toMember(JsonNode node) {
    JsonNode source = unwrap(node, "member");
    return new Member(
        longValue(source, "id"),
        stringValue(source, "username"),
        nullableText(source, "email"),
        nullableText(source, "firstname"),
        nullableText(source, "surname"),
        MemberStatus.fromValue(nullableText(source, "status")),
        booleanValue(source, "locked"),
        booleanValue(source, "onvacation"),
        booleanValue(source, "attachments"),
        offsetDateTime(source, "lastlogin")
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

  private static Group toGroup(JsonNode node) {
    return toGroup(node, null);
  }

  private static Group toGroup(JsonNode node, String rootElementName) {
    JsonNode source = unwrap(node, "group");
    GroupType type = rootElementName != null
        ? GroupType.fromValue(rootElementName)
        : GroupType.fromValue(nullableText(source, "type"));
    return new Group(
        longValue(source, "id"),
        stringValue(source, "name"),
        type,
        nullableText(source, "title"),
        nullableText(source, "description"),
        nullableText(source, "owner"),
        GroupRole.fromValue(nullableText(source, "defaultrole")),
        NotificationPreference.fromValue(nullableText(source, "defaultnotify"))
    );
  }

  private static Membership toMembership(JsonNode node) {
    JsonNode source = unwrap(node, "membership");
    Member member = source.has("member") ? toMember(source.get("member")) : null;
    Group group = source.has("group")
        ? toGroup(source.get("group"), "group")
        : source.has("project") ? toGroup(source.get("project"), "project") : null;
    return new Membership(
        longValue(source, "id"),
        member,
        group,
        GroupRole.fromValue(nullableText(source, "role")),
        MembershipStatus.fromValue(nullableText(source, "status")),
        NotificationPreference.fromValue(nullableText(source, "notification")),
        booleanValue(source, "email-listed"),
        offsetDateTime(source, "created"),
        details(source.get("details"))
    );
  }

  private static ResourceUri toResourceUri(JsonNode node) {
    JsonNode source = unwrap(node, "uri");
    return new ResourceUri(
        longValue(source, "id"),
        nullableText(source, "scheme"),
        nullableText(source, "host"),
        intValue(source, "port"),
        nullableText(source, "path"),
        nullableText(source, "title"),
        nullableText(source, "docid"),
        nullableText(source, "description"),
        nullableText(source, "mediatype"),
        offsetDateTime(source, "created"),
        offsetDateTime(source, "modified"),
        labels(source.get("labels")),
        booleanValue(source, "external"),
        booleanValue(source, "folder")
    );
  }

  private static Version toVersion(JsonNode node) {
    JsonNode source = unwrap(node, "version");
    return new Version(
        intValue(source, "major"),
        intValue(source, "build"),
        nullableText(source, "string")
    );
  }

  private static JsonNode unwrap(JsonNode node, String alias) {
    if (node == null || node.isNull()) {
      return node;
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

  private static String rootElementName(ObjectMapper mapper, byte[] body) {
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

  private static XMLInputFactory newXmlInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    return factory;
  }

  private static List<String> labels(JsonNode node) {
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

  private static List<MembershipDetail> details(JsonNode node) {
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

  private static List<Content> contents(JsonNode node) {
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

  private static Content toCommentContent(JsonNode node) {
    if (node == null || node.isNull()) {
      return new Content("text/plain", "");
    }
    if (node.isValueNode()) {
      return new Content("text/plain", node.asText(""));
    }
    return new Content(nullableText(node, "type"), commentContentValue(node));
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

  private static CommentContext toCommentContext(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    Group group = node.has("group") ? toGroup(node.get("group"), "group") : null;
    ResourceUri uri = node.has("uri") ? toResourceUri(node.get("uri")) : null;
    String fragmentId = node.has("fragment") ? commentFragmentId(node.get("fragment")) : null;
    return new CommentContext(group, uri, fragmentId);
  }

  private static String commentFragmentId(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isValueNode()) {
      return node.asText();
    }
    return nullableText(node, "id");
  }

  private static CommentUser toCommentUser(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    String firstname = nullableText(node, "firstname");
    String surname = nullableText(node, "surname");
    String fullname = nullableText(node, "fullname");
    if (fullname == null || fullname.isBlank()) {
      String combined = ((firstname == null ? "" : firstname) + " " + (surname == null ? "" : surname)).trim();
      fullname = combined;
    }
    long id = longValue(node, "id");
    Member member = id > 0L ? new Member(
        id,
        defaultText(node, "username", ""),
        nullableText(node, "email"),
        firstname,
        surname,
        MemberStatus.fromValue(nullableText(node, "status")),
        false,
        false,
        false,
        null
    ) : null;
    return new CommentUser(member, fullname);
  }

  private static StampedCommentUser toStampedCommentUser(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    return new StampedCommentUser(
        toCommentUser(node),
        offsetDateTime(node, "date")
    );
  }

  private static List<ResourceUri> attachments(JsonNode node) {
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

  private static ResourceUri toAttachment(JsonNode node) {
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

  private static String detailValue(JsonNode node) {
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

  private static String markup(JsonNode node, List<String> ignoredFields) {
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
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      String name = field.getKey();
      if (ignoredFields.contains(name)) {
        continue;
      }
      if ("".equals(name)) {
        xml.append(escapeXml(field.getValue().asText("")));
        continue;
      }
      appendMarkupElement(xml, name, field.getValue());
    }
    return xml.toString();
  }

  private static void appendMarkupElement(StringBuilder xml, String name, JsonNode value) {
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

  private static String nullableText(JsonNode node, String field) {
    return text(node, field);
  }

  private static String text(JsonNode node, String field) {
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

  private static int intValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    if (value == null || value.isBlank()) {
      return 0;
    }
    return Integer.parseInt(value);
  }

  private static boolean booleanValue(JsonNode node, String field) {
    String value = nullableText(node, field);
    return "true".equalsIgnoreCase(value);
  }

  private static OffsetDateTime offsetDateTime(JsonNode node, String field) {
    String value = nullableText(node, field);
    return value == null || value.isBlank() ? null : OffsetDateTime.parse(value);
  }
}
