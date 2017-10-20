package com.imadcn.framework.lock.redis.pubsub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 发布订阅工具
 * @author imadcn
 */
public abstract class PublishSubscribe<E extends PubSubEntry<E>> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private final ConcurrentMap<String, E> entries =  new ConcurrentHashMap<String, E>(); // 锁资源监听
    private final ConcurrentMap<String, MessageListener> listenerMap = new ConcurrentHashMap<String, MessageListener>(); // 监听器map
    
    /**
     * 获取事件实例
     * @param entryName
     * @return PubSubEntry
     */
    public E getEntry(String entryName) {
        return entries.get(entryName);
    }
	
    /**
     * 订阅监听事件
     * @param entryName entryName
     * @param channelName channelName
     * @param container RedisMessageListenerContainer
     * @return PubSubEntry
     */
    public E subscribe(String entryName, String channelName, RedisMessageListenerContainer container) {
    	synchronized (this) {
            E entry = entries.get(entryName);
            if (entry != null) {
                entry.acquire();
                return entry;
            }

            E value = createEntry(channelName);
            value.acquire();

            E oldValue = entries.putIfAbsent(entryName, value);
            if (oldValue != null) {
                oldValue.acquire();
                return entry;
            }
            
    		MessageListener listener = creatMessageListener(channelName, value); // 创建监听器
    		if (listenerMap.putIfAbsent(entryName, listener) == null) {
    			log.debug("message listener added with entry name [{}], channel name [{}]", entryName, channelName);
    			container.addMessageListener(listener, new ChannelTopic(channelName));
    		}
    		return value;
        }
	}
	
	/**
	 * 取消订阅监听事件
	 * @param entry entry
	 * @param entryName entryName
	 * @param container RedisMessageListenerContainer
	 */
	public void unsubscribe(E entry, String entryName, RedisMessageListenerContainer container) {
		synchronized (this) {
            if (entry.release() == 0) {
                boolean removed = entries.remove(entryName) == entry;
                if (removed) {
                	MessageListener listener = listenerMap.remove(entryName);
            		if (listener != null) {
            			container.removeMessageListener(listener);
            		}
                }
            }
            log.debug("entry size [{}], listener size [{}]", entries.size(), listenerMap.size());
        }
	}
	
	/**
	 * 实例化监听触发接口
	 * @param channelName channelName
	 * @param value value
	 * @return MessageListener
	 */
	private MessageListener creatMessageListener(final String channelName, final E value) {
		final String tag = channelName + ":" + Thread.currentThread().getId();
		MessageListenerAdapter listener = new MessageListenerAdapter() {
			@Override
			public void onMessage(Message message, byte[] pattern) {
				log.debug("message received in channel-thread [{}]", tag);
				Object pubsubMessage = extractMessage(message); // 订阅消息内容
				PublishSubscribe.this.onMessage(value, pubsubMessage);
			}
		};
		return listener;
	}
	
	/**
	 * 创建资源监听订阅实例
	 * @param channelName channelName
	 * @return PubSubEntry
	 */
	protected abstract E createEntry(String channelName);
	
    /**
     * 订阅消息获取后，处理方法
     * @param value value
     * @param message message
     */
    protected abstract void onMessage(E value, Object message);
}
