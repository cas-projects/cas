package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowAction;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.rest.SwivelTuringImageGeneratorController;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SwivelConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "swivelConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Bean
    @ConditionalOnMissingBean(name = "swivelAuthenticatorFlowRegistry")
    @Autowired
    public FlowDefinitionRegistry swivelAuthenticatorFlowRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices,
        @Qualifier("flowBuilder")
        final FlowBuilder flowBuilder) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, SwivelMultifactorWebflowConfigurer.MFA_SWIVEL_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "swivelMultifactorWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer swivelMultifactorWebflowConfigurer(
        @Qualifier("swivelAuthenticatorFlowRegistry")
        final FlowDefinitionRegistry swivelAuthenticatorFlowRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val cfg = new SwivelMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry,
            swivelAuthenticatorFlowRegistry, applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelAuthenticationWebflowEventResolver")
    @Autowired
    public CasWebflowEventResolver swivelAuthenticationWebflowEventResolver(
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new SwivelAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
    }

    @Bean
    @Autowired
    public SwivelTuringImageGeneratorController swivelTuringImageGeneratorController(
        final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        return new SwivelTuringImageGeneratorController(swivel);
    }

    @Bean
    @ConditionalOnMissingBean(name = "swivelCasWebflowExecutionPlanConfigurer")
    @Autowired
    public CasWebflowExecutionPlanConfigurer swivelCasWebflowExecutionPlanConfigurer(
        @Qualifier("swivelMultifactorWebflowConfigurer")
        final CasWebflowConfigurer swivelMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(swivelMultifactorWebflowConfigurer);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelAuthenticationWebflowAction")
    @Autowired
    public Action swivelAuthenticationWebflowAction(
        @Qualifier("swivelAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver swivelAuthenticationWebflowEventResolver) {
        return new SwivelAuthenticationWebflowAction(swivelAuthenticationWebflowEventResolver);
    }

    /**
     * The swivel multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.swivel")
    @Configuration(value = "swivelMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("swivelMultifactorWebflowConfigurer")
    public static class SwivelMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "swivelMultifactorTrustWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer(
            @Qualifier("swivelAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry swivelAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("loginFlowRegistry")
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new SwivelMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                swivelAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @Autowired
        public CasWebflowExecutionPlanConfigurer swivelAuthenticationCasWebflowExecutionPlanConfigurer(
            @Qualifier("swivelMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(swivelMultifactorTrustWebflowConfigurer);
        }
    }
}
