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
import org.kie.api.annotations.KieProperty;

@KieProperty
public class AlreadyAnnotatedJavaClass {

    /**
     * Some javadoc
     */
    @KieProperty
    public static final String STRONGLY_TYPED_CONFIGURATION_KEY = "kogito.decisions.stronglytyped";

    /**
     * (boolean) enable/disable asynchronous collection of decision events; default to true
     */
    public static final String KOGITO_ADDON_TRACING_DECISION_ASYNC_ENABLED = "kogito.addon.tracing.decision.asyncEnabled";

}
