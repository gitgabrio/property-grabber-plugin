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
package org.bamoe.utils.property_grabber.mojos;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPropertyMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPropertyMojo.class);

    @Parameter(readonly = true, defaultValue = "${project}")
    protected MavenProject mavenProject;

    void parentExecute(String mojoName) throws MojoExecutionException, MojoFailureException {
        logger.info("{} {} {}", mojoName, mavenProject.getGroupId(), mavenProject.getArtifactId());
        logger.debug("Sources {}", mavenProject.getCompileSourceRoots());
        try {
            mavenProject.getCompileSourceRoots().forEach(this::iterateSourceDirectory);
        } catch (Exception e) {
            throw new MojoFailureException("Failed to iterate source directory", e);
        }
    }

    abstract void readJavaClass(Path entry) throws IOException;

    private void iterateSourceDirectory(String sourceDirectory) {
        logger.debug("iterateSourceDirectory {}", sourceDirectory);
        Path path = Path.of(sourceDirectory);
        if (path.toFile().exists()) {
            iterateSourceDirectory(path);
        }
    }

    private void iterateSourceDirectory(Path sourceDirectory) {
        logger.debug("iterateSourceDirectory {}", sourceDirectory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    iterateSourceDirectory(entry);
                } else if (Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(".java")) {
                    readJavaClass(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private void readJavaClass(Path entry) throws IOException {
//        logger.debug("readJavaClass {}", entry);
//        var properties = "";
//
//        // If we get anything besides "asciidoc" for the output type, use the text based output
//        if (!"asciidoc".equals(outputType)) {
//            properties = ParserHelper.getProperties(entry);
//        } else {
//            properties = ParserHelper.getPropertiesAsAdoc(entry);
//        }
//
//        logger.debug("properties {}", properties);
//        if (!properties.isEmpty()) {
//            printProperties(properties);
//        }
//    }
//
//    private void printProperties(String toPrint) throws IOException {
//        logger.info("printProperties {}", toPrint);
//        File outputFile = new File("properties.adoc");
//        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
//        writer.append(toPrint);
//        writer.close();
//    }
}