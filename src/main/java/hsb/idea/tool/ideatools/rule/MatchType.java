package hsb.idea.tool.ideatools.rule;

/**
 * @author 胡帅博
 * @date 2022/10/20 14:02
 */
public enum MatchType {

    AUTO(0),

    CONTROLLER(1),

    SERVICE(2),


    SERVICEIMPL(3),


    MAPPER(4);

    private final int key;

    MatchType(int key) {
        this.key = key;
    }

}
