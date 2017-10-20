package com.imadcn.test.redislock;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.imadcn.framework.lock.redis.RedisLock;
import com.imadcn.framework.lock.redis.RedisLockManager;

public class SpringTest {
	
	private static ClassPathXmlApplicationContext context;
	private static String configPath = "classpath:spring-config.xml";
	
	public SpringTest() {
		context = new ClassPathXmlApplicationContext(new String[] {configPath});
		context.start();
	}
	
	public void testGen() {
		RedisLockManager manager = context.getBean(RedisLockManager.class);
		RedisLock redisLock = manager.getLock("asd");
		redisLock.lock();
		redisLock.unlock();
	} 
	
	void print(Object object) {
	}
	
	public static void main(String[] args) {
		SpringTest test = new SpringTest();
		test.testGen();
	}

}
