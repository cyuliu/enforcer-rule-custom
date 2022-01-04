package com.cyuliu.maven.enforcer.rule;

import com.cyuliu.maven.enforcer.domain.CustomRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 自定义enforcer规则
 *
 * @author liuchongyu
 */
public class EnforcerRuleCustom implements EnforcerRule {

    private boolean shouldIfail = false;

    private Set<CustomRule> customRules;

    public void execute(@Nonnull EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();
        MavenProject project = null;
        try {
            project = (MavenProject) helper.evaluate("${project}");
        } catch (Exception e) {
            // 获取模块失败，记录日志，默认让规则走下去
            log.error("Failed to get project");
            return;
        }

        // 获取依赖列表
        List<Dependency> dependencies = project.getDependencies();

        // 有配置依赖，需要根据配置规则进行校验
        if (null != dependencies && dependencies.size() > 0) {
            // 没有配置规则
            if (null == customRules) {
                log.error("The module has no configuration rules, module:" + project.getArtifactId());
                if (shouldIfail) {
                    throw new EnforcerRuleException("The module has no configuration rules");
                }
                return;
            }

            // 父子module工程，可以在配置父工程配置依赖引入规则
            // 先查父module的规则
            CustomRule parent = null;
            // 如果是packaging是jar或者是war
            if ("jar".equals(project.getPackaging()) || "war".equals(project.getPackaging())) {
                parent = findRule(project.getParent().getArtifactId(), customRules);
            }
            // 然后查找子module的规则
            CustomRule child = findRule(project.getArtifactId(), customRules);

            // 找不到规则
            if (null == parent && null == child) {
                log.error("The module has no configuration rules, module:" + project.getArtifactId());
                if (shouldIfail) {
                    throw new EnforcerRuleException("The module has no configuration rules");
                }
                return;
            }

            Set<Dependency> ruleDependencies = null;

            // 获取父module的依赖规则配置
            if (null != parent && null != parent.getDependencies()) {
                ruleDependencies = parent.getDependencies();
            }
            // 获取子module的依赖规则配置
            if (null != child && null != child.getDependencies()) {
                if (null != ruleDependencies) {
                    for (Dependency dependency : child.getDependencies()) {
                        // 子module的依赖规则配置覆盖父module的依赖规则配置
                        boolean exists = false;
                        for (Dependency d : ruleDependencies) {
                            if (dependency.getGroupId().equals(d.getGroupId()) && dependency.getArtifactId().equals(d.getArtifactId())) {
                                exists = true;
                                d.setVersion(dependency.getVersion());
                                d.setClassifier(dependency.getClassifier());
                                break;
                            }
                        }
                        if (!exists) {
                            ruleDependencies.add(dependency);
                        }
                    }
                } else {
                    ruleDependencies = child.getDependencies();
                }
            }
            // 规则没有依赖配置
            if (null == ruleDependencies || ruleDependencies.size() <= 0) {
                log.error("The module has no dependency on configuration rules, module:" + project.getArtifactId());
                if (shouldIfail) {
                    throw new EnforcerRuleException("The module has no dependency on configuration rules");
                }
            }
            // 不存在的列表
            Set<Dependency> notDependencies = new HashSet<Dependency>();
            for (Dependency dependency : dependencies) {
                boolean valid = false;
                for (Dependency ruleDependency : ruleDependencies) {
                    // 校验groupId，artifactId，version，classifier
                    if (StringUtils.equals(dependency.getGroupId(), ruleDependency.getGroupId())
                            && StringUtils.equals(dependency.getArtifactId(), ruleDependency.getArtifactId())
                            && StringUtils.equals(dependency.getVersion(), ruleDependency.getVersion())
                            && StringUtils.equals(dependency.getClassifier(), ruleDependency.getClassifier())) {
                        valid = true;
                        break;
                    }
                }
                // 如果校验不过，加入不存在的依赖列表里
                if (!valid) {
                    notDependencies.add(dependency);
                }
            }
            if (0 < notDependencies.size()) {
                log.error("The module has dependencies without configuration rules, module:" + project.getArtifactId() + ", dependencies:" + notDependencies);
                if (shouldIfail) {
                    throw new EnforcerRuleException("The module has dependencies without configuration rules");
                }
            }
        }

    }

    /**
     * 查找规则
     *
     * @param module
     * @return
     */
    private CustomRule findRule(String module, Set<CustomRule> customRules) {
        // 查找规则
        for (CustomRule rule : customRules) {
            if (rule.getModule().equals(module)) {
                return rule;
            }
            if (null != rule.getCustomRules() && rule.getCustomRules().size() > 0) {
                return findRule(module, rule.getCustomRules());
            }
        }
        return null;
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(@Nonnull EnforcerRule enforcerRule) {
        return false;
    }

    @Nullable
    public String getCacheId() {
        return null;
    }

    public void setCustomRules(Set<CustomRule> customRules) {
        this.customRules = customRules;
    }

    public void setShouldIfail(boolean shouldIfail) {
        this.shouldIfail = shouldIfail;
    }
}
