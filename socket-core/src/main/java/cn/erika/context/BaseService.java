package cn.erika.context;

import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.util.exception.SerialException;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.SecurityUtils;
import cn.erika.util.string.SerialUtils;

public abstract class BaseService {
    private BeanFactory beanFactory = BeanFactory.getInstance();
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected <T> T getBean(Class<?> clazz) throws BeanException {
        return beanFactory.getBean(clazz);
    }

    protected void addBean(Class<?> clazz, Object object) {
        beanFactory.addBean(clazz, object);
    }

    protected <T> T createBean(Class<?> clazz, Object... args) throws BeanException {
        return beanFactory.createBean(clazz, args);
    }

    protected byte[] encryptWithRsa(Object object, byte[] publicKey) throws SerialException {
        return SecurityUtils.encrypt(SerialUtils.serialObject(object), publicKey);
    }

    protected <T> T decryptWithRsa(byte[] data, byte[] privateKey) throws SerialException {
        return SerialUtils.serialObject(SecurityUtils.decrypt(data, privateKey));
    }
}