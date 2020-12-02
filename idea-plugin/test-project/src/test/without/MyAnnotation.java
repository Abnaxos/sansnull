package test.without;

import java.lang.annotation.Annotation;

public @interface MyAnnotation {
    String foo(); // EXPECTATION: no inferred @NotNull

    class Impl implements MyAnnotation {
        @Override
        public String foo() {
            return null; // EXPECTATION: warning about returning null
            // todo: it is a @NotNull method -- wouldn't it be IDEA's job to warn? should it be in the plugin?
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null; // EXPECTATION: no warning about returning null, because it's inherited from Java
            // todo: it is a @NotNull method -- wouldn't it be IDEA's job to warn? should it be in the plugin?
        }
    }
}
