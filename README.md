### 一个基于maven插件maven-enforcer-plugin严格控制pom引入的自定义规则。
### 具体使用方法：
### 1、用maven的install到本地仓库或deploy对应的仓库
### 2、工程使用，定义一个父pom，让使用的工程自动使用父pom
### 3、使用配置：
```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-enforcer-plugin</artifactId>
	<version>3.0.0</version>
	<dependencies>
		<dependency>
			<groupId>org.example</groupId>
			<artifactId>enforcer-rule-custom</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
	</dependencies>
	<executions>
		<execution>
			<id>enforce</id>
			<goals>
				<goal>enforce</goal>
			</goals>
			<configuration>
				<rules>
					<enforcerRuleCustom implementation="com.cyuliu.maven.enforcer.rule.EnforcerRuleCustom">
						<!--true打印错误信息，编译报错，false打印错误，可以编译通过，默认false-->
						<shouldIfail>true</shouldIfail>
						<customRules>
							<!--可以配置多个，一个项目对应一个-->
							<customRule>
								<module>${模块的artifactId}</module>
								<dependencies>
									<dependency>
										<groupId>${依赖的groupId}</groupId>
										<artifactId>${依赖的artifactId}</artifactId>
										<version>${依赖的version}</version>
										<!--项目中没有用到可不配-->
										<classifier>${依赖的classifier}</classifier>
									</dependency>
								</dependencies>
								<!--父子工程，可以继续配置规则-->
								<customRules>
								</<customRules>
							</customRule>
						</customRules>
					</enforcerRuleCustom>
				</rules>
			</configuration>
		</execution>
	</executions>
</plugin>
```
