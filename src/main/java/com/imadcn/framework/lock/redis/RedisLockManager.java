package com.imadcn.framework.lock.redis;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis lock 配置管理器
 * @author yc
 * @since 2017年1月16日
 */
public class RedisLockManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockManager.class);
	private final UUID uuid = UUID.randomUUID();
	
	private RedisTemplate<Object, Object> redisTemplate;
	private RedisMessageListenerContainer container;
	private volatile MessageListener daemonMessageListener;
	private String groupName; // 功能组名
	
	/**
	 * Redis lock 配置管理器
	 */
	public RedisLockManager() {}
	
	/**
	 * Redis lock 配置管理器
	 * @param redisTemplate
	 * @param container 
	 * @param groupName 功能组名(在不同程序中，KEY可能相同，需加入鉴别功能模块的组名)
	 */
	public RedisLockManager(RedisTemplate<Object, Object> redisTemplate, RedisMessageListenerContainer container, String groupName) {
		this.redisTemplate = redisTemplate;
		this.container = container;
		this.groupName = groupName;
	}

	/**
	 * 获取Redis锁
	 * @param key
	 * @return
	 */
	public RedisLock getLock(String key) {
		return new ReentrantRedisLock(redisTemplate, container, groupName, key, uuid);
	}
	
	public synchronized void init() {
		if (daemonMessageListener == null) {
			daemonMessageListener = new MessageListener() {
				@Override
				public void onMessage(Message message, byte[] pattern) {
					LOGGER.warn("daemon listener received some message, that's strange");
				}
			};
			container.addMessageListener(daemonMessageListener, new ChannelTopic("__redis_lock_channel__:daemon"));
		}
	}

	public void setRedisTemplate(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setContainer(RedisMessageListenerContainer container) {
		this.container = container;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}
