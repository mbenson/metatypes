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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Metaroot;
import javax.annotation.Metatype;
import javax.annotation.MetatypeExtractor;

/**
 * @author David Blevins
 */
public abstract class MetaAnnotatedObject<T> implements MetaAnnotated<T> {
    protected final Map<Class<? extends Annotation>, MetaAnnotation<?>> annotations = new HashMap<Class<? extends Annotation>, MetaAnnotation<?>>();
    protected final T target;

    MetaAnnotatedObject(T target, Map<Class<? extends Annotation>, MetaAnnotation<?>> annotations) {
        this.target = target;
        this.annotations.putAll(annotations);
    }

    public T get() {
        return target;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotations.containsKey(annotationClass);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        @SuppressWarnings("unchecked")
        MetaAnnotation<A> annotation = (MetaAnnotation<A>) annotations.get(annotationClass);
        return (annotation == null) ? null : annotation.get();
    }

    public Annotation[] getAnnotations() {
        Annotation[] annotations = new Annotation[this.annotations.size()];

        int i = 0;
        for (MetaAnnotation<?> annotation : this.annotations.values()) {
            annotations[i++] = annotation.get();
        }

        return annotations;
    }

    public Collection<MetaAnnotation<?>> getMetaAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    @Override
    public boolean equals(Object obj) {
        return get().equals(obj);
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public String toString() {
        return get().toString();
    }


    private static <A extends Annotation> void unroll(A source, int depth, Map<Class<? extends Annotation>, MetaAnnotation<?>> found) {

        for (Annotation annotation : extractFrom(source)) {
            final Class<? extends Annotation> type = annotation.annotationType();

            final MetaAnnotation<?> existing = found.get(type);

            if (existing != null && existing.getDepth() < depth) {

                // IGNORE, what we have already is higher priority

                continue;
            }

            final MetaAnnotation<?> metaAnnotation = new MetaAnnotation<Annotation>(annotation, depth);

            if (existing == null || existing.getDepth() > depth) {

                // ADD / OVERWRITE

                found.put(type, metaAnnotation);

                unroll(annotation, depth + 1, found);

            } else {

                // CONFLICT

                // They are the same depth and therefore conflicting

                addTo(existing.getConflicts(), metaAnnotation);

            }

        }
    }

    /*
     * Narrow scope of suppressed warnings:
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean addTo(List conflictsList, MetaAnnotation annotation) {
        return conflictsList.add(annotation);
    }

    private static <A extends Annotation> Collection<Annotation> extractFrom(A annotation) {
        final Annotation metaAnnotation = getMetatype(annotation.annotationType());
        if (metaAnnotation == null) {
            return Collections.<Annotation> emptySet();
        }
        final MetatypeExtractor<? super A> extractor;
        if (metaAnnotation.annotationType().equals(Metatype.class)) {
            extractor = newInstance(((Metatype) metaAnnotation).extractUsing());
        } else {
            extractor = new BasicMetatypeExtractor();
        }
        return extractor.extractAnnotations(annotation);
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> MetatypeExtractor<A> newInstance(
            @SuppressWarnings("rawtypes") Class<? extends MetatypeExtractor> extractorType) {
        try {
            return extractorType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("metadata exception", e);
        }
    }

    private static Annotation getMetatype(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (isMetatypeAnnotation(annotation.annotationType())) return annotation;
        }

        return null;
    }

    private static boolean isMetatypeAnnotation(Class<? extends Annotation> type) {
        // shortcut for the common case:
        if (Metatype.class.equals(type)) return true;

        for (Annotation annotation : type.getAnnotations()) {
            if (Metaroot.class.equals(annotation.annotationType())) return true;
        }

        return false;
    }

    //TODO use or delete
    private static boolean validTarget(Class<? extends Annotation> type) {
        final Target target = type.getAnnotation(Target.class);

        if (target == null) return false;

        final ElementType[] targets = target.value();

        return targets.length == 1 && targets[0] == ElementType.ANNOTATION_TYPE;
    }

    protected static Map<Class<? extends Annotation>, MetaAnnotation<?>> unroll(AnnotatedElement element) {
        return unroll(element.getDeclaredAnnotations());
    }

    protected static Map<Class<? extends Annotation>, MetaAnnotation<?>> unroll(Annotation[] annotations) {
        final Map<Class<? extends Annotation>, MetaAnnotation<?>> map = new HashMap<Class<? extends Annotation>, MetaAnnotation<?>>();

        for (Annotation annotation : annotations) {

            map.put(annotation.annotationType(), new MetaAnnotation<Annotation>(annotation, 0));

            unroll(annotation, 1, map);

        }

        return map;
    }

    protected Annotation[][] unrollParameters(Annotation[][] parameterAnnotations) {
        final Annotation[][] unrolledParameters = new Annotation[parameterAnnotations.length][];

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            final Map<Class<? extends Annotation>, MetaAnnotation<?>> map = unroll(annotations);

            int j = 0;

            final Annotation[] unrolled = new Annotation[map.size()];
            for (MetaAnnotation<?> metaAnnotation : map.values()) {
                unrolled[j++] = metaAnnotation.get();
            }

            unrolledParameters[i++] = unrolled;
        }
        return unrolledParameters;
    }
}
