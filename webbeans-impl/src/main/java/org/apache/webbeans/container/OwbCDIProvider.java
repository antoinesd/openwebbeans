/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.container;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContainerLifecycle;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

public class OwbCDIProvider implements CDIProvider
{
    private static final OwbCDI OWB_CDI = new OwbCDI();
    private static boolean firstTime = true;
    private static ContainerLifecycle lifecycle = null;

    @Override
    public CDI<Object> getCDI()
    {
        if (firstTime) {
            lifecycle = WebBeansContext.getInstance().getService(ContainerLifecycle.class);
            lifecycle.startApplication(null);
            firstTime = false;
        }
        return OWB_CDI;
    }
}
