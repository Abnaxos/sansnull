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

import com.intellij.codeInsight.Nullability;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.codeInsight.intention.AddAnnotationPsiFix;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MissingNullabilityAnnotationInspection extends LocalInspectionTool {

  // public for serialisation
  public boolean enableAnnotatePackageFix = true;
  // public for serialisation
  public boolean enableAnnotateClassFix = true;
  // public for serialisation
  public boolean enableAnnotateMethodFix = false;
  // public for serialisation
  public boolean enableAnnotateNullableFix = true;
  // public for serialisation
  public boolean enableAnnotateNotNullFix = false;

  @Override
  @Nullable
  public JComponent createOptionsPanel() {
    JPanel main = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, true, false));
    JPanel sansNull = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, true, false));
    sansNull.setBorder(BorderFactory.createTitledBorder("@SansNull"));
    sansNull.add(createCheckbox("Enable annotate package fix",
        () -> enableAnnotatePackageFix, e -> enableAnnotatePackageFix = e));
    sansNull.add(createCheckbox("Enable annotate class fix",
        () -> enableAnnotateClassFix, e -> enableAnnotateClassFix = e));
    sansNull.add(createCheckbox("Enable annotate method fix",
        () -> enableAnnotateMethodFix, e -> enableAnnotateMethodFix = e));
    main.add(sansNull);
    main.add(createCheckbox("Enable annotate as @Nullable fix",
        () -> enableAnnotateNullableFix, e -> enableAnnotateNullableFix = e));
    main.add(createCheckbox("Enable annotate as @NotNull fix",
        () -> enableAnnotateNotNullFix, e -> enableAnnotateNotNullFix = e));
    return main;
  }

  private JComponent createCheckbox(String text, Supplier<Boolean> getter, Consumer<Boolean> setter) {
    var checkbox = new JCheckBox(text);
    checkbox.setSelected(getter.get());
    checkbox.addChangeListener(e -> setter.accept(checkbox.isSelected()));
    JPanel leftAlign = new JPanel(new BorderLayout(0, 0));
    leftAlign.add(checkbox, BorderLayout.WEST);
    return leftAlign;
  }

  @Override
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if ((element instanceof PsiParameter || element instanceof PsiMethod || element instanceof PsiField)
            && SansNullPsi.isNullAnnotatable(element)) {
          var nullability = NullableNotNullManager.getInstance(element.getProject())
              .findEffectiveNullabilityInfo((PsiModifierListOwner) element);
          if (nullability == null || nullability.getNullability() == Nullability.UNKNOWN) {
            holder.registerProblem(
                Objects.requireNonNullElse(((PsiNameIdentifierOwner) element).getNameIdentifier(), element),
                "Missing nullability annotation",
                List.of(
                    addSansNullQuickFix(element, findPackage(element)),
                    addSansNullQuickFix(element, findClass(element)),
                    addSansNullQuickFix(element, findMethod(element)),
                    enableAnnotateNullableFix
                        ? Optional.ofNullable(AddAnnotationPsiFix.createAddNullableFix((PsiModifierListOwner) element))
                        : Optional.<LocalQuickFix>empty(),
                    enableAnnotateNotNullFix
                        ? Optional.ofNullable(AddAnnotationPsiFix.createAddNotNullFix((PsiModifierListOwner) element))
                        : Optional.<LocalQuickFix>empty())
                    .stream().flatMap(Optional::stream).toArray(LocalQuickFix[]::new));
          }
        }
      }
    };
  }

  @Nullable
  private PsiClass findClass(PsiElement element) {
    var current = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
    while (true) {
      var outer = PsiTreeUtil.getParentOfType(current, PsiClass.class, true);
      if (outer == null) {
        return current;
      } else {
        current = outer;
      }
    }
  }

  @Nullable
  private PsiMethod findMethod(PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PsiMethod.class, false);
  }

  @Nullable
  private PsiPackage findPackage(PsiElement element) {
    var psiDir = Optional.ofNullable(element.getContainingFile())
        .map(PsiFile::getContainingDirectory)
        .orElse(null);
    if (psiDir == null) {
      return null;
    }
    return JavaDirectoryService.getInstance().getPackageInSources(psiDir);
  }

  @NotNull
  private Optional<LocalQuickFix> addSansNullQuickFix(@NotNull PsiElement source,
                                                      @Nullable PsiModifierListOwner target) {
    if (target == null) {
      return Optional.empty();
    }
    if (enableAnnotatePackageFix && target instanceof PsiPackage) {
      return Optional.of(new AddAnnotationsToPackageQuickFix(Names.SansNull.CLASS, source, (PsiPackage) target));
    } else if (enableAnnotateClassFix && target instanceof PsiClass){
      return Optional.of(new AddAnnotationPsiFix(Names.SansNull.CLASS, target, PsiNameValuePair.EMPTY_ARRAY));
    } else if (enableAnnotateMethodFix && target instanceof PsiMethod) {
      return Optional.of(new AddAnnotationPsiFix(Names.SansNull.CLASS, target, PsiNameValuePair.EMPTY_ARRAY));
    } else {
      return Optional.empty();
    }
  }
}
