package test.with;

import ch.raffael.sansnull.SansNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Test {

    private Object myField; // EXPECTATION: inferred @NotNull, warning about missing initialisation
    @Nullable
    private Object myNullableField; // EXPECTATION: no warnings

    public Boolean warning(String msg) {
        if (msg.length() > 9 ) { // EXPECTATION
            System.out.println("greater than 9");
        }
        if (msg == null ) { // FAILING EXPECTATION: constant conditions warning
            System.out.println("not null");
        }
        return true;
    }

    public Boolean warning2(String msg) {
        if (msg == null ) { // FAILING EXPECTATION: constant conditions warning
            System.out.println("not null");
        }
        return true;
    }

    public void test(@NotNull String notNull) {
        warning(null);
    }

    public Object myField() {
        return myField; // EXPECTATION: no warnings
    }

    public Object myNullableFoo() {
        return myNullableField; // EXPECTATION: warning @NotNull method may return null
    }

}
