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

package ch.raffael.sansnull.idea;

import com.intellij.codeInsight.InferredAnnotationProvider;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static ch.raffael.sansnull.idea.SansNullPsi.createNotNull;
import static ch.raffael.sansnull.idea.SansNullPsi.defaultNotNull;
import static ch.raffael.sansnull.idea.SansNullPsi.findSansNull;
import static ch.raffael.sansnull.idea.SansNullPsi.isNullAnnotatable;

public class SansNullInferredAnnotationProvider implements InferredAnnotationProvider {

  private static final List<Class<? extends PsiModifierListOwner>> ROOT_TYPES = List.of(
      PsiParameter.class, PsiMethod.class, PsiField.class);

  @Override
  @Nullable
  public PsiAnnotation findInferredAnnotation(@NotNull PsiModifierListOwner listOwner, @NotNull String annotationFQN) {
    if (!defaultNotNull(listOwner).equals(annotationFQN)) {
      return null;
    }
    if (ROOT_TYPES.stream().noneMatch(t -> t.isInstance(listOwner))) {
      return null;
    }
    if (!isNullAnnotatable(listOwner)) {
      return null;
    }
    return createNotNull(listOwner, findSansNull(listOwner));
  }

  @Override
  public @NotNull List<PsiAnnotation> findInferredAnnotations(@NotNull PsiModifierListOwner listOwner) {
    return Optional.ofNullable(findInferredAnnotation(listOwner, defaultNotNull(listOwner)))
        .map(List::of).orElse(List.of());
  }
}
