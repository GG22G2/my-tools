package hsb.idea.tool.ideatools.service;


import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import hsb.idea.tool.ideatools.rule.MatchResult;
import hsb.idea.tool.ideatools.rule.MatchRule;
import hsb.idea.tool.ideatools.rule.MatchType;
import hsb.idea.tool.ideatools.setting.GotoSettingsState;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author 胡帅博
 * @date 2022/10/18 17:55
 */
public class GotoService implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Project project;

    List<MatchRule> rules;

    public GotoService(Project project) {
        this.project = project;
        init();
    }

    public void init() {
        GotoSettingsState settings = GotoSettingsState.getInstance(project);
        //todo 这里可以配置到setting里，实现自定义
        String controllerMatchRule = settings.controllerMatchRule;
        String serviceMatchRule = settings.serviceMatchRule;
        String serviceImplMatchRule = settings.serviceImplMatchRule;
        String mapperMatchRule = settings.mapperMatchRule;
        List<MatchRule> rules = new ArrayList<>(4);
        rules.add(new MatchRule(MatchType.CONTROLLER, controllerMatchRule,  settings.controllerFormatRule));
        rules.add(new MatchRule(MatchType.SERVICE, serviceMatchRule, settings.serviceFormatRule));
        rules.add(new MatchRule(MatchType.SERVICEIMPL, serviceImplMatchRule,  settings.serviceImplFormatRule));
        rules.add(new MatchRule(MatchType.MAPPER, mapperMatchRule, settings.mapperFormatRule));
        this.rules = rules;
    }


    public static GotoService getInstance(@NotNull Project project) {
        return project.getService(GotoService.class);
    }


    public String test(String className) {
        MatchResult matchResult = tryMatch(className, this.rules);
        if (matchResult != null) {
            List<String> associationClass = getAssociationClass(matchResult, this.rules);
            return String.join("\n", associationClass);
        }
        return "无";
    }

    public MatchResult identifyClass(@NotNull PsiClass clazz) {
        //controller service serviceImpl entity mapper
        return tryMatch(clazz.getQualifiedName(), this.rules);
    }


    public static MatchResult tryMatch(String name, List<MatchRule> rules) {
        try {
            for (MatchRule rule : rules) {
                Matcher matcher = rule.pattern.matcher(name);
                if (matcher.find()) {

                    int len = matcher.groupCount()+1;
                    String[] params = new String[len];

                    for(int i = 0; i < len; i++) {
                        params[i] = matcher.group(i);
                    }

                    return new MatchResult(rule.type,params);
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public void findAssociationClass(MatchResult matchResult, List<PsiClass> result) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);
        List<String> associationClass = getAssociationClass(matchResult, this.rules);
        associationClass.forEach(x -> {
            PsiClass aClass = javaPsiFacade.findClass(x, globalSearchScope);
            if (aClass != null) {
                result.add(aClass);
            }
        });
        //todo 找一下mapper xml文件

    }

    public List<String> getAssociationClass(MatchResult match, List<MatchRule> rules) {
        List<String> className = new ArrayList<>(rules.size());
        String[] params = match.params;
        for (MatchRule rule : rules) {
            MessageFormat format = rule.format;
            String name = format.format(params);
            className.add(name);
        }
        return className;
    }


}
