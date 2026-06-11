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
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.bamoe.utils.property_grabber.utils.AnnotatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(name = "annotate", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class PropertyAnnotatorMojo extends AbstractPropertyMojo {

    private static final Logger logger = LoggerFactory.getLogger(PropertyAnnotatorMojo.class);

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        parentExecute("PropertyAnnotator");
    }

    void readJavaClass(Path entry) throws IOException {
        logger.debug("readJavaClass {}", entry);
        AnnotatorHelper.annotateProperties(entry);
    }

}