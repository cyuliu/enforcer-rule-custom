package com.cyuliu.maven.enforcer.domain;

import org.apache.maven.model.Dependency;

import java.io.Serializable;
import java.util.Set;

public class CustomRule implements Serializable {

    private String module;

    private Set<Dependency> dependencies;

    private Set<CustomRule> customRules;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<CustomRule> getCustomRules() {
        return customRules;
    }

    public void setCustomRules(Set<CustomRule> customRules) {
        this.customRules = customRules;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("EnforcerRuleCustomModule[");
        builder.append("module='").append(module);
        builder.append(", dependencies=").append(dependencies);
        builder.append(", customRules=").append(customRules);
        builder.append("]");
        return builder.toString();
    }
}
