package com.example.zephyrevents.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.zephyrevents.model.SystemLog;
import com.example.zephyrevents.repository.SystemLogRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class SystemLogControllerTest {

    private SystemLogRepository mockRepo;
    private SystemLogController controller;

    @Before
    public void setUp() throws Exception {
        clearSingleton();
        mockRepo = Mockito.mock(SystemLogRepository.class);
        controller = new SystemLogController(mockRepo);
        Field inst = SystemLogController.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, controller);
    }

    @After
    public void tearDown() throws Exception {
        clearSingleton();
    }

    private static void clearSingleton() throws Exception {
        Field inst = SystemLogController.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    // Tests that it reference the same SystemLogController instance

    @Test
    public void getInstance_returnSameReference() throws Exception {
        clearSingleton();
        SystemLogRepository mock = Mockito.mock(SystemLogRepository.class);
        SystemLogController seeded = new SystemLogController(mock);
        Field inst = SystemLogController.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, seeded);

        assertSame(seeded, SystemLogController.getInstance());
        assertSame(SystemLogController.getInstance(), SystemLogController.getInstance());
    }

    // Tests that logAction() builds the right SystemLog

    @Test
    public void logAction_delegatesToRepositoryWithPopulatedLog() {
        controller.logAction("EVENT_CREATED", "Event 'X' was created", "Organizer");

        ArgumentCaptor<SystemLog> captor = ArgumentCaptor.forClass(SystemLog.class);
        verify(mockRepo).addLog(captor.capture());

        SystemLog log = captor.getValue();
        assertEquals("EVENT_CREATED", log.getActionType());
        assertEquals("Event 'X' was created", log.getDescription());
        assertEquals("Organizer", log.getActorName());
    }

    // Tests that addLog is call once


    @Test
    public void logAction_callsAddLogOnce() {
        controller.logAction("A", "B", "C");
        verify(mockRepo, Mockito.times(1)).addLog(any(SystemLog.class));
    }
}
