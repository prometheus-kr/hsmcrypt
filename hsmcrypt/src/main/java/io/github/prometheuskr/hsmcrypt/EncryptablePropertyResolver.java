package io.github.prometheuskr.hsmcrypt;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * Property resolver that automatically decrypts HCENC() formatted property
 * values.
 * <p>
 * This resolver wraps Spring's PropertySource to perform decryption at
 * getProperty() call time.
 * It uses lazy initialization to obtain HsmCryptHelper bean after all beans are
 * initialized,
 * avoiding circular dependency issues during Spring context initialization.
 * <p>
 * Package-private: Only accessible within hsmcrypt package.
 * Automatically registered by {@link HsmCryptAutoConfiguration} when encryption
 * is enabled.
 * 
 * @author Prometheus
 * @see HsmCryptAutoConfiguration#encryptablePropertyResolver()
 */
class EncryptablePropertyResolver implements BeanFactoryPostProcessor, Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var environment = beanFactory.getBean(ConfigurableEnvironment.class);

        // Wrap all PropertySources with EncryptablePropertySourceWrapper
        // HsmCryptHelper is obtained lazily at actual usage time
        environment.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof EnumerablePropertySource &&
                    !(propertySource instanceof EncryptablePropertySourceWrapper)) {
                environment.getPropertySources().replace(
                        propertySource.getName(),
                        new EncryptablePropertySourceWrapper<>((EnumerablePropertySource<?>) propertySource,
                                beanFactory));
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * PropertySource wrapper that automatically decrypts HCENC() formatted values.
     * <p>
     * This wrapper uses lazy initialization to obtain HsmCryptHelper bean,
     * ensuring it's only retrieved after all beans are fully initialized.
     * 
     * @param <T>
     *            the source type
     */
    private static class EncryptablePropertySourceWrapper<T> extends EnumerablePropertySource<T> {
        private final EnumerablePropertySource<T> delegate;
        private final ConfigurableListableBeanFactory beanFactory;
        private volatile HsmCryptHelper helper;

        /**
         * Creates a new wrapper for the given property source.
         * 
         * @param delegate
         *            the original property source to wrap
         * @param beanFactory
         *            the bean factory to obtain HsmCryptHelper from
         */
        public EncryptablePropertySourceWrapper(EnumerablePropertySource<T> delegate,
                ConfigurableListableBeanFactory beanFactory) {
            super(delegate.getName(), delegate.getSource());
            this.delegate = delegate;
            this.beanFactory = beanFactory;
        }

        @Override
        public Object getProperty(String name) {
            var value = delegate.getProperty(name);

            if (value instanceof String strValue) {
                // Lazy initialization of helper
                if (helper == null) {
                    synchronized (this) {
                        if (helper == null) {
                            try {
                                helper = beanFactory.getBean(HsmCryptHelper.class);
                            } catch (BeansException e) {
                                // Helper not yet available, return raw value
                                return value;
                            }
                        }
                    }
                }

                return helper.decryptIfEncrypted(strValue);
            }
            return value;
        }

        @Override
        public String[] getPropertyNames() {
            return delegate.getPropertyNames();
        }
    }
}
