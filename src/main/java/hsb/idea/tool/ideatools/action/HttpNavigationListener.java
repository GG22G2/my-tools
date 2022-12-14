package hsb.idea.tool.ideatools.action;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys;
import com.intellij.psi.impl.search.JavaSourceFilterScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.FindClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.spring.model.utils.AntPathMatcher;
import com.intellij.util.Processor;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.awt.windows.WComponentPeer;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

/**
 * @author ?????????
 * @date 2022/10/21 9:47
 */
public class HttpNavigationListener extends AnAction {

    volatile ServerSocket serverSocket = null;


    public HttpNavigationListener() {
        initListener();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

//        Project project = e.getProject();
//        PsiElement matchPsiElement = findMatchPsiElement("hello/h4");
//        if (matchPsiElement != null) {
//            navigate(matchPsiElement, false);
//        }

        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        for (Project openProject : openProjects) {
            WindowManager wm = WindowManager.getInstance();
            Window window = wm.suggestParentWindow(openProject);
            long hwnd = getHWnd(window);
            System.out.println(openProject.getName());
            System.out.println(Long.toString(hwnd, 16));
        }


    }


    public boolean isSpringMvcController(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        PsiAnnotation annotation1 = psiClass.getAnnotation("org.springframework.stereotype.Controller");
        PsiAnnotation annotation2 = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
        if (annotation != null || annotation1 != null || annotation2 != null) {
            return true;
        }
        return false;
    }

    public @Nullable PsiElement analysisSpringMvcController(PsiClass controllerClass, PsiConstantEvaluationHelper evaluationHelper, String lookPath) {
        PsiAnnotation annotation = controllerClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        String path1 = annotation == null ? "" : parseRequestMappingValue(annotation, evaluationHelper);

        PsiMethod[] methods = controllerClass.getMethods();
        for (PsiMethod method : methods) {
            PsiAnnotation[] annotations = method.getAnnotations();
            for (PsiAnnotation psiAnnotation : annotations) {
                if (Objects.equals(psiAnnotation.getQualifiedName(), "org.springframework.web.bind.annotation.PostMapping")
                        || Objects.equals(psiAnnotation.getQualifiedName(), "org.springframework.web.bind.annotation.GetMapping")
                        || Objects.equals(psiAnnotation.getQualifiedName(), "org.springframework.web.bind.annotation.RequestMapping")
                ) {
                    String path = getSanitizedPath(path1 + parseRequestMappingValue(psiAnnotation, evaluationHelper));
                    if (match(path, lookPath)) {
                        return method;
                    }

                }
            }
        }
        return null;
    }

