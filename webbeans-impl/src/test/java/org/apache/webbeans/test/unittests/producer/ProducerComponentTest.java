/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.test.unittests.producer;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.enterprise.util.TypeLiteral;
import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractBean;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.lifecycle.test.MockHttpSession;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.producer.ParametrizedModel1;
import org.apache.webbeans.test.component.producer.ParametrizedModel2;
import org.apache.webbeans.test.component.producer.ParametrizedProducer;
import org.apache.webbeans.test.component.producer.Producer1;
import org.apache.webbeans.test.component.producer.Producer2;
import org.apache.webbeans.test.component.producer.Producer3;
import org.apache.webbeans.test.component.producer.Producer4;
import org.apache.webbeans.test.component.producer.Producer4ConsumerComponent;
import org.junit.Before;
import org.junit.Test;

public class ProducerComponentTest extends TestContext
{

    public ProducerComponentTest()
    {
        super(ProducerComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    /**
     * From the container with servlet context
     */
    public void startTests(ServletContext ctx)
    {
        testProducerDeployment1();
        testProducerDeployment2();
    }

    @Test
    public void testProducerDeployment1()
    {
        clear();
        defineManagedBean(Producer1.class);
        Assert.assertEquals(3, getDeployedComponents());

    }

    @Test
    public void testProducerDeployment2()
    {
        clear();
        defineManagedBean(Producer2.class);
        Assert.assertEquals(4, getDeployedComponents());
    }

    @Test
    public void testProducerDeployment3()
    {
        clear();
        defineManagedBean(Producer3.class);

        Assert.assertEquals(6, getDeployedComponents());
    }

    @Test
    public void testParametrizedProducer()
    {
        clear();
        defineManagedBean(ParametrizedProducer.class);

        ContextFactory.initRequestContext(null);
        Assert.assertEquals(4, getDeployedComponents());

        TypeLiteral<List<ParametrizedModel1>> model1 = new TypeLiteral<List<ParametrizedModel1>>()
        {
        };

        List<ParametrizedModel1> instance = getManager().getInstanceByType(model1, new Annotation[0]);
        Assert.assertNull(instance);
        Assert.assertTrue(ParametrizedProducer.getCALLMODEL1());
        Assert.assertTrue(!ParametrizedProducer.getCALLMODEL2());

        TypeLiteral<List<ParametrizedModel2>> model2 = new TypeLiteral<List<ParametrizedModel2>>()
        {
        };
        List<ParametrizedModel2> instance2 = getManager().getInstanceByType(model2, new Annotation[0]);

        Assert.assertNull(instance2);
        Assert.assertTrue(ParametrizedProducer.getCALLMODEL2());

    }

    @Test
    public void testProducer4()
    {
        defineManagedBean(Producer4.class);
        AbstractBean<Producer4ConsumerComponent> component = defineManagedBean(Producer4ConsumerComponent.class);

        ContextFactory.initSessionContext(new MockHttpSession());

        Producer4ConsumerComponent instance = getManager().getInstance(component);

        Assert.assertNotNull(instance);

        int count = instance.count();

        Assert.assertEquals(1, count);

    }

}