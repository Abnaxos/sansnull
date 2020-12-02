SansNull
========

SansNull is a plugin for IntelliJ IDEA with an accompanying annotation,
similar to the
[nullability-annotation-plugin](https://github.com/stylismo/nullability-annotations-inspection).

The idea is to allow a no-nulls-by-default policy: everything is `@NotNull`
unless explicitly marked as `@Nullable`.


Rationale
---------

In my programming style, null only exists in a small interface layer to
third party code. I normally don't allow null anywhere.  However, if I
actually mark everything as `@NotNull` that is expected not to be null, I
can't find my code in all these `@NotNull` annotations. So, I'd like to mark
everything implicitly as `@NotNull` that isn't explicitly marked as
`@Nullable`.

The nullability-annotation-plugin can do this. Unfortunately, it relies on
JSR-305, which should be avoided. JSR-305 resides in the `javax.annotation`
package, but since it was never accepted, it doesn't belong there.
Especially when you start using JPMS (Java modules), this gets a real
problem. You have to deal with a split package, and use hacks like
`--patch-module` to get this working. This is ok for the transition, but we
need a solution. One that doesn't involve `javax.annotation.Nonnull`.

This is where the JetBrains annotations come into the picture. They were
always better than the JSR-305 ones, and the package also contains a lot of
other useful annotations. So, I decided to try to the same as the
nullability-annotation-plugin does, but for the JetBrains annotations
instead of JSR-305.

Meet SansNull.


Concept
-------

The `@SansNull` annotation can be applied to packages, classes and methods.
It implies `@NotNull` for everything which is not annotated within its
scope. Since the retention of `@SansNull` is class, this works across
project boundaries.

The main work is done in the plugin. It simply implies `@NotNull` where no
other annotation is present. IntelliJ IDEA sees this and issues the correct
warnings. It uses whatever you set as your preferred nullable/nonnull
annotations for the project.

Additionally, it adds a new warning whenever something is not annotatet all,
neither explicitly nor inferred (configurable in the inspection settings).
It also provides quick-fixes to quickly add `@SansNull` or
`@Nullable`/`@NotNull` annotations.

Usually, you'll simply annotate every package in your project with
`@SansNull`, that's it.


Building
--------

* Import the project with Gradle into IDEA, it should work out-of-the-box.
  It usese the
  [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin).

* Run the plugin from the IDE with a Gradle build configuration running the
  *runIde* task.

* Use `./gradlew buildPlugin` to build an installable ZIP file.

* You can open idea-plugin/test-project in IDEA to play around.

* Hint: you can display inferred annotations with *Settings → Editor → Inlay
  Hints → Java → Annotations*.
  

Known Issues
------------

### Constant Conditions

```java
@SansNull
public void foo(String bar) {
  if (bar == null) {
    System.out.println("bar is null");  
  }
}
```

There's currently no warning that `bar==null` is always false. If `bar` is
annotated explicitly, IDEA will warn correctly. I suspect this is a bug in
IDEA, but I'll investigate.

### Overridden methods are not annotated

If there is an explicit nullability annotation, it will have to be repeated.
The implied ones are not recognised by this inspection and it will be
warned.
