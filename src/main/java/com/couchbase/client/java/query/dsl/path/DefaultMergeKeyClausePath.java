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
package com.couchbase.client.java.query.dsl.path;

import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.element.Element;

import static com.couchbase.client.java.query.dsl.Expression.x;

/**
 * .
 *
 * @author Michael Nitschinger
 */
public class DefaultMergeKeyClausePath extends AbstractPath implements MergeKeyClausePath {

    public DefaultMergeKeyClausePath(AbstractPath parent) {
        super(parent);
    }

    @Override
    public MergeUpdatePath onKey(final Expression expression) {
        element(new Element() {
            @Override
            public String export() {
                return "ON KEY " + expression.toString();
            }
        });
        return new DefaultMergeUpdatePath(this);
    }

    @Override
    public MergeUpdatePath onPrimaryKey(final Expression expression) {
        element(new Element() {
            @Override
            public String export() {
                return "ON PRIMARY KEY " + expression.toString();
            }
        });
        return new DefaultMergeUpdatePath(this);
    }

    @Override
    public MergeUpdatePath onKey(String expression) {
        return onKey(x(expression));
    }

    @Override
    public MergeUpdatePath onPrimaryKey(String expression) {
        return onPrimaryKey(x(expression));
    }
}
