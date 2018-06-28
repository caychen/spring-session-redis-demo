1、新建一个基于Maven的web工程

2、添加相关依赖
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>4.3.14.RELEASE</version>
</dependency>
<!-- https://mvnrepository.com/artifact/org.springframework.session/spring-session-data-redis -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
    <version>1.3.1.RELEASE</version>
</dependency>
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.0</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.6.2</version>
</dependency>

<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
<!-- https://mvnrepository.com/artifact/org.projectlombok/lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.0</version>
    <scope>provided</scope>
</dependency>
```

3、编写redis.properties文件
```properties
redis.host=172.20.16.133
redis.port=6379
redis.maxTotal=100
redis.timeout=20000
redis.maxIdle=30
```

4、编写spring的配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <context:property-placeholder location="classpath*:*.properties"/>

    <context:component-scan base-package="org.com.cay"/>

    <bean id="redisHttpSessionConfiguration"
          class="org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration">
        <property name="maxInactiveIntervalInSeconds" value="600"/>
    </bean>

    <import resource="classpath*:spring-redis.xml"/>

</beans>
```

5、编写spring与redis集成的配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="poolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxIdle" value="${redis.maxIdle}"/>
        <property name="maxTotal" value="${redis.maxTotal}"/>
    </bean>

    <bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
        <property name="poolConfig" ref="poolConfig"/>
        <property name="hostName" value="${redis.host}"/>
        <property name="port" value="${redis.port}"/>
        <property name="usePool" value="true"/>
        <property name="timeout" value="${redis.timeout}"/>
    </bean>
</beans>
```

6、最后编写springmvc的配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.com.cay.controller"/>
    <mvc:annotation-driven/>
    <mvc:default-servlet-handler/>
</beans>
```

7、编写User实体类
```java
package org.com.cay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Cay on 2018/6/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable{

	private String name;
}

```

8、编写控制器类，为了能够看到session共享，分别将项目进行打包，其中控制器的get方法，一个返回8080（部署在8080端口的tomcat上），一个返回8081（部署在8081端口的tomcat上）
```java
package org.com.cay.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.com.cay.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Cay on 2018/6/14.
 */
@ResponseBody
@Controller
public class RedisSessionController {

	private final Gson gson = new GsonBuilder().setDateFormat("yyyyMMddHHmmss").create();

	@RequestMapping("/set")
	public String set(HttpSession session, String name) {
		String s = gson.toJson(new User(name));
		session.setAttribute("user", s);
		return s;
	}

	@RequestMapping("/get")
	public String get(HttpSession session){
		User user = gson.fromJson(session.getAttribute("user").toString(), User.class);

		//返回8080或者8081，打包前请修改
		return user.toString() + ",8080";
	}
}
```

9、进行maven打包

10、部署到两台tomcat容器上

11、修改nginx配置
	在nginx.conf配置最后的地方}之前加上
```
include vhost/*.conf;
```

12、在nginx目录下新建vhost文件夹，并新增一个session.conf文件
```
vim vhost/session.conf
```
添加如下内容
```
upstream abc {
	server  localhost:8081;
    server  localhost:8080;
}

server {
	listen 80;
    server_name 172.20.16.133;
  
	location /{
		proxy_pass http://abc;
	}
}
```
13、然后启动两台tomcat和nginx即可。

14、访问http://172.20.16.133/set?name=xxxx

15、访问http://172.20.16.133/get