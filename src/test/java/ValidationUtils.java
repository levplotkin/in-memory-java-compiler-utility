import com.tikal.compiler.validation.Validation;
import com.tikal.compiler.validation.Validator;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.Policy;
import java.util.Arrays;
import java.util.List;


/**
 * @author levp
 */
public class ValidationUtils {
    final static int MAX_SIZE_10_MB = 1024 * 1024 * 10;

    /**
     * @return true if source is valid
     */
    public static boolean isSourceValid(final String source) {

        final List<String> mandatoryWords = Arrays.asList("HelloWorld", "public class",
                "public static void main");

        final Validator validator = new Validator.Builder().addValidation(validateFileSize(source, MAX_SIZE_10_MB))
                .addValidation(validateMandatoryWords(mandatoryWords, source)).build();

        return validator.isValid();
    }

    /**
     * @return true if compiled class is valid
     */
    public static boolean isClassValid(final Class<Policy> clazz) {
        final List<Class<?>> mandatoryAnnotations = Arrays.asList(new Class<?>[]{Resource.class});
        final List<Class<?>> mandatoryInterfaces = Arrays.asList(new Class<?>[]{Serializable.class});

        final Validator validator = new Validator.Builder()
                .addValidation(validateAnnotations(mandatoryAnnotations, clazz))
                .addValidation(validateInterfaces(mandatoryInterfaces, clazz)).build();

        return validator.isValid();
    }

    private static Validation validateAnnotations(final List<Class<?>> mandatoryAnnotations, final Class<Policy> clazz) {
        return new Validation() {
            @SuppressWarnings("unchecked")
            public boolean isPassed() {
                int countOfAnnotations = 0;
                for (final Class<?> annotation : mandatoryAnnotations) {
                    if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotation))
                        countOfAnnotations++;
                }

                return countOfAnnotations == mandatoryAnnotations.size();
            }
        };
    }

    private static Validation validateInterfaces(final List<Class<?>> interfaces, final Class<Policy> clazz) {
        return new Validation() {

            public boolean isPassed() {
                for (final Class<?> classesInterface : interfaces) {
                    if (!classesInterface.isAssignableFrom(clazz))
                        return false;
                }
                return true;
            }
        };
    }

    private static Validation validateMandatoryWords(final List<String> mandatoryWords, final String source) {
        return new Validation() {

            public boolean isPassed() {
                if (source == null || source.trim().length() == 0)
                    return false;

                for (final String mandatoryWord : mandatoryWords) {
                    if (!source.contains(mandatoryWord))
                        return false;
                }
                return true;
            }
        };
    }

    private static Validation validateFileSize(final String source, final int maxFileSize) {
        return new Validation() {
            public boolean isPassed() {
                return source != null && source.length() < maxFileSize;
            }
        };
    }
}
