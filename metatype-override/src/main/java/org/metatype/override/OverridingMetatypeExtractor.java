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
import javax.annotation.MetatypeExtractor;

import org.apache.commons.lang3.AnnotationUtils;

/**
 * {@link MetatypeExtractor} with typesafe override feature.
 */
public abstract class OverridingMetatypeExtractor<A extends Annotation>
        implements MetatypeExtractor<A> {
    private static final DefaultMetatypeExtractor DEFAULT_METATYPE_EXTRACTOR = new DefaultMetatypeExtractor();

    private final AnnotationOverrider<?, ?>[] overriders;

    /**
     * @param overriders
     *            in ascending priority (last runs last)
     */
    protected OverridingMetatypeExtractor(
            AnnotationOverrider<?, ?>... overriders) {
        this.overriders = overriders == null ? new AnnotationOverrider[] {}
                : overriders;
    }

    @Override
    public final Collection<Annotation> extractAnnotations(A annotation) {
        // ensure a modifiable list:
        final ArrayList<Annotation> result = new ArrayList<Annotation>(
                DEFAULT_METATYPE_EXTRACTOR.extractAnnotations(annotation));
        final ListIterator<Annotation> iter = result.listIterator();
        while (iter.hasNext()) {
            Annotation toOverride = iter.next();
            Annotation overridden = override(toOverride, annotation);
            if (!AnnotationUtils.equals(overridden, toOverride)) {
                iter.set(overridden);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <S extends Annotation> S override(S servant, A master) {
        S result = servant;
        for (AnnotationOverrider<?, ?> overrider : overriders) {
            if (overrider.supports(servant.annotationType(),
                    master.annotationType())) {
                result = ((AnnotationOverrider<S, A>) overrider).override(
                        servant, master);
            }
        }
        return result;
    }
}
