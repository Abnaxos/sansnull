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

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

class SansNullPsi {

  private SansNullPsi() {
  }

  @Nullable
  static PsiAnnotation findSansNull(@Nullable PsiModifierListOwner element) {
    if (element == null) {
      return null;
    }
    if (element instanceof PsiParameter) {
      return findParameterSansNull((PsiParameter) element);
    } else if (element instanceof PsiMethod) {
      return findMethodSansNull((PsiMethod) element);
    } else if (element instanceof PsiField) {
      return findFieldSansNull((PsiField) element);
    } else if (element instanceof PsiClass) {
      return findClassSansNull((PsiClass) element);
    } else {
      return null;
    }
  }

  @Nullable
  static PsiAnnotation findParameterSansNull(PsiParameter psiParameter) {
    if (psiParameter == null) {
      return null;
    }
    return or(getSansNull(psiParameter),
        () -> findMethodSansNull(PsiTreeUtil.getParentOfType(psiParameter, PsiMethod.class)));
  }

  @Nullable
  static PsiAnnotation findMethodSansNull(@Nullable PsiMethod psiMethod) {
    if (psiMethod == null) {
      return null;
    }
    var sansNull = or(getSansNull(psiMethod),
        () -> findClassSansNull(PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class)));
    if (sansNull == null) {
      return null;
    }
    // now check the overridden methods; if any of them is not affected by SansNull, we won't infer any @NotNull
    if (Stream.of(psiMethod.findSuperMethods()).map(SansNullPsi::findMethodSansNull).anyMatch(Objects::isNull)) {
      return null;
    }
    return sansNull;
  }

  @Nullable
  static PsiAnnotation findFieldSansNull(@Nullable PsiField psiField) {
    if (psiField == null) {
      return null;
    }
    return or(getSansNull(psiField),
        () -> findClassSansNull(PsiTreeUtil.getParentOfType(psiField, PsiClass.class)));
  }

  @Nullable
  static PsiAnnotation findClassSansNull(@Nullable PsiClass psiClass) {
    if (psiClass == null) {
      return null;
    }
    var notNull = getSansNull(psiClass);
    if (notNull != null) {
      return notNull;
    }
    var outer = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class);
    if (outer != null) {
      notNull = findClassSansNull(outer);
      if (notNull != null) {
        return notNull;
      }
    }
    var psiDir = Optional.ofNullable(psiClass.getContainingFile())
        .map(PsiFile::getContainingDirectory)
        .orElse(null);
    if (psiDir == null) {
      return null;
    }
    PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDir);
    if (psiPackage == null) {
      return null;
    }
    return findPackageSansNull(psiPackage);
  }

  @Nullable
  static PsiAnnotation findPackageSansNull(@NotNull PsiPackage psiPackage) {
    return getSansNull(psiPackage);
  }

  @Nullable
  static PsiAnnotation getSansNull(@Nullable PsiModifierListOwner element) {
    return element == null ? null : AnnotationUtil.findAnnotation(element, Names.SansNull.CLASS);
  }

  static boolean isNullAnnotatable(@Nullable PsiElement element) {
    if (element instanceof PsiParameter) {
      return isNullableType(((PsiParameter) element).getType());
    } else if (element instanceof PsiMethod) {
      return isNullableType(((PsiMethod) element).getReturnType());
    } else if (element instanceof PsiField) {
      return isNullableType(((PsiField) element).getType());
    } else {
      return false;
    }
  }

  @Nullable
  static PsiAnnotation createNotNull(@NotNull PsiModifierListOwner target, @Nullable PsiAnnotation sansNull) {
    if (sansNull == null) {
      return null;
    } else {
      return PsiElementFactory.getInstance(target.getProject()).createAnnotationFromText(
          "@" + defaultNotNull(target), target);
    }
  }

  @NotNull
  static String defaultNotNull(@NotNull PsiElement context) {
    return NullableNotNullManager.getInstance(context.getProject()).getDefaultNotNull();
  }

  private static boolean isNullableType(@Nullable PsiType type) {
    return type != null && !(type instanceof PsiPrimitiveType);
  }

  @Nullable
  private static PsiAnnotation or(@Nullable PsiAnnotation first, @NotNull Supplier<@Nullable PsiAnnotation> second) {
    return first != null ? first : second.get();
  }
}
