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
package org.apache.webbeans.util;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.exception.inject.InconsistentSpecializationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * This class contains a few helpers for handling
 * &#064;Specializes.
 */
public class SpecializationUtil
{
    private final WebBeansContext webBeansContext;
    private final AlternativesManager alternativesManager;
    private final WebBeansUtil webBeansUtil;


    public SpecializationUtil(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        this.alternativesManager = webBeansContext.getAlternativesManager();
        this.webBeansUtil = webBeansContext.getWebBeansUtil();
    }


    public void removeDisabledTypes(List<AnnotatedType<?>> annotatedTypes)
    {
        if (annotatedTypes != null && !annotatedTypes.isEmpty())
        {
            // superClassList is used to handle the case: Car, CarToyota, Bus, SchoolBus, CarFord
            // for which case OWB should throw exception that both CarToyota and CarFord are
            // specialize Car.
            // see spec section 5.1.3
            Set<Class<?>> superClassList = new HashSet<Class<?>>();

            // first let's find all superclasses of Specialized types
            Set<Class<?>> disabledClasses = new HashSet<Class<?>>();
            for(AnnotatedType<?> annotatedType : annotatedTypes)
            {
                if(annotatedType.getAnnotation(Specializes.class) != null && isEnabled(annotatedType))
                {
                    Class<?> specialClass = annotatedType.getJavaClass();
                    Class<?> superClass = specialClass.getSuperclass();

                    if(superClass.equals(Object.class))
                    {
                        throw new WebBeansDeploymentException(new WebBeansConfigurationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0003)
                                + specialClass.getName() + WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0004)));
                    }
                    if (superClassList.contains(superClass))
                    {
                        // since CDI 1.1 we have to wrap this in a DeploymentException
                        throw new WebBeansDeploymentException(new InconsistentSpecializationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0005) +
                                                                       superClass.getName()));
                    }
                    if (!containsAllSuperclassTypes(annotatedType, superClass, annotatedTypes))
                    {
                        throw new WebBeansDeploymentException(new InconsistentSpecializationException("@Specialized Class : " + specialClass.getName()
                                                                          + " must have all bean types of its super class"));
                    }

                    AnnotatedType<?> superType = getAnnotatedTypeForClass(annotatedTypes, superClass);

                    if (!webBeansUtil.isConstructorOk(superType))
                    {
                        throw new WebBeansDeploymentException(new InconsistentSpecializationException("@Specializes class " + specialClass.getName()
                                + " does not extend a bean with a valid bean constructor"));
                    }

                    try
                    {
                        webBeansUtil.checkManagedBean(specialClass);
                    }
                    catch (WebBeansConfigurationException illegalBeanTypeException)
                    {
                        // this Exception gets thrown if the given class is not a valid bean type
                        throw new WebBeansDeploymentException(new InconsistentSpecializationException("@Specializes class " + specialClass.getName()
                                                                    + " does not extend a valid bean type", illegalBeanTypeException));
                    }

                    superClassList.add(superClass);

                    while (!superClass.equals(Object.class))
                    {
                        disabledClasses.add(superClass);
                        superClass = superClass.getSuperclass();
                    }
                }
            }

            // and now remove all AnnotatedTypes of those collected disabledClasses
            if (!disabledClasses.isEmpty())
            {
                Iterator<AnnotatedType<?>> annotatedTypeIterator = annotatedTypes.iterator();
                while (annotatedTypeIterator.hasNext())
                {
                    AnnotatedType<?> annotatedType = annotatedTypeIterator.next();
                    if (disabledClasses.contains(annotatedType.getJavaClass()))
                    {
                        annotatedTypeIterator.remove();
                    }
                }
            }
        }
    }

    private boolean containsAllSuperclassTypes(AnnotatedType<?> annotatedType, Class<?> superClass, List<AnnotatedType<?>> annotatedTypes)
    {
        Typed typed = annotatedType.getAnnotation(Typed.class);
        if (typed != null)
        {
            List<Class<?>> typeList = Arrays.asList(typed.value());
            AnnotatedType<?> superType = getAnnotatedTypeForClass(annotatedTypes, superClass);
            if (superType != null)
            {
                Typed superClassTyped = superType.getAnnotation(Typed.class);
                Set<Type> superClassTypes;
                if (superClassTyped != null)
                {
                    superClassTypes = new HashSet<Type>(Arrays.asList(superClassTyped.value()));
                }
                else
                {
                    superClassTypes = superType.getTypeClosure();

                    // we can ignore Object.class in this case
                    superClassTypes.remove(Object.class);
                }

                return typeList.containsAll(superClassTypes);
            }
        }
        return true;
    }

    private AnnotatedType<?> getAnnotatedTypeForClass(List<AnnotatedType<?>> annotatedTypes, Class<?> clazz)
    {
        for (AnnotatedType<?> annotatedType : annotatedTypes)
        {
            if (annotatedType.getJavaClass().equals(clazz))
            {
                return annotatedType;
            }
        }

        return null;
    }

    /**
     * @return false if the AnnotatedType is for a not enabled Alternative
     */
    private boolean isEnabled(AnnotatedType<?> annotatedType)
    {
        return  annotatedType.getAnnotation(Alternative.class) == null ||
                alternativesManager.isAlternative(annotatedType.getJavaClass(), getAnnotationClasses(annotatedType));
    }

    private Set<Class<? extends Annotation>> getAnnotationClasses(AnnotatedType<?> annotatedType)
    {
        Set<Annotation> annotations = annotatedType.getAnnotations();
        if (annotations != null && !annotations.isEmpty())
        {
            Set<Class<? extends Annotation>> annotationClasses = new HashSet<Class<? extends Annotation>>(annotations.size());
            for (Annotation annotation : annotations)
            {
                annotationClasses.add(annotation.annotationType());
            }

            return annotationClasses;
        }
        return Collections.EMPTY_SET;
    }
}
