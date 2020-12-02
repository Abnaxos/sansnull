package test.with;

import test.without.TestWithout;

public class ExtendingNonSansNull extends TestWithout implements SansNulIface {

    public ExtendingNonSansNull(Object dummy) { // EXPECTATION: inferred @NotNull
    }

    @Override
    public void notNull(String notNull) { // EXPECTATION: warning about missing @NotNull (overridden from non-SansNull)
        super.notNull(notNull);
    }

    @Override
    public void nonAnnotated(String notNull) { // EXPECTATION: no inferred @NotNull (overridden from non-SansNull)
        super.nonAnnotated(notNull);
    }

    @Override
    public String fromIface(String str) { // EXPECTATION: inferred @NotNull
        return null; // EXPECTATION: warning about returning null
    }
}
