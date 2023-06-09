/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.java.query.dsl.path.index;

import com.couchbase.client.java.query.dsl.path.Path;

/**
 * On path in the primary Index creation DSL.
 *
 * @author Simon Baslé
 * @since 2.2
 */
public interface OnPrimaryPath extends Path {

    /**
     * Describes on which keyspace (bucket name) to index.
     *
     * @param keyspace the keyspace targeted (it will automatically be escaped).
     */
    UsingWithPath on(String keyspace);

    /**
     * Describes on which keyspace (bucket name) to index, also prefixing the keyspace with a namespace.
     *
     * @param namespace the optional namespace prefix for the keyspace (it will automatically be escaped).
     * @param keyspace  the keyspace targeted (it will automatically be escaped).
     */
    UsingWithPath on(String namespace, String keyspace);
}
