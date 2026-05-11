/**
 * PageSeeder SDK model and codec module.
 */
module org.pageseeder.sdk.model {
  requires transitive org.pageseeder.sdk.core;
  requires transitive com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.xml;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires java.xml;
  requires static org.jspecify;

  exports org.pageseeder.sdk.model;
  exports org.pageseeder.sdk.model.codec;
  exports org.pageseeder.sdk.model.util;
}
