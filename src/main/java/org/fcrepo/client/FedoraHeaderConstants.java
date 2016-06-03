/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.client;

/**
 * Header constants used in calls to the Fedora API
 * 
 * @author bbpennel
 */
public class FedoraHeaderConstants {

    public static final String DESCRIBED_BY = "describedby";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String SLUG = "Slug";

    public static final String DIGEST = "digest";

    public static final String LOCATION = "Location";

    public static final String ACCEPT = "Accept";

    public static final String PREFER = "Prefer";

    public static final String RANGE = "Range";

    public static final String IF_NONE_MATCH = "If-None-Match";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String IF_MATCH = "If-Match";

    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    public static final String DESTINATION = "Destination";

    private FedoraHeaderConstants() {
    }
}
