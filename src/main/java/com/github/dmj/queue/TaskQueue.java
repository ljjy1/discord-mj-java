package com.github.dmj.queue;

import com.github.dmj.autoconfigure.DiscordAccountProperties;
import com.github.dmj.autoconfigure.DiscordProperties;
import com.github.dmj.autoconfigure.DiscordPropertiesAutoConfig;
import com.github.dmj.error.DiscordMjJavaException;
import com.github.dmj.model.DiscordTask;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * @author ljjy1
 * @classname TaskQueue
 * @description 任务队列
 * @date 2023/10/11 13:23
 */
public class TaskQueue {

    /**
     * key存储账户key   任务队列
     */
    private static final Map<String,LinkedBlockingQueue<DiscordTask>> taskQueueMap = new ConcurrentHashMap<>();
    /**
     * key存储账户key  等待队列
     */
    private static final Map<String,LinkedBlockingQueue<DiscordTask>> waitQueueMap = new ConcurrentHashMap<>();


    private static ExecutorService runExecutorService;
    private static ExecutorService waitToRunExecutorService;

    private TaskQueue() {
    }

    private static class TaskQueueHolder{
        private static final TaskQueue INSTANCE = new TaskQueue();

        static {
            DiscordProperties discordProperties = DiscordPropertiesAutoConfig.discordProperties;
            Iterator<Map.Entry<String, DiscordAccountProperties>> iterator = discordProperties.getAccount().entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, DiscordAccountProperties> next = iterator.next();
                String key = next.getKey();
                DiscordAccountProperties discordAccountProperties = next.getValue();
                int waitSize = discordAccountProperties.getWaitSize();
                int concurSize = discordAccountProperties.getConcurSize();
                LinkedBlockingQueue<DiscordTask> taskQueue = new LinkedBlockingQueue<>(concurSize);
                LinkedBlockingQueue<DiscordTask> waitQueue = new LinkedBlockingQueue<>(waitSize);
                taskQueueMap.put(key,taskQueue);
                waitQueueMap.put(key,waitQueue);

            }
            //创建一个跟随配置账户数量的线程池
            runExecutorService = Executors.newFixedThreadPool(taskQueueMap.size());

            waitToRunExecutorService = Executors.newFixedThreadPool(taskQueueMap.size());

            //执行任务启动
            INSTANCE.runTask();

            //同步等待任务到运行任务
            INSTANCE.waitToRunTask();
        }
    }

    public static TaskQueue getInstance(){
        return TaskQueue.TaskQueueHolder.INSTANCE;
    }


    /**
     * 给某个账户增加任务
     * @param userKey
     * @param function
     * @param param
     */
    public <T,S> void putTask(String userKey,Function<T,S> function, T param){
        LinkedBlockingQueue<DiscordTask> waitTasks = waitQueueMap.get(userKey);
        //判断队列是否已经满了 offer返回false表示队列已经达到初始设置的大小  不会进行扩容
        if(!waitTasks.offer(new DiscordTask(function, param))){
            throw new DiscordMjJavaException("The current account userKey:{} waiting queue is full, please try again later",userKey);
        }
    }


    /**
     * 运行任务
     */
    public void runTask(){
        for (LinkedBlockingQueue<DiscordTask> taskQueue : taskQueueMap.values()) {
            runExecutorService.submit(() -> {
                while (true){
                    //阻塞获取队列元素 执行任务
                    DiscordTask take = taskQueue.take();
                    take.run();
                }
            });
        }
    }

    /**
     * 将等待队列任务推入运行队列
     */
    public void waitToRunTask(){
        Iterator<Map.Entry<String, LinkedBlockingQueue<DiscordTask>>> iterator = waitQueueMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, LinkedBlockingQueue<DiscordTask>> next = iterator.next();
            String key = next.getKey();
            LinkedBlockingQueue<DiscordTask> waitQueue = next.getValue();
            LinkedBlockingQueue<DiscordTask> taskQueue = taskQueueMap.get(key);

            waitToRunExecutorService.submit(() -> {
                while (true){
                    //判断任务队列是否满了
                    if(taskQueue.remainingCapacity() != 0){
                        DiscordTask take = waitQueue.take();
                        taskQueue.offer(take);
                    }else{
                        //休眠1000毫秒  避免任务队列taskQueue一直满 一直循环
                        Thread.sleep(1000);
                    }
                }
            });
        }


    }










}
