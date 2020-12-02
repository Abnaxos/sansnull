package test.with;

public class Extending extends Test {

    @Override
    public Boolean warning(String msg) { // EXPECTATION: both method and param with inferred @NotNull
        return super.warning(msg);
    }

    public void test() {
        warning(null); // EXPECTATION: warning: passing null as @NotNull parameter
    }
}
