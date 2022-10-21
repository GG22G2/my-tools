package hsb.idea.tool.ideatools.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * @author 胡帅博
 * @date 2022/10/20 17:43
 */
@State(
    name = "gotoSettings",
    storages = @Storage("$WORKSPACE_FILE$")
)
public class GotoSettingsState implements PersistentStateComponent<GotoSettingsState>, Serializable {

    public String controllerMatchRule;

    public String serviceMatchRule;
    public String serviceImplMatchRule;
    public String mapperMatchRule;

    public String controllerFormatRule;
    public String serviceFormatRule;
    public String serviceImplFormatRule;
    public String mapperFormatRule;

    public GotoSettingsState(){

    }
    public static GotoSettingsState getInstance(@NotNull Project project) {
        GotoSettingsState settings =project.getService(GotoSettingsState.class);
        if (settings.controllerMatchRule == null) {
            settings.controllerMatchRule = "([a-zA-Z_\\.]+)\\.controller\\.([a-zA-Z]+)Controller$";
        }
        if (settings.serviceMatchRule == null) {
            settings.serviceMatchRule = "([a-zA-Z_\\.]+)\\.service\\.I([a-zA-Z_]+)Service$";
        }
        if (settings.serviceImplMatchRule == null) {
            settings.serviceImplMatchRule = "([a-zA-Z_\\.]+)\\.service\\.impl\\.([a-zA-Z_]+)ServiceImpl$";
        }
        if (settings.mapperMatchRule == null) {
            settings.mapperMatchRule = "([a-zA-Z_\\.]+)\\.mapper\\.([a-zA-Z_]+)Mapper$";
        }


        if (settings.controllerFormatRule == null) {
            settings.controllerFormatRule = "{1}.controller.{2}Controller";
        }
        if (settings.serviceFormatRule == null) {
            settings.serviceFormatRule = "{1}.service.I{2}Service";
        }
        if (settings.serviceImplFormatRule == null) {
            settings.serviceImplFormatRule = "{1}.service.impl.{2}ServiceImpl";
        }
        if (settings.mapperFormatRule == null) {
            settings.mapperFormatRule = "{1}.mapper.{2}Mapper";
        }


        return settings;
    }


//    public static AppSettingsState getInstance() {
//        AppSettingsState settings = ApplicationManager.getApplication().getService(AppSettingsState.class);
//        return settings;
//    }

    @Nullable
    @Override
    public GotoSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GotoSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
