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
package javax.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * Default {@link MetatypeExtractor} implementation which uses the {@link ServiceLoader} API
 * to consult installed {@link MetatypeExtractor} implementations.
 * Implementations should have a default constructor.
 *
 * @author mbenson
 */
public class DefaultMetatypeExtractor implements MetatypeExtractor<Annotation> {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Annotation> extractAnnotations(Annotation annotation) {
        for (@SuppressWarnings("rawtypes") MetatypeExtractor extractor : ServiceLoader.load(MetatypeExtractor.class)) {
            if (extractor.getClass().equals(DefaultMetatypeExtractor.class)) {
                continue;
            }
            Collection<Annotation> result;
            try {
                result = extractor.extractAnnotations(annotation);
            } catch (Exception e) {
                //should really check type compatibility
                continue;
            }
            if (result == null) {
                continue;
            }
            return result;
        }
        return null;
    }

}
