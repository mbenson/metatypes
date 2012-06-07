/*
 * Copyright 2011 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.metatype;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author David Blevins
 */
public class MetaAnnotatedMethod extends MetaAnnotatedObject<Method> implements AnnotatedMethod<Method> {

    private final Annotation[][] parameterAnnotations;

    public MetaAnnotatedMethod(Method method) {
        super(method, unroll(method));

        this.parameterAnnotations = unrollParameters(method.getParameterAnnotations());
    }

    public Annotation[] getDeclaredAnnotations() {
        return target.getDeclaredAnnotations();
    }

    public Annotation[][] getParameterAnnotations() {
        return parameterAnnotations;
    }

    public Class<?> getDeclaringClass() {
        return target.getDeclaringClass();
    }

    public String getName() {
        return target.getName();
    }

    public int getModifiers() {
        return target.getModifiers();
    }

    public Class<?>[] getParameterTypes() {
        return target.getParameterTypes();
    }

    public java.lang.reflect.Type[] getGenericParameterTypes() {
        return target.getGenericParameterTypes();
    }

    public Class<?>[] getExceptionTypes() {
        return target.getExceptionTypes();
    }

    public java.lang.reflect.Type[] getGenericExceptionTypes() {
        return target.getGenericExceptionTypes();
    }

    public String toGenericString() {
        return target.toGenericString();
    }

    public boolean isVarArgs() {
        return target.isVarArgs();
    }

    public boolean isSynthetic() {
        return target.isSynthetic();
    }
}
