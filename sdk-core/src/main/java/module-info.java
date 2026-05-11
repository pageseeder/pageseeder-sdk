/**
 * Core PageSeeder SDK module.
 */
module org.pageseeder.sdk.core {
    requires java.net.http;
    requires java.xml;
    requires org.slf4j;
    requires static org.jspecify;

    exports org.pageseeder.sdk;
    exports org.pageseeder.sdk.auth;
    exports org.pageseeder.sdk.client;
    exports org.pageseeder.sdk.exception;
    exports org.pageseeder.sdk.oauth;
    exports org.pageseeder.sdk.service;
    exports org.pageseeder.sdk.xml.sax;
    exports org.pageseeder.sdk.xml.stax;
}
