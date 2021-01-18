package cn.erika.context;

import cn.erika.config.GlobalSettings;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.SecurityUtils;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * 服务类的工具方法 提供了一些方法避免反复书写
 */
public abstract class BaseService {
    // 对象工厂
    private BeanFactory beanFactory = BeanFactory.getInstance();
    // 日志
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    // 字符编码
    protected Charset charset = GlobalSettings.charset;
    // Base64的解编码对象
    protected Base64.Encoder encoder = Base64.getEncoder();
    protected Base64.Decoder decoder = Base64.getDecoder();

    protected <T> T getBean(Class<?> clazz) throws BeanException {
        return beanFactory.getBean(clazz);
    }

    protected <T> T getBean(String beanName) throws BeanException {
        return beanFactory.getBean(beanName);
    }

    protected void addBean(Class<?> clazz, Object object) {
        beanFactory.addBean(clazz, object);
    }

    protected byte[] encryptWithRsa(byte[] data, byte[] publicKey) {
        try {
            return encoder.encode(SecurityUtils.encrypt(data, publicKey));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected byte[] encryptWithRsa(String data, byte[] publicKey) {
        return encryptWithRsa(data.getBytes(charset), publicKey);
    }

    protected byte[] decryptWithRsa(byte[] data, byte[] privateKey) {
        try {
            return SecurityUtils.decrypt(decoder.decode(data), privateKey);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected String decryptWithRsaToString(byte[] data, byte[] privateKey) {
        return new String(decryptWithRsa(data, privateKey), charset);
    }
}
