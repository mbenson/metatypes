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
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

import javax.annotation.DefaultMetatypeExtractor;

import org.apache.commons.lang3.ObjectUtils;

public abstract class OverridingMetatypeExtractor extends
        DefaultMetatypeExtractor {
    private final AnnotationOverrider<?, ?>[] overriders;

    protected OverridingMetatypeExtractor(
            AnnotationOverrider<?, ?>... overriders) {
        this.overriders = overriders == null ? new AnnotationOverrider[] {}
                : overriders;
    }

    @Override
    public final Collection<Annotation> extractAnnotations(Annotation annotation) {
        // ensure a modifiable list:
        final ArrayList<Annotation> result = new ArrayList<Annotation>(
                super.extractAnnotations(annotation));
        final ListIterator<Annotation> iter = result.listIterator();
        while (iter.hasNext()) {
            Annotation toOverride = iter.next();
            Annotation overridden = override(toOverride, annotation);
            if (ObjectUtils.notEqual(overridden, toOverride)) {
                iter.set(overridden);
            }
        }
        return result;
    }

    private <A extends Annotation, M extends Annotation> A override(A servant,
            M master) {
        A result = servant;
        for (AnnotationOverrider<?, ?> overrider : overriders) {
            //TODO check applicability and run

        }
        return result;
    }
}
