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

import java.net.URI;

/**
 * Builds a request to copy a resource (and its subtree) to a new location
 * 
 * @author bbpennel
 */
public class CopyBuilder<T extends CopyBuilder<T>> extends MoveBuilder<CopyBuilder<T>> {

    protected CopyBuilder(URI sourceUrl, URI destinationUrl, FcrepoClient client) {
        super(sourceUrl, destinationUrl, client);
        this.method = HttpMethods.COPY;
    }

    @Override
    protected CopyBuilder<T> self() {
        return this;
    }
}