    private boolean match(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return true;
        } else if (AntPathMatcher.isPattern(pattern)) {
            return AntPathMatcher.match(pattern, lookupPath);
        }
        return false;
    }

    private String getSanitizedPath(String path) {
        StringTokenizer stringTokenizer = new StringTokenizer(path, "/");
        StringJoiner joiner = new StringJoiner("/");
        while (stringTokenizer.hasMoreTokens()) {
            joiner.add(stringTokenizer.nextToken());
        }
        return "/" + joiner;
    }


    /**
     * PostMapping  GetMapping RequestMapping??????????????????value
     * <p>
     * ??????value???path??????????????????????????????????????????????????????????????????????????????
     */
    private @NotNull String parseRequestMappingValue(PsiAnnotation requestMappingAnnotation, PsiConstantEvaluationHelper evaluationHelper) {
        PsiAnnotationParameterList parameterList = requestMappingAnnotation.getParameterList();
        PsiNameValuePair[] attributes = parameterList.getAttributes();
        for (PsiNameValuePair attribute : attributes) {
            if ("value".equals(attribute.getAttributeName())) {
                String path = attribute.getLiteralValue();
                if (path == null) {
                    Object what = evaluationHelper.computeConstantExpression(attribute.getValue());
                    if (what != null) {
                        path = what.toString();
                    }
                }

                StringBuilder builder = null;

                //?????????????????????????????????????????????/?????????????????????
                if (path == null || path.length() == 0) {
                    return "";
                } else if (!path.startsWith("/")) {
                    builder = new StringBuilder("/").append(path);
                } else {
                    builder = new StringBuilder(path);
                }

                //???{pathVariable}?????????????????????*,????????????antPath????????????
                int index = 0;
                while (true) {
                    int pathVariableStart = builder.indexOf("{", index);
                    int pathVariableEnd = builder.indexOf("}", index);
                    if (pathVariableStart != -1 && pathVariableEnd != -1) {
                        builder.replace(pathVariableStart, pathVariableEnd + 1, "*");
                    } else {
                        break;
                    }
                }

                return builder.toString();
            }
        }
        return "";
    }


    /**
     * ???????????????????????????????????????????????????????????????
     */
    public List<PsiClass> getProjectJavaClass(Project project) {
        List<PsiClass> classList = new LinkedList<>();

        /**
         * ?????????????????????????????????java??????
         * ??????????????????api
         *
         * ??????????????????????????????
         * ?????????:
         *         JavaFullClassNameIndex.getInstance().getAllKeys(project)
         *         JavaFullClassNameIndex.getInstance().get()
         * ???????????????????????????????????????jar??????
         *
         *
         * ????????????????????????
         *
         * */

        VirtualFile[] contentSourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile contentSourceRoot : contentSourceRoots) {
            /**
             * contentSourceRoot????????????????????????????????????????????????????????????
             * idea????????? ?????????????????????????????????java??? ??????????????????????????????????????????????????????????????????????????????
             * ????????????????????????java???????????????????????????????????????api??????????????????java????????????
             *
             * */
            if ("java".equals(contentSourceRoot.getName())) {
                PsiDirectory sourceDirectory = PsiManager.getInstance(project).findDirectory(contentSourceRoot);
                if (sourceDirectory != null) {
                    sourceDirectory.accept(new PsiRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitElement(@NotNull PsiElement element) {
                            if (element instanceof PsiClass) {
                                classList.add((PsiClass) element);
                            } else {
                                super.visitElement(element);
                            }
                        }
                    });
                }
            }
        }
        return classList;
    }

    public PsiElement findMatchPsiElement(String lookPath) {
        lookPath = getSanitizedPath(lookPath);
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();
        for (Project project : openProjects) {
            PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();
            List<PsiClass> classList = getProjectJavaClass(project);
            for (PsiClass psiClass : classList) {
                if (isSpringMvcController(psiClass)) {
                    //System.out.println(psiClass.getQualifiedName());
                    PsiElement element = analysisSpringMvcController(psiClass, evaluationHelper, lookPath);
                    if (element != null) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    public void navigate(PsiElement element, boolean focus) {
        if (focus) {
            WindowManager wm = WindowManager.getInstance();
            Window window = wm.suggestParentWindow(element.getProject());
            long hWnd = getHWnd(window);
            showWindow(hWnd);
        }
        NavigationUtil.activateFileWithPsiElement(element);

        NavigationUtil.activateFileWithPsiElement(element);
    }

    private void initListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int firstPort = 30000;
                while (firstPort < 30500) {
                    try {
                        ServerSocket server = new ServerSocket(firstPort, 50);
                        serverSocket = server;
                        System.out.println("create port:" + firstPort + " listener");
                        break;
                    } catch (IOException e) {
                        firstPort += 4;
                    }
                }

                while (true) {
                    try (Socket socket = serverSocket.accept()) {
                        byte[] data = new byte[1];
                        int read = socket.getInputStream().read(data);
                        if (data[0] == 0x01) {
                            socket.getOutputStream().write(new byte[]{0x10});
                        } else {
                            socket.close();
                            continue;
                        }

                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        while (true) {
                            String url = reader.readLine();
                            System.out.println(url);
                            ApplicationManager.getApplication().runReadAction(new Runnable() {
                                @Override
                                public void run() {
                                    long startNanos_17_250 = System.nanoTime();
                                    PsiElement matchPsiElement = findMatchPsiElement(url);
                                    long endNanos_17_252 = System.nanoTime();
                                    System.out.println((endNanos_17_252 - startNanos_17_250) / 1000000.0);
                                    if (matchPsiElement != null) {
                                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                navigate(matchPsiElement, true);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * ???????????????????????????
     * https://www.jianshu.com/p/886565057994/
     * <p>
     * HWND hForeWnd = ::GetForegroundWindow();
     * DWORD dwForeID = ::GetWindowThreadProcessId(hForeWnd, NULL);
     * DWORD dwCurID = ::GetCurrentThreadId();
     * ::AttachThreadInput(dwCurID, dwForeID, TRUE);
     * ::ShowWindow(wnd_, SW_SHOWNORMAL);
     * ::SetWindowPos(wnd_, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);
     * ::SetWindowPos(wnd_, HWND_NOTOPMOST, 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);
     * ::SetForegroundWindow(wnd_);
     * ::AttachThreadInput(dwCurID, dwForeID, FALSE);
     * <p>
     * hWndInsertAfter ???????????????:
     * HWND_TOP = 0; {?????????}
     * HWND_BOTTOM = 1; {?????????}
     * HWND_TOPMOST = HWND(-1); {?????????, ?????????????????????????????????}
     * HWND_NOTOPMOST = HWND(-2); {?????????, ?????????????????????????????????}
     * <p>
     * uFlags ???????????????:
     * SWP_NOSIZE = 1; {?????? cx???cy, ????????????}
     * SWP_NOMOVE = 2; {?????? X???Y, ???????????????}
     * SWP_NOZORDER = 4; {?????? hWndInsertAfter, ?????? Z ??????}
     * SWP_NOREDRAW = 8; {?????????}
     * SWP_NOACTIVATE = $10; {?????????}
     */
    public void showWindow(long hwnd) {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hWnd = new WinDef.HWND(new Pointer(hwnd));
        if (user32.IsWindowVisible(hWnd)) {
            //WINDOWPLACEMENT??????????????????????????????????????????????????????????????????????????????????????????
            WinUser.WINDOWPLACEMENT windowPlacement = new WinUser.WINDOWPLACEMENT();
            user32.GetWindowPlacement(hWnd,windowPlacement);
            //??????????????????
            if (windowPlacement.showCmd == 2){
                //??????????????????????????????????????? User32.SW_SHOWNOACTIVATE????????????????????????????????????
                user32.ShowWindow(hWnd, User32.SW_SHOWNOACTIVATE);
            }else {
                //?????????????????????????????????????????????????????????
                user32.ShowWindow(hWnd, User32.SW_SHOW);
            }
            WinDef.HWND hForeWnd = user32.GetForegroundWindow();
            int dwForeID = user32.GetWindowThreadProcessId(hForeWnd, null);
            int dwCurID = Kernel32.INSTANCE.GetCurrentThreadId();

            /**
             * ????????????AttachThreadInput
             *
             * ????????????????????????idea??????????????????(??????????????????????????????)
             * ????????????????????????????????????????????????????????????????????????tab+alt????????????webstorm???????????????????????????
             *
             * */
            user32.AttachThreadInput(new WinDef.DWORD(dwForeID),new WinDef.DWORD(dwCurID),true);

            //???????????????????????????????????????????????????????????????????????????????????????
            user32.SetWindowPos(hWnd, new WinDef.HWND(new Pointer(-1)), 0, 0, 0, 0
                    , User32.SWP_NOSIZE | User32.SWP_NOMOVE);
            user32.SetWindowPos(hWnd, new WinDef.HWND(new Pointer(-2)), 0, 0, 0, 0
                    , User32.SWP_NOSIZE | User32.SWP_NOMOVE);
            user32.SetForegroundWindow(hWnd);

            user32.AttachThreadInput(new WinDef.DWORD(dwForeID),new WinDef.DWORD(dwCurID),false);
        }
    }

    public static long getHWnd(Window window) {
        try {
            Field peer = Window.class.getSuperclass().getSuperclass().getDeclaredField("peer");
            peer.setAccessible(true);
            WComponentPeer value = (WComponentPeer) peer.get(window);
            return value.getHWnd();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }
        return 1;
    }

}