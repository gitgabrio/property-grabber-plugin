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
                
                kogito.addon.tracing.decision.kafka.bootstrapAddress ->  (string) kafka bootstrap server address
                kogito.addon.tracing.decision.kafka.topic.name ->  (string) name of the decision topic; default to kogito-tracing-decision
                kogito.addon.tracing.decision.kafka.topic.partitions ->  (integer) number of decision topic partitions; default to 1
                kogito.addon.tracing.decision.kafka.topic.replicationFactor ->  (integer) number of decision topic replication factor; default to 1
                kogito.addon.tracing.decision.asyncEnabled ->  (boolean) enable/disable asynchronous collection of decision events; default to true""";

        var result = ParserHelper.getProperties(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));

        assertEquals(expected, result);
    }
}