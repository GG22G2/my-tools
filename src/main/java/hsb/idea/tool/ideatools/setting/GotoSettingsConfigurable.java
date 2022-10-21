package hsb.idea.tool.ideatools.setting;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import hsb.idea.tool.ideatools.service.GotoService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author 胡帅博
 * @date 2022/10/20 17:51
 */
public class GotoSettingsConfigurable implements Configurable {

    private ClassMatchRuleSettingForm mySettingsComponent;

    private final Project project;

    GotoSettingsState settings;

    public GotoSettingsConfigurable(Project project) {
        this.project = project;
        settings = GotoSettingsState.getInstance(project);
    }

    //@Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "跳转配置";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.mapperMatchRule;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new ClassMatchRuleSettingForm();

        mySettingsComponent.testBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = mySettingsComponent.test.getText();
                GotoService service = GotoService.getInstance(project);
                mySettingsComponent.testShow.setText("");
                String test = service.test(text);
                mySettingsComponent.testShow.setText(test);
            }
        });

        return mySettingsComponent.mainPanel;
    }

    @Override
    public boolean isModified() {
//        AppSettingsState settings = AppSettingsState.getInstance();
//        boolean modified = !mySettingsComponent.getUserNameText().equals(settings.userId);
//        modified |= mySettingsComponent.getIdeaUserStatus() != settings.ideaStatus;
        // return modified;
        return true;
    }

    @Override
    public void apply() {
        settings.controllerMatchRule = mySettingsComponent.controllerMatchRule.getText();
        settings.controllerFormatRule = mySettingsComponent.controllerFormatRule.getText();
        settings.serviceMatchRule = mySettingsComponent.serviceMatchRule.getText();
        settings.serviceFormatRule = mySettingsComponent.serviceFormatRule.getText();
        settings.serviceImplMatchRule = mySettingsComponent.serviceImplMatchRule.getText();
        settings.serviceImplFormatRule = mySettingsComponent.serviceImplFormatRule.getText();
        settings.mapperMatchRule = mySettingsComponent.mapperMatchRule.getText();
        settings.mapperFormatRule = mySettingsComponent.mapperFormatRule.getText();

        GotoService service = GotoService.getInstance(project);
        service.init();
        GotoService.getInstance(project);
    }

    @Override
    public void reset() {
        mySettingsComponent.controllerMatchRule.setText(settings.controllerMatchRule);
        mySettingsComponent.controllerFormatRule.setText(settings.controllerFormatRule);
        mySettingsComponent.serviceMatchRule.setText(settings.serviceMatchRule);
        mySettingsComponent.serviceFormatRule.setText(settings.serviceFormatRule);
        mySettingsComponent.serviceImplMatchRule.setText(settings.serviceImplMatchRule);
        mySettingsComponent.serviceImplFormatRule.setText(settings.serviceImplFormatRule);
        mySettingsComponent.mapperMatchRule.setText(settings.mapperMatchRule);
        mySettingsComponent.mapperFormatRule.setText(settings.mapperFormatRule);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
