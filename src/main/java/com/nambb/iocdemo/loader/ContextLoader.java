package com.nambb.iocdemo.loader;

import com.nambb.iocdemo.annotation.Autowired;
import com.nambb.iocdemo.annotation.Component;
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
        var reflections = new Reflections(scanPackage);
        var classes = reflections.getTypesAnnotatedWith(Component.class);

        initialInstance(classes);
        for (var entry : nameToInstance.entrySet()) {
            var instance = entry.getValue();
            injectInstance(instance);
        }
        executeRunner();
    }

    void executeRunner() {
        var runner = nameToInstance.values().stream().filter(Runner.class::isInstance).toList();

        if (runner.isEmpty()) {
            return;
        }

        if (runner.size() > 1) {
            throw new RuntimeException("Multiple runner instances found");
        }

        ((Runner) runner.get(0)).run();
    }

    private void initialInstance(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                val c = Class.forName(clazz.getName());
                val instance = c.getDeclaredConstructor().newInstance();
                nameToInstance.put(clazz.getName(), instance);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void injectInstance(Object instance) {
        val fields = instance.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> Arrays.stream(field.getDeclaredAnnotations())
                        .anyMatch(a -> a.annotationType() == Autowired.class))
                .forEach(field -> {
                    val value = nameToInstance.get(field.getType().getName());
                    field.setAccessible(true);
                    try {
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                });
    }

}
