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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Request builder which includes a body component
 * 
 * @author bbpennel
 */
public abstract class BodyRequestBuilder<T extends BodyRequestBuilder<T>> extends
        RequestBuilder<BodyRequestBuilder<T>> {

    protected InputStream bodyStream;

    protected String contentType;

    protected abstract T self();

    /**
     * Add a body to this request from a stream, with application/octet-stream as its content type
     * 
     * @param stream
     * @return
     */
    public T body(InputStream stream) {
        return body(stream, null);
    }

    /**
     * Add a body to this request as a stream with the given content type
     * 
     * @param stream
     * @param contentType
     * @return
     */
    public T body(InputStream stream, String contentType) {
        this.bodyStream = stream;
        if (contentType == null) {
            this.contentType = "application/octet-stream";
        } else {
            this.contentType = contentType;
        }

        return self();
    }

    /**
     * Add the given file as the body for this request with the provided content type
     * 
     * @param file
     * @param contentType
     * @return
     * @throws IOException
     */
    public T body(File file, String contentType) throws IOException {
        return body(new FileInputStream(file), contentType);
    }
}
