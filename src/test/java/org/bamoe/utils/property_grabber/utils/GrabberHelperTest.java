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
package org.bamoe.utils.property_grabber.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bamoe.utils.property_grabber.utils.GrabberHelper.ANNOTATION_NAME_MAP;
import static org.bamoe.utils.property_grabber.utils.GrabberHelper.KIE_PROPERTY_IMPORT;

class GrabberHelperTest {

    private static final String TESTING_CLASS_NAME = "BasicJavaClassWithFields";
    private static final String TESTING_CLASS_FILE = TESTING_CLASS_NAME + ".java";
    private static final String IF_BUILD_CLASS_NAME = "IfBuildPropertyJavaClass";
    private static final String IF_BUILD_CLASS_FILE = IF_BUILD_CLASS_NAME + ".java";
    private static final String UNLESS_BUILD_CLASS_NAME = "UnlessBuildPropertyJavaClass";
    private static final String UNLESS_BUILD_CLASS_FILE = UNLESS_BUILD_CLASS_NAME + ".java";
    private static final String CONDITIONAL_ON_PROPERTY_CLASS_NAME = "ConditionalOnPropertyJavaClass";
    private static final String CONDITIONAL_ON_PROPERTY_CLASS_FILE = CONDITIONAL_ON_PROPERTY_CLASS_NAME + ".java";
    private static final String SPRINGBOOT_CONFIG_CLASS = "SpringbootConfigClass";
    private static final String SPRINGBOOT_CONFIG_FILE = SPRINGBOOT_CONFIG_CLASS + ".java";
    private static final String QUARKUS_CONFIG_CLASS = "QuarkusConfigClass";
    private static final String QUARKUS_CONFIG_FILE = QUARKUS_CONFIG_CLASS + ".java";

