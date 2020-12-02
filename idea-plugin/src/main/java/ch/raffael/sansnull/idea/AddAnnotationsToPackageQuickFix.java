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

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.AddAnnotationPsiFix;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.CreatePackageInfoAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

class AddAnnotationsToPackageQuickFix implements LocalQuickFix, HighPriorityAction {

  @NotNull
  private final String annotationFqn;
  @NotNull
  private final SmartPsiElementPointer<PsiElement> source;
  @NotNull
  private final AddAnnotationPsiFix addAnnotationsFix;


  AddAnnotationsToPackageQuickFix(@NotNull String annotationFqn,
                                  @NotNull PsiElement source,
                                  @NotNull PsiPackage psiPackage) {
    this.annotationFqn = annotationFqn;
    this.source = SmartPointerManager.createPointer(source);
    addAnnotationsFix = new AddAnnotationPsiFix(Names.SansNull.CLASS, psiPackage, PsiNameValuePair.EMPTY_ARRAY);
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return addAnnotationsFix.getFamilyName();
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }


  @Override
  @NotNull
  public String getName() {
    return addAnnotationsFix.getName();
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var packageInfo = ensurePackageInfoFile();
    if (packageInfo == null) {
      return;
    }
    var packageStatement = packageInfo.getPackageStatement();
    if (packageStatement == null) {
      return;
    }
    if (!FileModificationService.getInstance().preparePsiElementForWrite(packageInfo)) {
      return;
    }
    PsiAnnotation annotation = JavaPsiFacade.getElementFactory(project).createAnnotationFromText(
        "@" + annotationFqn, packageStatement.getContext());
    PsiElement addedAnnotation = packageInfo.addBefore(annotation, packageStatement);
    JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedAnnotation);
  }

  @Nullable
  private PsiJavaFile ensurePackageInfoFile() {
    var dir = Optional.ofNullable(source.getContainingFile()).map(PsiFile::getContainingDirectory).orElse(null);
    if (dir == null) {
      return null;
    }
    var packageInfo = dir.findFile(PsiPackage.PACKAGE_INFO_FILE);
    if (packageInfo == null) {
      createPackageInfoFile();
      packageInfo = dir.findFile(PsiPackage.PACKAGE_INFO_FILE);
    }
    return packageInfo instanceof PsiJavaFile ? (PsiJavaFile) packageInfo : null;
  }

  private void createPackageInfoFile() {
    DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(context -> {
      AnActionEvent event =
          new AnActionEvent(null, context, "", new Presentation(), ActionManager.getInstance(), 0);
      new CreatePackageInfoAction().actionPerformed(event);
    });
  }
}
