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

import com.couchbase.client.java.query.dsl.element.OnElement;
import com.couchbase.client.java.query.dsl.path.AbstractPath;

/**
 * See {@link OnPrimaryPath}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
public class DefaultOnPrimaryPath extends AbstractPath implements OnPrimaryPath {

    public DefaultOnPrimaryPath(AbstractPath parent) {
        super(parent);
    }

    @Override
    public UsingWithPath on(String namespace, String keyspace) {
        element(new OnElement(namespace, keyspace, null, null));
        return new DefaultUsingWithPath(this);
    }

    @Override
    public UsingWithPath on(String keyspace) {
        return on(null, keyspace);
    }
}