    @Test
    void getProperties() {
        var expected = Arrays.asList(
                "Config Name: kogito.addon.tracing.decision.kafka.bootstrapAddress | Description: kafka bootstrap server address | Type: string | Default:\s",
                "Config Name: kogito.addon.tracing.decision.kafka.topic.name | Description: name of the decision topic | Type: String | Default: Value of `kogito-tracing-decision`",
                "Config Name: kogito.addon.tracing.decision.kafka.topic.partitions | Description: number of decision topic partitions | Type: integer | Default: 1",
                "Config Name: kogito.addon.tracing.decision.kafka.topic.replicationFactor | Description: number of decision topic replication factor | Type: integer | Default: 1",
                "Config Name: kogito.addon.tracing.decision.asyncEnabled | Description: enable/disable asynchronous collection of decision events | Type: boolean | Default: true",
                "Config Name: quarkus.kogito.data-index.graphql.ui.always-include | Description: Property used to instantiate String (only active when \"true\") | Type:  | Default: false",
                "Config Name: (Constants.MONITORING_RULE_USE_DEFAULT) | Description: Property used to instantiate String (only active when \"true\") | Type:  | Default:\s",
                "Config Name: kogito.data-index.blocking | Description: Property used to instantiate String (only active when not \"true\") | Type:  | Default: false",
                "Config Name: kogito.jobs-service.url | Description: Property used to instantiate String | Type:  | Default:\s",
                "Config Name: kogito.jobs-service.port | Description: Property used to instantiate String (only active when \"true\") | Type:  | Default: true",
                "Config Name: kogito.events.processinstances.enabled | Description: Property used to instantiate String | Type:  | Default: true");

        var result = GrabberHelper.getProperties(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        for (String expectedValue : expected) {
            assertThat(result).contains(expectedValue);
        }
    }

    @Test
    void getPropertiesAsAdoc() {
        var expected = Arrays.asList(
                "a| `kogito.addon.tracing.decision.kafka.bootstrapAddress`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "kafka bootstrap server address\n" +
                        "--\n" +
                        "| string\n" +
                        "| ",
                "a| `kogito.addon.tracing.decision.kafka.topic.name`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "name of the decision topic\n" +
                        "--\n" +
                        "| String\n" +
                        "| Value of `kogito-tracing-decision`",
                "a| `kogito.addon.tracing.decision.kafka.topic.partitions`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "number of decision topic partitions\n" +
                        "--\n" +
                        "| integer\n" +
                        "| 1",
                "a| `kogito.addon.tracing.decision.kafka.topic.replicationFactor`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "number of decision topic replication factor\n" +
                        "--\n" +
                        "| integer\n" +
                        "| 1",
                "a| `kogito.addon.tracing.decision.asyncEnabled`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "enable/disable asynchronous collection of decision events\n" +
                        "--\n" +
                        "| boolean\n" +
                        "| true",
                "a| `quarkus.kogito.data-index.graphql.ui.always-include`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String (only active when \"true\")\n" +
                        "--\n" +
                        "| \n" +
                        "|",
                "a| `(Constants.MONITORING_RULE_USE_DEFAULT)`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String (only active when \"true\")\n" +
                        "--\n" +
                        "| \n" +
                        "|",
                "a| `kogito.data-index.blocking`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String (only active when not \"true\")\n" +
                        "--\n" +
                        "| \n" +
                        "|",
                "a| `kogito.jobs-service.url`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String\n" +
                        "--\n" +
                        "| \n" +
                        "|",
                "a| `kogito.jobs-service.port`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String (only active when \"true\")\n" +
                        "--\n" +
                        "| \n" +
                        "| true",
                "a| `kogito.events.processinstances.enabled`\n" +
                        "[.description]\n" +
                        "--\n" +
                        "Property used to instantiate String\n" +
                        "--\n" +
                        "| \n" +
                        "| true");
        var result = GrabberHelper.getPropertiesAsAdoc(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        for (String expectedValue : expected) {
            assertThat(result).contains(expectedValue);
        }
    }

    @Test
    void getPropertiesFromSpringbootConfigClass() {
        var expected = Arrays.asList(
                "Config Name: kie.flyway.isEnabled | Description:  | Type:  | Default:\s",
                "Config Name: kie.flyway.modules | Description:  | Type:  | Default:\s");

        var result = GrabberHelper.getProperties(Path.of("src", "test", "resources", SPRINGBOOT_CONFIG_FILE));
        for (String expectedValue : expected) {
            assertThat(result).contains(expectedValue);
        }
    }

    @Test
    void getPropertiesFromQuarkusConfigClass() {
        var expected = Arrays.asList(
                "Config Name: kie.flyway.isEnabled | Description:  | Type:  | Default: false",
                "Config Name: kie.flyway.modules | Description:  | Type:  | Default:\s");

        var result = GrabberHelper.getProperties(Path.of("src", "test", "resources", QUARKUS_CONFIG_FILE));
        for (String expectedValue : expected) {
            assertThat(result).contains(expectedValue);
        }
    }

    @Test
    void getApplicationPropertyFields() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(TESTING_CLASS_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(TESTING_CLASS_NAME + " not found"));
        Collection<FieldDeclaration> retrieved = GrabberHelper.getApplicationPropertyFields(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull();
        retrieved.forEach(this::checkApplicationPropertyField);
    }

    @Test
    void getConfigClassAnnotation() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", SPRINGBOOT_CONFIG_FILE));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(SPRINGBOOT_CONFIG_CLASS))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(SPRINGBOOT_CONFIG_CLASS + " not found"));
        Optional<AnnotationExpr> retrieved = GrabberHelper.getConfigClassAnnotation(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get().getNameAsString()).isEqualTo("ConfigurationProperties");
        //--
        compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", QUARKUS_CONFIG_FILE));
        assertThat(compilationUnit).isNotNull();
        classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(QUARKUS_CONFIG_CLASS))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(QUARKUS_CONFIG_CLASS + " not found"));
        retrieved = GrabberHelper.getConfigClassAnnotation(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get().getNameAsString()).isEqualTo("ConfigMapping");
    }

    @Test
    void getApplicationPropertiesFromConfigClass() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", SPRINGBOOT_CONFIG_FILE));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(SPRINGBOOT_CONFIG_CLASS))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(SPRINGBOOT_CONFIG_CLASS + " not found"));
        List<MethodDeclaration> retrieved = GrabberHelper.getApplicationPropertyFieldsFromConfigClass(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved).hasSize(2);
        //--
        compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", QUARKUS_CONFIG_FILE));
        assertThat(compilationUnit).isNotNull();
        classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(QUARKUS_CONFIG_CLASS))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(QUARKUS_CONFIG_CLASS + " not found"));
        retrieved = GrabberHelper.getApplicationPropertyFieldsFromConfigClass(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved).hasSize(2);
    }

    @Test
    void getApplicationPropertyAnnotationsFromMethods() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(TESTING_CLASS_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(TESTING_CLASS_NAME + " not found"));
        Map<AnnotationExpr, Node> retrieved = GrabberHelper.getApplicationPropertyAnnotationsFromMethods(classOrInterfaceDeclaration);
        final int EXPECTED_DISCOVERED_METHODS = 6; // Magic number: it must reflect what is written inside BasicJavaClassWithFields.java
        assertThat(retrieved).isNotNull().hasSize(EXPECTED_DISCOVERED_METHODS);
        retrieved.keySet().forEach(this::checkApplicationPropertyAnnotation);
    }

    @Test
    void getApplicationPropertyAnnotationsFromClass() {
        commonGetApplicationPropertyAnnotationsFromClass(IF_BUILD_CLASS_FILE, IF_BUILD_CLASS_NAME);
        commonGetApplicationPropertyAnnotationsFromClass(UNLESS_BUILD_CLASS_FILE, UNLESS_BUILD_CLASS_NAME);
        commonGetApplicationPropertyAnnotationsFromClass(CONDITIONAL_ON_PROPERTY_CLASS_FILE, CONDITIONAL_ON_PROPERTY_CLASS_NAME);
    }

    @Test
    void getAnnotatedValueFromMethodWithNormalAnnotation() {
        AnnotationExpr annotationExpr = StaticJavaParser.parseAnnotation("@KieProperty(name = \"test\")");
        MethodDeclaration methodDeclaration = new MethodDeclaration();
        methodDeclaration.setAnnotations(NodeList.nodeList(annotationExpr));
        Optional<String> retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, "KieProperty", "name");
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get()).isEqualTo("test");
        assertThatThrownBy(() -> GrabberHelper.getAnnotatedValue(methodDeclaration, "KieProperty",null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("annotationProperty cannot be null");
        //--
        retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, "NotAnnotation","name");
        assertThat(retrieved).isNotNull().isNotPresent();
        //--
        retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, null,"name");
        assertThat(retrieved).isNotNull().isNotPresent();
        //--
        assertThatThrownBy(() -> GrabberHelper.getAnnotatedValue(methodDeclaration, "KieProperty","notexistingkey"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No matching pair: @KieProperty(name = \"test\") for annotationProperty: notexistingkey");
    }

    @Test
    void getAnnotatedValueFromMethodWithSingleMemberAnnotation() {
        AnnotationExpr annotationExpr = StaticJavaParser.parseAnnotation("@KieProperty(\"test\")");
        MethodDeclaration methodDeclaration = new MethodDeclaration();
        methodDeclaration.setAnnotations(NodeList.nodeList(annotationExpr));
        //--
        Optional<String> retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, "KieProperty",null);
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get()).isEqualTo("test");
        //--
        retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, "NotAnnotation","name");
        assertThat(retrieved).isNotNull().isNotPresent();
        //--
        retrieved = GrabberHelper.getAnnotatedValue(methodDeclaration, null,"name");
        assertThat(retrieved).isNotNull().isNotPresent();
        //--
        assertThatThrownBy(() -> GrabberHelper.getAnnotatedValue(methodDeclaration, "KieProperty","notexistingkey"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("annotationProperty must be null");

    }

    @Test
    void getAnnotatedValueFromAnnotation() {
        AnnotationExpr annotationExpr = StaticJavaParser.parseAnnotation("@KieProperty(name = \"test\")");
        String retrieved = GrabberHelper.getAnnotatedValue(annotationExpr, "name");
        assertThat(retrieved).isEqualTo("test");
        assertThatThrownBy(() -> GrabberHelper.getAnnotatedValue(annotationExpr, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("annotationProperty cannot be null");
        assertThatThrownBy(() -> GrabberHelper.getAnnotatedValue(annotationExpr, "notexistingkey"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No matching pair: @KieProperty(name = \"test\") for annotationProperty: notexistingkey");
    }

    private void commonGetApplicationPropertyAnnotationsFromClass(String javaSource, String className) {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", javaSource));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(className))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(className + " not found"));
        Map<AnnotationExpr, Node> retrieved = GrabberHelper.getApplicationPropertyAnnotationsFromClass(classOrInterfaceDeclaration);
        assertThat(retrieved).isNotNull().hasSize(1);
        retrieved.keySet().forEach(this::checkApplicationPropertyAnnotation);
    }

    private void commonCheckImportDeclaration(CompilationUnit toCheck) {
        assertThat(toCheck.findAll(ImportDeclaration.class)
                .stream()
                .anyMatch(importDeclaration -> KIE_PROPERTY_IMPORT.equals(importDeclaration.getNameAsString())))
                .isTrue();
    }


    private void checkApplicationPropertyField(FieldDeclaration toCheck) {
        assertThat(toCheck).isNotNull()
                .matches(FieldDeclaration::isStatic)
                .matches(FieldDeclaration::isPublic)
                .matches(FieldDeclaration::isFinal)
                .matches(this::hasExactlyOneStringInitializer)
                .matches(it -> it.getJavadoc().isPresent());
    }

    private void checkApplicationPropertyAnnotation(AnnotationExpr toCheck) {
        assertThat(toCheck).isNotNull()
                .matches(annotationExpr -> ANNOTATION_NAME_MAP.containsKey(annotationExpr.getNameAsString()));
    }

    private boolean hasExactlyOneStringInitializer(FieldDeclaration toCheck) {
        return toCheck.getVariables().size() == 1 &&
                toCheck.getVariable(0).getInitializer().isPresent() &&
                toCheck.getVariable(0).getInitializer().get() instanceof StringLiteralExpr;
    }
}