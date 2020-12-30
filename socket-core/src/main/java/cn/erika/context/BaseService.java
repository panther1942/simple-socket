package cn.erika.context;

import cn.erika.config.GlobalSettings;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.exception.UnsupportedAlgorithmException;
import cn.erika.util.exception.SerialException;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.SecurityUtils;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.Base64;

public abstract class BaseService {
    private BeanFactory beanFactory = BeanFactory.getInstance();
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Charset charset = GlobalSettings.charset;
    protected Base64.Encoder encoder = Base64.getEncoder();
    protected Base64.Decoder decoder = Base64.getDecoder();

    protected <T> T getBean(Class<?> clazz) throws BeanException {
        return beanFactory.getBean(clazz);
    }

    protected void addBean(Class<?> clazz, Object object) {
        beanFactory.addBean(clazz, object);
    }

    protected <T> T createBean(Class<?> clazz, Object... args) throws BeanException {
        return beanFactory.createBean(clazz, args);
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
        return new String(decryptWithRsa(data, privateKey));
    }
}
