package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.RedisConsentRepository;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link CasConsentRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.consent.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casConsentRedisConfiguration", proxyBeanMethods = false)
public class CasConsentRedisConfiguration {

    @Bean
    public ConsentRepository consentRepository(
        @Qualifier("consentRedisTemplate")
        final RedisTemplate consentRedisTemplate) {
        return new RedisConsentRepository(consentRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisConsentConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisConsentConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getConsent()
            .getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "consentRedisTemplate")
    public RedisTemplate consentRedisTemplate(
        @Qualifier("redisConsentConnectionFactory")
        final RedisConnectionFactory redisConsentConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisConsentConnectionFactory);
    }
}
