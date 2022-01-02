package cn.migu.maven.enforcer.rule;

import cn.migu.maven.enforcer.domain.CustomRule;
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
        if (null != dependencies) {
            // 没有配置规则
            if (null == customRules) {
                throw new EnforcerRuleException("The system has no configuration rules");
            }
            // 找不到规则
            CustomRule rule = findRule(project);
            if (null == rule) {
                throw new EnforcerRuleException("The system has no configuration rules");
            }

            if (null == rule.getDependencies()) {
                throw new EnforcerRuleException("The system has no configuration rules");
            }
            // 不存在的列表
            Set<Dependency> notDependencies = new HashSet<Dependency>();
            for (Dependency dependency : dependencies) {
                boolean valid = false;
                for (Dependency ruleDependency : rule.getDependencies()) {
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
                log.error("The module has dependencies without configuration rules, dependencies:" + notDependencies);
                throw new EnforcerRuleException("The module has dependencies without configuration rules");
            }
        }

    }

    /**
     * 查找规则
     *
     * @param project
     * @return
     */
    private CustomRule findRule(MavenProject project) {
        CustomRule customRule = null;
        // 查找规则
        for (CustomRule rule : customRules) {
            if (rule.getModule().equals(project.getArtifactId())) {
                customRule = rule;
            } else if (null != rule.getRules()) {
                customRule = findRule(project);
            }
            // 找到规则
            if (null != customRule) {
                break;
            }
        }
        return customRule;
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
}
