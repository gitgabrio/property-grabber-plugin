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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bamoe.utils.property_grabber.utils.CommonHelper.KIE_PROPERTY_ANNOTATION;
import static org.bamoe.utils.property_grabber.utils.CommonHelper.KIE_PROPERTY_IMPORT;

public class AnnotatorHelper {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorHelper.class);

    public static void annotateProperties(Path javaCode) {
        logger.debug("annotateProperties {}", javaCode);
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(javaCode);
        LexicalPreservingPrinter.setup(compilationUnit);
        boolean annotated = annotateProperties(compilationUnit);
        if (annotated) {
            writeUpdatedCode(javaCode, compilationUnit);
        } else {
            logger.debug("No properties to annotate in {}", javaCode);
        }
    }

    /*
      Default access modifier for testing purpose
    */
    static boolean annotateProperties(CompilationUnit compilationUnit) {
        logger.debug("annotateProperties {}", compilationUnit.getPrimaryTypeName());
        AtomicBoolean isAnnotationAdded = new AtomicBoolean(false);
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(cls -> isAnnotationAdded.set(annotateProperties(cls) || isAnnotationAdded.get()));
        if (isAnnotationAdded.get()) {
            conditionallyAddImport(compilationUnit);
        }
        return isAnnotationAdded.get();
    }

    /*
        Default access modifier for testing purpose
    */
    static void conditionallyAddImport(CompilationUnit compilationUnit) {
        List<ImportDeclaration> imports = compilationUnit.findAll(ImportDeclaration.class);
        if (imports.stream().noneMatch(imprt -> imprt.getNameAsString().equals(KIE_PROPERTY_IMPORT))) {
            addAnnotationImport(compilationUnit);
        }
    }

    /*
        Default access modifier for testing purpose
    */
    static void addAnnotationImport(CompilationUnit compilationUnit) {
        logger.debug("addAnnotationImport {}", compilationUnit.getPrimaryTypeName());
        compilationUnit.getImports().add(new ImportDeclaration(KIE_PROPERTY_IMPORT, false, false));
    }

    /*
        Default access modifier for testing purpose
    */
    static boolean annotateProperties(ClassOrInterfaceDeclaration node) {
        logger.debug("annotateProperties {}", node.getName());
        Collection<FieldDeclaration> applicationPropertyFields = GrabberHelper.getNotKieAnnotatedApplicationPropertyFields(node);
        if (applicationPropertyFields.isEmpty()) {
            return false;
        } else {
            applicationPropertyFields
                    .forEach(AnnotatorHelper::annotateProperties);
            return true;
        }
    }

    /*
        Default access modifier for testing purpose
    */
    static void annotateProperties(FieldDeclaration field) {
        logger.trace("annotateProperties {}", field);
        field.addAnnotation(KIE_PROPERTY_ANNOTATION);
    }

    private static void writeUpdatedCode(Path javaCode, CompilationUnit compilationUnit) {
        logger.debug("writeUpdatedCode {} {}", javaCode,  compilationUnit.getPrimaryTypeName());
        try {
            String prettyPrintedCode = LexicalPreservingPrinter.print(compilationUnit);
            //String prettyPrintedCode = compilationUnit.toString();
            writeUpdatedCode(javaCode, prettyPrintedCode);
        } catch (Exception e) {
            logger.error("Error while writing file: {}", javaCode);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static void writeUpdatedCode(Path toOverWrite, String newContent) {
        logger.debug("writeUpdatedCode {}", toOverWrite);
        try (PrintWriter writer = new PrintWriter(toOverWrite.toFile())) {
            writer.print("");
            writer.print(newContent);
            writer.flush();
        } catch (Exception e) {
            logger.error("Error while writing file: {}", toOverWrite);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}