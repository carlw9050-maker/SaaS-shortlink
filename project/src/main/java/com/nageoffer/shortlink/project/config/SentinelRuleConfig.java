package com.nageoffer.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 *
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        //覆写了接口 InitializingBean 的 afterPropertiesSet()，该方法会在 Spring 初始化这个 bean 的所有属性后自动调用
        //硬编码配置方式
        List<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        //FlowRule 是 Sentinel 的流量控制规则类。
        createOrderRule.setResource("create_shortlink");
        //设置规则对应的资源名称，即该规则会用到名称为它的Sentinel资源上面
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //设置流量控制的类型，基于QPS（每秒查询率）
        createOrderRule.setCount(1);
        //每秒最多允许一次请求通过
        rules.add(createOrderRule);
        FlowRuleManager.loadRules(rules);
        //使用Sentinel的FlowRuleManager加载这些规则，使它们生效。
    }
}
