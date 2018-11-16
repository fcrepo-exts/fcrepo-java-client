/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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
 * Helper constants for resource types used in the Fedora specification.
 *
 * @author bbpennel
 */
public class FedoraTypes {

    // Type representing a Memento TimeGate
    public final static String MEMENTO_TIME_GATE_TYPE = "http://mementoweb.org/ns#TimeGate";

    // Type representing a Memento TimeMap (LDPCv)
    public final static String MEMENTO_TIME_MAP_TYPE = "http://mementoweb.org/ns#TimeMap";

    // Type representing a Memento original resource (LDPRv)
    public final static String MEMENTO_ORIGINAL_TYPE = "http://mementoweb.org/ns#OriginalResource";

    // Type representing a Memento (LDPRm)
    public final static String MEMENTO_TYPE = "http://mementoweb.org/ns#Memento";

    private FedoraTypes() {
    }
}
