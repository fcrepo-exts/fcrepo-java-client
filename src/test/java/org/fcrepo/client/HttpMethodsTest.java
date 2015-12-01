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

import static org.junit.Assert.assertEquals;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Aaron Coburn
 */
@RunWith(JUnit4.class)
public class HttpMethodsTest {

    @Test
    public void testMethods() {
        assertEquals(HttpMethods.DELETE.toString(), HttpDelete.METHOD_NAME);
        assertEquals(HttpMethods.GET.toString(), HttpGet.METHOD_NAME);
        assertEquals(HttpMethods.HEAD.toString(), HttpHead.METHOD_NAME);
        assertEquals(HttpMethods.OPTIONS.toString(), HttpOptions.METHOD_NAME);
        assertEquals(HttpMethods.PATCH.toString(), HttpPatch.METHOD_NAME);
        assertEquals(HttpMethods.POST.toString(), HttpPost.METHOD_NAME);
        assertEquals(HttpMethods.PUT.toString(), HttpPut.METHOD_NAME);
    }
}
