package hsb.idea.tool.ideatools.rule;

import java.util.Arrays;

/**
 * @author 胡帅博
 * @date 2022/10/20 13:56
 */
public class MatchResult {

    public MatchType type;

    public String[] params;

    public MatchResult(MatchType type, String[] params) {
        this.type = type;

        this.params = params;
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "type=" + type +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
