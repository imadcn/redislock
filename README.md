## redislock - 基于redis的分布式可重入锁

[![Build Status](https://travis-ci.org/imadcn/redislock.svg?branch=master)](https://travis-ci.org/imadcn/redislock)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imadcn.framework/lock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imadcn.framework/lock)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

### 是什么
redislock 是一个基于Redis的分布式可重入锁

### 怎么用
#### maven

```xml
<dependency>
  <groupId>com.imadcn.framework</groupId>
  <artifactId>lock</artifactId>
  <version>0.0.1</version>
</dependency>
```

#### XML配置 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context" xmlns:p="http://www.springframework.org/schema/p" xmlns:lock="http://code.imadcn.com/schema/lock"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context 
		http://www.springframework.org/schema/context/spring-context.xsd
		http://code.imadcn.com/schema/lock
		http://code.imadcn.com/schema/lock/lock.xsd">
		
	<!-- redis pool -->
	<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
		<property name="testOnBorrow" value="false" />
		<property name="testOnReturn" value="true" />
	</bean>

	<!-- redis sentinel -->
	<bean id="redisSentinelConfig" class="com.imadcn.framework.lock.config.RedisSentinelConfig">
		<property name="masterName" value="mymaster" />
		<property name="sentinelAddrs" value="127.0.0.1:26380,127.0.0.1:26381,127.0.0.1:26382" />
	</bean>

	<!-- redis connectionFactory -->
	<bean id="connectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory" destroy-method="destroy">
		<constructor-arg index="0" ref="redisSentinelConfig" />
		<constructor-arg index="1" ref="jedisPoolConfig" />
	</bean>

	<bean id="stringRedisSerializer" class="org.springframework.data.redis.serializer.StringRedisSerializer" />
	<bean id="jdkSerializationRedisSerializer" class="org.springframework.data.redis.serializer.JdkSerializationRedisSerializer" />

	<!-- redisTemplate -->
	<bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="keySerializer" ref="stringRedisSerializer" />
		<property name="valueSerializer" ref="stringRedisSerializer" />
		<property name="hashKeySerializer" ref="stringRedisSerializer" />
		<property name="hashValueSerializer" ref="stringRedisSerializer" />
		<property name="stringSerializer" ref="stringRedisSerializer" />
	</bean>
	
	<!-- redisMessageListenerContainer -->
	<bean id="redisMessageListenerContainer" class="org.springframework.data.redis.listener.RedisMessageListenerContainer">
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="topicSerializer" ref="stringRedisSerializer" />
	</bean>
	
	<lock:config id="lockManager" group="physical-exam" redisTemplate="redisTemplate" messageContainer="redisMessageListenerContainer"/>
</beans>

```

#### API

```java
@Autowired
private RedisLockManager manager;

public void lock() {
	RedisLock redisLock = manager.getLock("asd");
	redisLock.lock();
	redisLock.unlock();
} 

```

### 配置参考
#### <redislock:config /> redislock 配置

|属性|类型|必填|缺省值|描述|
|:------|:------|:------|:------|:------|
|id|String|是| |Spring容器中的ID|
|group|String|是| |分组名，可以为不同业务分配分组|
|redisTemplate|String|是| |redisTemplate|
|messageContainer|String|是| |RedisMessageListenerContainer|
