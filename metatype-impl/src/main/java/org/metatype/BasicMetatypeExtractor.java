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
package org.metatype;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Metaroot;
import javax.annotation.Metatype;
import javax.annotation.MetatypeExtractor;

/**
 * Basic {@link MetatypeExtractor} implementation.
 *
 * @author David Blevins
 */
public class BasicMetatypeExtractor implements MetatypeExtractor<Annotation> {

    @Override
    public Collection<Annotation> extractAnnotations(Annotation annotation) {
        return getDeclaredMetaAnnotations(annotation.annotationType());
    }

    private static Collection<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> clazz) {

        Map<Class<? extends Annotation>, Annotation> map = new HashMap<Class<? extends Annotation>, Annotation>();

        // pull in the annotations declared on this annotation

        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            map.put(annotation.annotationType(), annotation);
        }

        List<Annotation[]> groups = new ArrayList<Annotation[]>();

        Class<? extends Annotation> metatype = getMetatype(clazz);
        if (metatype != null) {
            try {
                Class<?> def = clazz.getClassLoader().loadClass(clazz.getName() + "$$");

                List<AnnotatedElement> elements = new ArrayList<AnnotatedElement>();

                elements.addAll(asList(def.getDeclaredFields()));
                elements.addAll(asList(def.getDeclaredConstructors()));
                elements.addAll(asList(def.getDeclaredMethods()));

                for (Method method : def.getDeclaredMethods()) {
                    for (Annotation[] array : method.getParameterAnnotations()) {
                        groups.add(array);
                    }
                }

                for (Constructor<?> constructor : def.getDeclaredConstructors()) {
                    for (Annotation[] array : constructor.getParameterAnnotations()) {
                        groups.add(array);
                    }
                }

                for (AnnotatedElement element : elements) {
                    groups.add(element.getDeclaredAnnotations());
                }

                for (Annotation[] annotations : groups) {
                    if (contains(annotations, clazz)) {
                        for (Annotation annotation : annotations) {
                            map.put(annotation.annotationType(), annotation);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // inner class is optional
            }
        }

        // TODO: why not? maybe ignore all annotations that are not applicable
        map.remove(Target.class);
        map.remove(Retention.class);
        map.remove(Documented.class);
        // if the chicken is an egg, carry it forward
        if (!isMetaAnnotation(metatype))
            map.remove(metatype);
        map.remove(clazz);

        return map.values();
    }

    private static Class<? extends Annotation> getMetatype(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();

            if (isMetatypeAnnotation(type)) return type;
        }

        return null;
    }

    private static boolean contains(Annotation[] annotations, Class<? extends Annotation> clazz) {
        for (Annotation annotation : annotations) {
            if (clazz.equals(annotation.annotationType())) return true;
        }
        return false;
    }

    private static boolean isMetaAnnotation(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (isMetatypeAnnotation(annotation.annotationType())) return true;
        }

        return false;
    }

    private static boolean isMetatypeAnnotation(Class<? extends Annotation> type) {
        if (Metatype.class.equals(type)) return true;

        for (Annotation annotation : type.getAnnotations()) {
            if (Metaroot.class.equals(annotation.annotationType())) return true;
        }

        return false;
    }

}
