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

import java.nio.file.Path;
import java.util.Collection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserHelperTest {

    private static final String TESTING_CLASS_NAME = "BasicJavaClassWithFields";

    @Test
    void getProperties() {
        var expected = """
                Config Name: kogito.addon.tracing.decision.kafka.bootstrapAddress | Description: kafka bootstrap server address | Type: string | Default:\s
                Config Name: kogito.addon.tracing.decision.kafka.topic.name | Description: name of the decision topic | Type: String | Default: Value of `kogito-tracing-decision`
                Config Name: kogito.addon.tracing.decision.kafka.topic.partitions | Description: number of decision topic partitions | Type: integer | Default: 1
                Config Name: kogito.addon.tracing.decision.kafka.topic.replicationFactor | Description: number of decision topic replication factor | Type: integer | Default: 1
                Config Name: kogito.addon.tracing.decision.asyncEnabled | Description: enable/disable asynchronous collection of decision events | Type: boolean | Default: true
                """;

        var result = ParserHelper.getProperties(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPropertiesAsAdoc() {
        var expected = """
                a| `kogito.addon.tracing.decision.kafka.bootstrapAddress`
                [.description]
                --
                kafka bootstrap server address
                --
                | string
                |\s
                a| `kogito.addon.tracing.decision.kafka.topic.name`
                [.description]
                --
                name of the decision topic
                --
                | String
                | Value of `kogito-tracing-decision`
                a| `kogito.addon.tracing.decision.kafka.topic.partitions`
                [.description]
                --
                number of decision topic partitions
                --
                | integer
                | 1
                a| `kogito.addon.tracing.decision.kafka.topic.replicationFactor`
                [.description]
                --
                number of decision topic replication factor
                --
                | integer
                | 1
                a| `kogito.addon.tracing.decision.asyncEnabled`
                [.description]
                --
                enable/disable asynchronous collection of decision events
                --
                | boolean
                | true
                """;

        var result = ParserHelper.getPropertiesAsAdoc(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getApplicationPropertyFields() {
        CompilationUnit compilationUnit = ParserHelper.getCompilationUnit(Path.of("src", "test", "resources", "BasicJavaClassWithFields.java"));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(TESTING_CLASS_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(TESTING_CLASS_NAME + " not found"));
        Collection<FieldDeclaration> retrieved = ParserHelper.getApplicationPropertyFields(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull();
        retrieved.forEach(this::checkApplicationPropertyField);
    }

    private void checkApplicationPropertyField(FieldDeclaration toCheck) {
        assertThat(toCheck).isNotNull()
                .matches(FieldDeclaration::isStatic)
                .matches(FieldDeclaration::isPublic)
                .matches(FieldDeclaration::isFinal)
                .matches(this::hasExactlyOneStringInitializer)
                .matches(it -> it.getJavadoc().isPresent());
    }

    private boolean hasExactlyOneStringInitializer(FieldDeclaration toCheck) {
        return toCheck.getVariables().size() == 1 &&
                toCheck.getVariable(0).getInitializer().isPresent() &&
                toCheck.getVariable(0).getInitializer().get() instanceof StringLiteralExpr;
    }
}