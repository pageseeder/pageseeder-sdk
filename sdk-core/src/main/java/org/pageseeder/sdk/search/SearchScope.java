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

import java.util.List;
import java.util.Objects;

/**
 * Defines where a search is executed.
 *
 * <p>Use the static factory methods to create a scope:</p>
 * <ul>
 *   <li>{@link #group(String)} — search within a single group</li>
 *   <li>{@link #project(String, String)} — search within a project</li>
 *   <li>{@link #project(String, String, List)} — search within specific groups of a project</li>
 *   <li>{@link #global(String)} — search all content accessible to the member</li>
 * </ul>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public sealed interface SearchScope permits SearchScope.Group, SearchScope.Project, SearchScope.Global {

  /**
   * A scope targeting a single group.
   *
   * @param group The name of the group.
   */
  record Group(String group) implements SearchScope {
    /**
     * Creates a group search scope.
     *
     * @param group The group name.
     */
    public Group {
      Objects.requireNonNull(group, "group");
    }
  }

  /**
   * A scope targeting a project, optionally restricted to specific groups within it.
   *
   * @param project The name of the project.
   * @param member  The member making the search (required for project-level access).
   * @param groups  Specific groups within the project to search; empty means all groups.
   */
  record Project(String project, String member, List<String> groups) implements SearchScope {
    /**
     * Creates a project search scope.
     *
     * @param project The project name.
     * @param member  The member making the search.
     * @param groups  Specific groups within the project to search.
     */
    public Project {
      Objects.requireNonNull(project, "project");
      Objects.requireNonNull(member, "member");
      groups = List.copyOf(groups);
    }
  }

  /**
   * A scope covering all content the member has access to, across all groups and projects.
   *
   * @param member The member making the search.
   */
  record Global(String member) implements SearchScope {
    /**
     * Creates a member-wide search scope.
     *
     * @param member The member making the search.
     */
    public Global {
      Objects.requireNonNull(member, "member");
    }
  }

  /**
   * @param group The group name.
   * @return A scope targeting the specified group.
   */
  static SearchScope group(String group) {
    return new Group(group);
  }

  /**
   * @param project The project name.
   * @param member  The member making the search.
   * @return A scope targeting all groups in the specified project.
   */
  static SearchScope project(String project, String member) {
    return new Project(project, member, List.of());
  }

  /**
   * @param project The project name.
   * @param member  The member making the search.
   * @param groups  The groups within the project to search.
   * @return A scope targeting specific groups within the specified project.
   */
  static SearchScope project(String project, String member, List<String> groups) {
    return new Project(project, member, groups);
  }

  /**
   * @param project The project name.
   * @param member  The member making the search.
   * @param groups  The groups within the project to search.
   * @return A scope targeting specific groups within the specified project.
   */
  static SearchScope project(String project, String member, String... groups) {
    return new Project(project, member, List.of(groups));
  }

  /**
   * @param member The member making the search.
   * @return A scope covering all content accessible to the specified member.
   */
  static SearchScope global(String member) {
    return new Global(member);
  }
}
