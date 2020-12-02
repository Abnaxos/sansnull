package test.without;

import org.jetbrains.annotations.NotNull;

public class TestWithout {

    public Boolean foo(String msg) { // EXPECTATION: warning: missing nullability annotation on method & param
        return msg == null || msg.length() > 0;
    }

    public void notNull(@NotNull String notNull) { // FAILING EXPECTATION: no warning overriding method not @NotNull
    }

    public void nonAnnotated(String notNull) { // EXPECTATION: missing nullability annotation warning on parameter
    }

    public Object foo() { // EXPECTATION: missing nullability annotation warning on method
        return null;
    }
}
