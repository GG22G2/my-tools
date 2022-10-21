package hsb.idea.tool.ideatools.action;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import sun.awt.windows.WComponentPeer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author 胡帅博
 * @date 2022/10/21 9:47
 */
public class HttpNavigationListener extends AnAction {

    volatile ServerSocket serverSocket = null;

    public HttpNavigationListener() {
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
                        //e.printStackTrace();
                        firstPort += 4;
                        // throw new RuntimeException(e);
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

                            ApplicationManager.getApplication().invokeLater(new Runnable() {
                                @Override
                                public void run() {

                                    Project currProject = getCurrProject();

                                    WindowManager wm = WindowManager.getInstance();
                                    Window window = wm.suggestParentWindow(currProject);
                                    // SetForegroundWindow
                                    long hWnd = getHWnd(window);
                                    //todo 这里有问题，如果存在多个project，这里展示能反映，但是不会展示出来
                                    showWindow(hWnd);
                                    JavaPsiFacade instance = JavaPsiFacade.getInstance(currProject);
                                    GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(currProject);
                                    //EditorService editorService = EditorService.getInstance(currProject);
                                    PsiClass aClass = instance.findClass("org.example.mapper.UserMapper", globalSearchScope);


                                    NavigationUtil.activateFileWithPsiElement(aClass);
                                    //editorService.scrollTo(aClass, 0);


                                    // JavaPsiFacade.getInstance()

                                    // SearchUtil.


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

    public void showWindow(long hwnd){
        User32 user32 = User32.INSTANCE;
        //HWND hWnd = user32.FindWindow(null, "plugin-test-example – test.js Administrator");

        WinDef.HWND hWnd = new WinDef.HWND(new Pointer(hwnd));

        if (user32.IsWindowVisible(hWnd)) {
            user32.ShowWindow(hWnd, User32.SW_SHOWMINIMIZED);
            user32.ShowWindow(hWnd, User32.SW_SHOWMAXIMIZED);
            user32.SetFocus(hWnd);
        }
    }

    public static long getHWnd(Window window) {
        try {
            Field peer = Window.class.getSuperclass().getSuperclass().getDeclaredField("peer");
            peer.setAccessible(true);
            WComponentPeer value = (WComponentPeer)peer.get(window);
            return value.getHWnd();
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }

        return 1;
        //return   ((WComponentPeer) f).getHWnd();;
    }



//    public static long getHWnd(Frame f) {
//
//        return   ((WComponentPeer) f).getHWnd();;
//    }


    public static Project getCurrProject() {

        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();

        if (openProjects.length > 0) {
            return openProjects[0];
        }

//        if (openProjects.length == 0) {
//            return projectManager.getDefaultProject();//没有打开项目
//        } else if (openProjects.length == 1) {
//            // 只存在一个打开的项目则使用打开的项目
//            return openProjects[0];
//        }

        //如果有项目窗口处于激活状态
//        try {
//            WindowManager wm = WindowManager.getInstance();
//            for (Project project : openProjects) {
//                Window window = wm.suggestParentWindow(project);
//                if (window != null && window.isActive()) {
//                    return project;
//                }
//            }
//        } catch (Exception ignored) {
//        }

        //否则使用默认项目
        return projectManager.getDefaultProject();
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

       // Project project = e.getProject();
       // JavaPsiFacade instance = JavaPsiFacade.getInstance(e.getProject());
       // GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);
       // EditorService editorService = EditorService.getInstance(project);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(3000);
//
//                        ApplicationManager.getApplication().invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                PsiClass aClass = instance.findClass("org.example.mapper.UserMapper", globalSearchScope);
//                                editorService.scrollTo(aClass, 0);
//                            }
//                        });
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }).start();


//


    }
}