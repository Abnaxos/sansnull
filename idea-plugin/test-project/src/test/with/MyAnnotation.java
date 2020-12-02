package test.with;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public @interface MyAnnotation {
    String foo(); // FAILING EXPECTATION: no warning about overridden methods not being annotated

    class Impl implements MyAnnotation {
        @Override
        public String foo() {
            return null; // EXPECTATION: warning about returning null
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null; // EXPECTATION: no warning about returning null, because it's inherited from Java
            // todo: it is a @NotNull method -- wouldn't it be IDEA's job to warn?
        }
    }
}
