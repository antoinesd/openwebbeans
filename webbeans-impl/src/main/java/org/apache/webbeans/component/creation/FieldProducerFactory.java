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
package org.apache.webbeans.component.creation;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.ProducerFieldProducer;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public class FieldProducerFactory<P> extends BaseProducerFactory<P>
{
    private AnnotatedField<? super P> producerField;

    public FieldProducerFactory(AnnotatedField<? super P> producerField, Bean<P> parent, WebBeansContext webBeansContext)
    {
        super(parent, webBeansContext);
        Asserts.assertNotNull(producerField, "producer method may not be null");

        if (producerField.isAnnotationPresent(Inject.class))
        {
            throw new IllegalArgumentException("producer field has @Inject annotation: " + producerField);
        }

        final Type type = producerField.getJavaMember().getGenericType();
        if (ParameterizedType.class.isInstance(type))
        {
            for (final Type arg : ParameterizedType.class.cast(type).getActualTypeArguments())
            {
                if (ClassUtil.isWildCardType(arg))
                {
                    throw new IllegalArgumentException("Wildcard are forbidden: " + type);
                }
            }
        }

        this.producerField = producerField;
        defineDisposalMethod();
    }

    @Override
    public <T> Producer<T> createProducer(Bean<T> bean)
    {
        final Set<InjectionPoint> disposalIPs = getInjectionPoints(bean);
        final Producer<T> producer = new ProducerFieldProducer<T, P>(parent, producerField, disposalMethod, disposalIPs, webBeansContext);
        return webBeansContext.getWebBeansUtil().fireProcessProducerEvent(producer, producerField);
    }

    public Class<?> getReturnType()
    {
        return producerField.getJavaMember().getType();
    }

    @Override
    protected AnnotatedMember<? super P> producerType()
    {
        return producerField;
    }
}
