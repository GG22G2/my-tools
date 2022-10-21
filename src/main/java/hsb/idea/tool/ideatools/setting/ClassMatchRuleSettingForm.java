package hsb.idea.tool.ideatools.setting;

import javax.swing.*;

/**
 * @author 胡帅博
 * @date 2022/10/20 16:51
 */
public class ClassMatchRuleSettingForm {
    public JPanel mainPanel;
    public JTextField controllerMatchRule;
    public JTextField serviceMatchRule;
    public JTextField serviceImplMatchRule;
    public JTextField mapperMatchRule;
    public JTextField controllerFormatRule;
    public JTextField serviceFormatRule;
    public JTextField serviceImplFormatRule;
    public JTextField mapperFormatRule;
    public JTextField test;
    public JButton testBtn;
    public JTextArea testShow;
    private JTextField rootPackage;


    public ClassMatchRuleSettingForm() {

    }
}
