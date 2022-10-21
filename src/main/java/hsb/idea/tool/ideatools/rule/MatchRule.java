package hsb.idea.tool.ideatools.rule;

import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * @author 胡帅博
 * @date 2022/10/20 13:45
 */
public class MatchRule {

    public MatchType type;
    public String ruleRegex;
    public Pattern pattern;


    public MessageFormat format;
    public MatchRule(MatchType type, String ruleRegex, String format) {
        this.type = type;
        this.ruleRegex = ruleRegex;
        this.format = new MessageFormat(format);
        pattern = Pattern.compile(ruleRegex);
    }


}
