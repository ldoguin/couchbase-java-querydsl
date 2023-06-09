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

import com.couchbase.client.java.query.dsl.element.IndexElement;
import com.couchbase.client.java.query.dsl.path.AbstractPath;

/**
 * See {@link CreateIndexPath}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
public class DefaultCreateIndexPath extends AbstractPath implements CreateIndexPath {

    public DefaultCreateIndexPath() {
        super(null);
    }

    @Override
    public OnPath create(String indexName) {
        element(new IndexElement(indexName));
        return new DefaultOnPath(this);
    }

    @Override
    public OnPrimaryPath createPrimary() {
        element(new IndexElement());
        return new DefaultOnPrimaryPath(this);
    }

    @Override
    public OnPrimaryPath createPrimary(String customPrimaryName) {
        element(new IndexElement(customPrimaryName, true));
        return new DefaultOnPrimaryPath(this);
    }
}
