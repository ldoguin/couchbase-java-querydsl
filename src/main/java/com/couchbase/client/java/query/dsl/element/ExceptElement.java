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
package com.couchbase.client.java.query.dsl.element;


import com.couchbase.client.java.query.Statement;


public class ExceptElement implements Element {
    private final boolean all;
    private final String with;

    public ExceptElement(final boolean all) {
        this.all = all;
        this.with = null;
    }

    public ExceptElement(final boolean all, final String with) {
        this.all = all;
        this.with = with;
    }

    public ExceptElement(final boolean all, final Statement with) {
        this.all = all;
        this.with = with.toString();
    }

    @Override
    public String export() {
        final StringBuilder sb = new StringBuilder();

        sb.append("EXCEPT");

        if (all) {
            sb.append(" ALL");
        }

        if (!(with == null || with.isEmpty())) {
            sb.append(" ");
            sb.append(with);
        }

        return sb.toString();

    }
}
