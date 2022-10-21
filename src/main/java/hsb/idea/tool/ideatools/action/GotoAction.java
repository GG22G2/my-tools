package hsb.idea.tool.ideatools.action;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;
import hsb.idea.tool.ideatools.rule.MatchResult;
import hsb.idea.tool.ideatools.service.GotoService;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 胡帅博
 * @date 2022/10/21 9:05
 */
public class GotoAction extends AnAction {

    boolean isShow = false;

    public GotoAction() {

    }

    public static class ShowItem {
        public PsiClass psiClass;

        public ShowItem(PsiClass psiClass) {
            this.psiClass = psiClass;
        }

        @Override
        public String toString() {
            return psiClass.getName();
        }
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (isShow) {
            return;
        }
        //  PsiElement element = event.getData(LangDataKeys.PSI_ELEMENT);
        PsiElement psiFile = event.getData(LangDataKeys.PSI_FILE);
        PsiClass element = PsiTreeUtil.getStubChildOfType(psiFile, PsiClass.class);
        JBPopupFactory instance = JBPopupFactory.getInstance();
        long startNanos_0_62 = System.nanoTime();
        List<ShowItem> list = findGoToItem(element);
        long endNanos_0_64 = System.nanoTime();
        System.out.println((endNanos_0_64 - startNanos_0_62) / 1000000.0);
        if (list.size() == 0) {
            return;
        }
        JBPopup popup = instance.createPopupChooserBuilder(list)
                .setItemChosenCallback(new Consumer<ShowItem>() {
                    @Override
                    public void consume(ShowItem item) {
                        PsiClass psiClass = item.psiClass;
                        // 这里跳转到选中的文件
                        NavigationUtil.activateFileWithPsiElement(psiClass);
                    }
                })
                .addListener(new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {
                        JBPopupListener.super.beforeShown(event);
                        isShow = true;
                    }
                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        JBPopupListener.super.onClosed(event);
                        isShow = false;
                    }
                })
                .createPopup();
        popup.show(new RelativePoint(MouseInfo.getPointerInfo().getLocation()));  //在鼠标位置弹出

    }


    public List<ShowItem> findGoToItem(PsiClass clazz) {
        List<ShowItem> result = new ArrayList<>();
        if (clazz != null) {
            GotoService springMvcService = GotoService.getInstance(clazz.getProject());
            MatchResult matchResult = springMvcService.identifyClass(clazz);
            if (matchResult != null) {
                List<PsiClass> list = new ArrayList<>();
                springMvcService.findAssociationClass(matchResult, list);
                result = list.stream().map(ShowItem::new).collect(Collectors.toList());
            }
        }
        return result;
    }

}
