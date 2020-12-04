/*
 *  Copyright (c) 2020 Raffael Herzog
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

package ch.raffael.sansnull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> The {@code @SansNull} annotation implies nullability annotations on
 * methods, fields and parameters within the annotated scope. It can be
 * applied to methods (affects method return value and parameters), classes
 * (affects all methods, fields and inner or nested classes) and packages
 * (affects all classes within that package). </p>
 *
 * <p> The affected elements can be filtered using the attributes. All
 * elements are enabled by default. </p>
 *
 * <p> Nested {@code @SansNull} annotations override outer annotations, for
 * example:</p>
 *
 * <pre>
 *{@literal @SansNull}
 * class Example {
 *  {@literal @SansNull(parameters=false)}
 *   void foo(String fooParam) {
 *   }
 *
 *   void bar(String barParam) {
 *   }
 * }
 * </pre>
 *
 * <p> In this example, {@code @NotNull} will be implied for {@code
 * barParam}, but not {@code fooParam}. </p>
 *
 * @see Imply
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface SansNull {
  boolean method() default true;
  boolean field() default true;
  boolean parameter() default true;

  // TODO (2020-12-02) implement local(), typeUse()
  // boolean local() default false;
  // boolean typeUse() default false;
  // TODO (2020-12-01) add exception(), message()

  /**
   * <p> Meta annotation to imply {@link SansNull @SansNull} with other
   * annotations. Any annotation that's annotated with this, will imply
   * {@link SansNull @SansNull} on the annotated element with the specified
   * parameters </p>
   *
   * <p> This won't traverse the whole annotation tree, so only annotations
   * that are directly annotated with {@code Imply} pass the
   * {@link SansNull @SansNull} on to the annotated element. </p>
   *
   * <p> If an element has both an implied and an explicit
   * {@link SansNull @SansNull}, the explicit one overrides the implied
   * one. </p>
   *
   * <p> If an element has multiple implied {@link SansNull @SansNull}
   * annotations, they will be merged, i.e. all attributes of the implied
   * annotations are OR-ed. </p>
   *
   * @see SansNull
   */
  @Documented
  @Retention(RetentionPolicy.CLASS)
  @Target(ElementType.ANNOTATION_TYPE)
  @interface Imply {
    boolean method() default true;
    boolean field() default true;
    boolean parameter() default true;
  }
}
