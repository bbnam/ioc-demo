package com.nambb.iocdemo.loader;

import com.nambb.iocdemo.annotation.Autowired;
import com.nambb.iocdemo.annotation.Component;
import lombok.SneakyThrows;
import lombok.val;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextLoader {

    private static final ContextLoader INSTANCE = new ContextLoader();
    private final Map<String, Object> nameToInstance = new HashMap<>();

    private ContextLoader() {
    }

    public static ContextLoader getInstance() {
        return INSTANCE;
    }

    public synchronized void load(String scanPackage) {
        val reflections = new Reflections(scanPackage);
        val classes = reflections.getTypesAnnotatedWith(Component.class);
        initialInstance(classes);
        for (Class<?> clazz : classes) {
            val instance = nameToInstance.get(clazz.getName());
            injectInstance(instance);
        }
        executeRunner();
    }

    void executeRunner() {
        val runner = nameToInstance.values().stream().filter(Runner.class::isInstance).toList();
        if (runner.isEmpty()) {
            return;
        }
        if (runner.size() > 1) {
            throw new IllegalStateException("Multiple runner instances found: " + runner);
        }
        ((Runner) runner.getFirst()).run();
    }

    @SneakyThrows
    private void initialInstance(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            nameToInstance.put(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
        }
    }

    private void injectInstance(Object instance) {
        val fields = instance.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> Arrays.stream(field.getAnnotations()).anyMatch(annotation -> annotation.annotationType().equals(Autowired.class)))
                .forEach(field -> {
                    val constructor = nameToInstance.get(field.getType().getName());
                    field.setAccessible(true);
                    try {
                        field.set(instance, constructor);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
