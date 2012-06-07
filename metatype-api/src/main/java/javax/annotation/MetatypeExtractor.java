package javax.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Interface for unrolling a {@link Metatype}.
 *
 * @author mbenson
 */
public interface MetatypeExtractor {
    /**
     * Extract annotations from a given meta-annotation.
     * @param annotation
     * @return the extracted Annotations.
     */
    Collection<Annotation> extractAnnotations(Annotation annotation);
}
