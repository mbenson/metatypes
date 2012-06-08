/*
 * Copyright 2012 The original author or authors
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
package org.metatype.override;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.proxy2.stub.AnnotationConfigurer;
import org.apache.commons.proxy2.stub.AnnotationFactory;

/**
 * Overrides an annotation.
 *
 * @author mbenson
 *
 * @param <A>
 *            overridden type
 * @param <M>
 *            overriding type
 */
public abstract class AnnotationOverrider<A extends Annotation, M extends Annotation>
        extends AnnotationConfigurer<A> {
    private final Class<M> overridingType;

    private A overridden;
    private M overriding;

    @SuppressWarnings("unchecked")
    protected AnnotationOverrider() {
        overridingType = (Class<M>) TypeUtils.getRawType(
                AnnotationOverrider.class.getTypeParameters()[1], getClass());
        Validate.validState(overridingType != null,
                "Overriding type must be fully specified");
    }

    final synchronized A override(A overridden, M overriding) {
        this.overridden = overridden;
        this.overriding = overriding;
        try {
            return AnnotationFactory.INSTANCE.createDelegator(overridden, this);
        } finally {
            this.overridden = null;
            this.overriding = null;
        }
    }

    @Override
    protected final synchronized void configure(A stub) {
        Validate.validState(overridden != null && overriding != null);
        override(stub, overridden, overriding);
    }

    /**
     * Override implementation.
     *
     * @param result
     * @param overridden
     * @param overriding
     */
    protected abstract void override(A result, A overridden, M overriding);
}
