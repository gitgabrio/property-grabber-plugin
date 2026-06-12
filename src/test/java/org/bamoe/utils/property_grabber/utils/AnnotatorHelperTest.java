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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bamoe.utils.property_grabber.utils.AnnotatorHelper.KIE_PROPERTY_ANNOTATION;
import static org.bamoe.utils.property_grabber.utils.AnnotatorHelper.KIE_PROPERTY_IMPORT;

class AnnotatorHelperTest {

    private static final String TESTING_CLASS_NAME = "BasicJavaClassWithFields";
    private static final String TESTING_CLASS_FILE = TESTING_CLASS_NAME + ".java";
    private static final String ALREADY_ANNOTATED_CLASS_NAME = "AlreadyAnnotatedJavaClass";
    private static final String ALREADY_ANNOTATED_CLASS_FILE = ALREADY_ANNOTATED_CLASS_NAME + ".java";

    @Test
    void annotatePropertiesOnPath() throws IOException {
        Path javaCode = Path.of("src", "test", "resources", TESTING_CLASS_FILE);
        Path backup = javaCode.resolveSibling(TESTING_CLASS_FILE + ".bak");
        // Deleting backup file if already exists
        Files.deleteIfExists(backup);
        // Backing up original file content to restore it after test
        Files.copy(javaCode, backup);
        AnnotatorHelper.annotateProperties(javaCode);
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(javaCode);
        commonCheckImportDeclaration(compilationUnit);
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(this::commonCheckAnnotatedClassDeclaration);
        // Restore original file content
        Files.copy(backup, javaCode, StandardCopyOption.REPLACE_EXISTING);
        // Deleting backup file
        Files.deleteIfExists(backup);
    }

    @Test
    void conditionallyAddImport() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        AnnotatorHelper.conditionallyAddImport(compilationUnit);
        commonCheckImportDeclaration(compilationUnit);
        compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", ALREADY_ANNOTATED_CLASS_FILE));
        AnnotatorHelper.conditionallyAddImport(compilationUnit);
        commonCheckImportDeclaration(compilationUnit);
    }

    @Test
    void addAnnotationImport() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        AnnotatorHelper.addAnnotationImport(compilationUnit);
        commonCheckImportDeclaration(compilationUnit);
    }

    @Test
    void annotatePropertiesOnClass() {
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(Path.of("src", "test", "resources", TESTING_CLASS_FILE));
        assertThat(compilationUnit).isNotNull();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(cls -> cls.getName().toString().equals(TESTING_CLASS_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(TESTING_CLASS_NAME + " not found"));
        AnnotatorHelper.annotateProperties(classOrInterfaceDeclaration);
        commonCheckAnnotatedClassDeclaration(classOrInterfaceDeclaration);
    }


    @Test
    void annotatePropertiesOnFieldDeclaration() {
        FieldDeclaration fieldDeclaration = new FieldDeclaration();
        fieldDeclaration.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
        fieldDeclaration.setVariables(NodeList.nodeList(new VariableDeclarator(StaticJavaParser.parseClassOrInterfaceType("String"), "test")));
        AnnotatorHelper.annotateProperties(fieldDeclaration);
        commonCheckAnnotatedFieldDeclaration(fieldDeclaration);
    }

    private void commonCheckAnnotatedClassDeclaration(ClassOrInterfaceDeclaration toCheck) {
        Collection<FieldDeclaration> fieldDeclarations = GrabberHelper.getApplicationPropertyFields(toCheck);
        fieldDeclarations.forEach(this::commonCheckAnnotatedFieldDeclaration);
    }

    private void commonCheckImportDeclaration(CompilationUnit toCheck) {
        assertThat(toCheck.findAll(ImportDeclaration.class)
                .stream()
                .filter(importDeclaration -> KIE_PROPERTY_IMPORT.equals(importDeclaration.getNameAsString()))
                .toList())
                .hasSize(1);
    }


    private void commonCheckAnnotatedFieldDeclaration(FieldDeclaration toCheck) {
        assertThat(toCheck.getAnnotations().stream().anyMatch(annotationExpr -> KIE_PROPERTY_ANNOTATION.equals(annotationExpr.getNameAsString()))).isTrue();
    }
}