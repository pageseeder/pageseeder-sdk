/*
 * Copyright 2018 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.sdk.search;

/**
 * Constants for built-in PageSeeder index field names used in search queries.
 *
 * <p>Each constant is the field name with the {@code ps} prefix removed and converted to
 * upper case. Pass these to {@link Filter}, {@link RangeFilter}, and the search builder
 * methods that accept a field name.</p>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Fields {

  private Fields() {}

  /** Document type field (e.g. {@code default}, {@code references}). */
  public static final String TYPE = "pstype";

  /** Status field (e.g. {@code Approved}, {@code Draft}). */
  public static final String STATUS = "psstatus";

  /** Priority field (e.g. {@code High}, {@code Low}). */
  public static final String PRIORITY = "pspriority";

  /** Media type / MIME type field (e.g. {@code application/pdf}). */
  public static final String MEDIATYPE = "psmediatype";

  /** Assigned-to member field. */
  public static final String ASSIGNEDTO = "psassignedto";

  /** Folder path field. */
  public static final String FOLDER = "psfolder";

  /** PSML document type field. */
  public static final String DOCUMENTTYPE = "psdocumenttype";

  /** Publication or creation date field. */
  public static final String DATE = "psdate";

  /** Last-modified date field — use with range filters. */
  public static final String MODIFIEDDATE = "psmodifieddate";
}
