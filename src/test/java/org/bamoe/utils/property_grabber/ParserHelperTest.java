/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.bamoe.utils.property_grabber;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class ParserHelperTest {

    @Test
    void getProperties() {
        var expected = """
               Config Name: "kogito.addon.tracing.decision.kafka.bootstrapAddress" | Description: kafka bootstrap server address | Type: string | Default:\s
               Config Name: "kogito.addon.tracing.decision.kafka.topic.name" | Description: name of the decision topic | Type: String | Default: Value of `kogito-tracing-decision`
               Config Name: "kogito.addon.tracing.decision.kafka.topic.partitions" | Description: number of decision topic partitions | Type: integer | Default: 1
               Config Name: "kogito.addon.tracing.decision.kafka.topic.replicationFactor" | Description: number of decision topic replication factor | Type: integer | Default: 1
               Config Name: "kogito.addon.tracing.decision.asyncEnabled" | Description: enable/disable asynchronous collection of decision events | Type: boolean | Default: true
               """;

        var result = ParserHelper.getProperties(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));

        assertEquals(expected, result);
    }

    @Test
    void getPropertiesAsAdoc() {
        var expected = """
                a| `"kogito.addon.tracing.decision.kafka.bootstrapAddress"`
                [.description]
                --
                kafka bootstrap server address
                --
                | string
                |\s
                a| `"kogito.addon.tracing.decision.kafka.topic.name"`
                [.description]
                --
                name of the decision topic
                --
                | String
                | Value of `kogito-tracing-decision`
                a| `"kogito.addon.tracing.decision.kafka.topic.partitions"`
                [.description]
                --
                number of decision topic partitions
                --
                | integer
                | 1
                a| `"kogito.addon.tracing.decision.kafka.topic.replicationFactor"`
                [.description]
                --
                number of decision topic replication factor
                --
                | integer
                | 1
                a| `"kogito.addon.tracing.decision.asyncEnabled"`
                [.description]
                --
                enable/disable asynchronous collection of decision events
                --
                | boolean
                | true
                """;

        var result = ParserHelper.getPropertiesAsAdoc(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));

        assertEquals(expected, result);
    }
}