package hsb.idea.tool.ideatools.service;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author 胡帅博
 * @date 2022/10/21 9:39
 */


public class ListenerService {


    public ListenerService(){
        System.out.println("213123");
    }


    public static ListenerService getInstance(){
        return ApplicationManager.getApplication().getService(ListenerService.class);
    }

}
