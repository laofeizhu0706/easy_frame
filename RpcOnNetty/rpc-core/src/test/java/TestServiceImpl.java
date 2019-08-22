/**
 * @author 老肥猪
 * @since 2019/8/20
 */
public class TestServiceImpl implements TestService {
    @Override
    public String test(String test, TestBean testBean) {
        return test+":"+testBean;
    }
}
