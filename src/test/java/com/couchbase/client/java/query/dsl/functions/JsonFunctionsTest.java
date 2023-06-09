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

package com.couchbase.client.java.query.dsl.functions;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.dsl.Expression;
import org.junit.jupiter.api.Test;

import static com.couchbase.client.java.query.dsl.Expression.x;
import static com.couchbase.client.java.query.dsl.functions.JsonFunctions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFunctionsTest {

    @Test
    public void testDecodeJson() throws Exception {
        Expression e1 = decodeJson(x("jsonContainingField"));
        Expression e2 = decodeJson("{\"test\": true}");
        Expression e3 = decodeJson(JsonObject.create().put("test", true));

        assertEquals("DECODE_JSON(jsonContainingField)", e1.toString());
        assertEquals("DECODE_JSON(\"{\\\"test\\\":true}\")", e2.toString());
        assertEquals("DECODE_JSON(\"{\\\"test\\\":true}\")", e3.toString());
    }

    @Test
    public void testEncodeJson() throws Exception {
        Expression e1 = encodeJson(x(1));
        Expression e2 = encodeJson("1");

        assertEquals(e1.toString(), e2.toString());
        assertEquals("ENCODE_JSON(1)", e1.toString());
    }

    @Test
    public void testEncodedSize() throws Exception {
        Expression e1 = encodedSize(x(1));
        Expression e2 = encodedSize("1");

        assertEquals(e1.toString(), e2.toString());
        assertEquals("ENCODED_SIZE(1)", e1.toString());
    }

    @Test
    public void testPolyLength() throws Exception {
        Expression e1 = polyLength(x(1));
        Expression e2 = polyLength("1");

        assertEquals(e1.toString(), e2.toString());
        assertEquals("POLY_LENGTH(1)", e1.toString());
    }
}