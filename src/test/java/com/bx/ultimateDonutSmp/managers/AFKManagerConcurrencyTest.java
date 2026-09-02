package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AFKManager.recordMovement is called from PlayerActivityListener.onChat, which handles
 * AsyncPlayerChatEvent off the main thread, and from PlayerMoveListener.onMove on the main thread.
 * The map behind it is a plain HashMap.
 */
class AFKManagerConcurrencyTest {

    private static final int THREADS = 8;
    private static final int PER_THREAD = 4000;

    @Test
    @SuppressWarnings("unchecked")
    void recordMovementFromSeveralThreadsKeepsEveryEntry() throws Exception {
        AFKManager manager = new AFKManager(newPlugin());

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        for (int thread = 0; thread < THREADS; thread++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int index = 0; index < PER_THREAD; index++) {
                        manager.recordMovement(UUID.randomUUID());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await(30, TimeUnit.SECONDS);

        Field field = AFKManager.class.getDeclaredField("lastMovement");
        field.setAccessible(true);
        Map<UUID, Long> lastMovement = (Map<UUID, Long>) field.get(manager);

        assertEquals(THREADS * PER_THREAD, lastMovement.size(),
                "every recorded movement has to survive; a plain HashMap drops entries under concurrent puts");
    }

    private static UltimateDonutSmp newPlugin() throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> pluginConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        return (UltimateDonutSmp) pluginConstructor.newInstance();
    }
}
