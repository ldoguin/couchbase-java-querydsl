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

import com.couchbase.client.java.query.dsl.Expression;

public class InsertValueElement implements Element {

  private final InsertPosition position;
  private final Expression id;
  private final Expression value;

  public InsertValueElement(InsertPosition position, Expression id, Expression value) {
    this.position = position;
    this.id = id;
    this.value = value;
  }

  @Override
  public String export() {
    return position.repr + "(" + id.toString() + ", " + value.toString() + ")";
  }

  public enum InsertPosition {
    INITIAL("VALUES "),
    NOT_INITIAL(", ");

    private final String repr;

    InsertPosition(String repr) {
      this.repr = repr;
    }
  }

}
