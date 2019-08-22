import com.laofeizhu.rpc.core.Utils;

import java.lang.reflect.Method;

/**
 * @author 老肥猪
 * @since 2019/8/20
 */
public class DemoTest {
    public static void main(String[] args) throws Exception {
        TestServiceImpl testService = new TestServiceImpl();
        Method method = testService.getClass().getMethod("test", String.class, TestBean.class);
        String s = Utils.buildIdentify(TestService.class, method);
        System.out.println(s);
    }
}
