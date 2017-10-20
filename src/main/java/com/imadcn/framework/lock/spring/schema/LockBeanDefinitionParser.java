package com.imadcn.framework.lock.spring.schema;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.imadcn.framework.lock.redis.RedisLockManager;

public class LockBeanDefinitionParser extends BaseBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RedisLockManager.class);
		String group = element.getAttribute("group");
		String redisTemplate = element.getAttribute("redisTemplate");
		String messageContainer = element.getAttribute("messageContainer");
		if (!StringUtils.hasText(group)) {
			throw new IllegalArgumentException("redis lock group name should not be null");
		}
		builder.addPropertyValue("groupName", group);
		if (StringUtils.hasText(redisTemplate)) {
			builder.addPropertyReference("redisTemplate", redisTemplate);
		}
		if (StringUtils.hasText(messageContainer)) {
			builder.addPropertyReference("container", messageContainer);
		}
		builder.setInitMethodName("init");
		return builder.getBeanDefinition();
	}
}
