package org.tvenceslau.java.Cloneable;

import org.tvenceslau.java.Cloneable.Cloneable.UpdateSpecification;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class Cloneables {

    private static final Pattern CLASS_COMPARE_PAT = Pattern.compile("^(.*?)(?:\\$\\d+)?$");
    private static final Map<Class<?>, UpdateSpecification<?>> specMapCache = new HashMap<>();

    /**
     * Recursively perform a deep'ish copy of the provided {@param origin} into the
     * provided {@param destination}
     *
     * <ul>
     * <li>Every primitive property of the {@param origin} will be copied </li>
     * <li>Every Object property that implements {@link Cloneable} will be cloned as well</li>
     * <li><strong>Non</strong> {@link Cloneable} Objects will be the shared according to the current implementation</li>
     * <li>Final properties will be ignored</li>
     * </ul>
     *
     * @param clazz       Should be the origin's/destination's class when initially called.
     * @param origin      Object to be copied of class {@param T}
     * @param destination Resulting object of class {@param T}
     * @param <T>         Mutual Instance Class
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> void deepClone(Class<?> clazz, final T origin, final T destination) {
        final Field[] fields = clazz.getDeclaredFields();

        Object obj;
        Class<?> classType;

        for (Field field : fields) {
            if (!field.isAccessible())
                field.setAccessible(true);

            try {
                if (field.isAnnotationPresent(Cloneable.NotToClone.class)) {
                    field.set(destination, field.get(origin));
                    continue;
                }

                classType = field.getType();

                if (((obj = field.get(origin)) != null) && Collection.class.isAssignableFrom(classType)) {
                    field.set(destination, collectToList(((Collection) obj).stream()
                            .map(Cloneables::handleSingleField), obj.getClass()));
                } else if (obj != null && field.isAnnotationPresent(Cloneable.ToClone.class)) {
                    // for fields that must be cloned, but do not implement 'Cloneable'
                    field.set(destination, customCloneField(obj, field.getAnnotation(Cloneable.ToClone.class)));
                } else {
                    field.set(destination, handleSingleField(obj));
                }

            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(clazz.getSimpleName() + " copying went wrong. " + e.getMessage());
            }
        }

        if (clazz.getSuperclass() != null)
            deepClone(clazz.getSuperclass(), origin, destination);
    }

    @SuppressWarnings("rawtypes")
    private static Object handleSingleField(Object obj) {
        if (obj != null && Cloneable.class.isAssignableFrom(obj.getClass()))
            return ((Cloneable) obj).cloneSelf();
        return obj;
    }

    /**
     * This method calls an {@link Cloneable.ToClone} annotated method in the provided object
     * Part of the {@link Cloneable} functionality.
     * The annotated method will have to return an Object of the same class as the provided {@param obj} and take no parameters
     *
     * @param obj An Object to be cloned via its {@link Cloneable.ToClone} annotated method
     * @return the clone of the object
     */
    private static Object customCloneField(Object obj, Cloneable.ToClone annotation) {
        final List<Method> methods = new ArrayList<>();
        final Class<?> clazz = obj.getClass();
        final String methodName = annotation.method();
        for (final Method method : clazz.getDeclaredMethods())
            if (method.getName().equals(methodName))
                methods.add(method);

        if (methods.isEmpty())
            throw new IllegalStateException("Couldn't find a match to the provided method name.");

        if (methods.size() > 1)
            throw new IllegalStateException("The method name must be unique.");

        final Method method = methods.get(0);
        if (method.getParameterCount() != 0 || !compareClass(clazz, method.getReturnType()))
            throw new IllegalStateException("'ToClone' methods can take no parameters and must return an object of the same class as the original object");

        if (!method.isAccessible())
            method.setAccessible(true);

        try {
            return method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Couldn't invoke cloning method. Do you have access?");
        }
    }

    /**
     * Necessary because the getClass method returns an instance I guess and we can't compare it with the returnType
     * even if they're the same, they'll always test false.
     * Could also use a split or something, but since the idea is to do this for big tree like structures recursively
     * I chose the cache a single regexp
     *
     * Still wish I'd found a way around this...
     *
     * @param clazz1 class
     * @param clazz2 class
     * @return true if they're the same class
     */
    private static boolean compareClass(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1.equals(clazz2)) return true;

        final Matcher m1 = CLASS_COMPARE_PAT.matcher(clazz1.getName());
        final Matcher m2 = CLASS_COMPARE_PAT.matcher(clazz2.getName());
        if (!m1.matches() || !m2.matches()) return false;

        return m1.group(1).equals(m2.group(1));
    }

    /**
     * Updates @ToUpdate annotated {@link Field}s according to the provided {@link UpdateSpecification}
     *
     * @param clazz  The entry class in the method. Should be originally called with {@param object}'s class
     * @param object The {@link Cloneable} component that contains the Fields to have their name changed
     * @param index  The clone index (Optional if the {@link UpdateSpecification} doesn't use it
     * @param <T>    ...
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void update(Class<?> clazz, Cloneable<T> object, int index) {
        final Field[] fields = clazz.getDeclaredFields();

        Class<?> classType;
        Object obj;
        for (Field field : fields) {
            if (!field.isAccessible())
                field.setAccessible(true);

            if (!field.isAnnotationPresent(Cloneable.NotToUpdate.class)) {
                classType = field.getType();
                try {
                    if (((obj = field.get(object)) != null) && Collection.class.isAssignableFrom(classType))
                        field.set(object, collectToList(((Collection) obj).stream()
                                .map(f -> updateSingleField(f, null, index)), obj.getClass()));
                    else
                        field.set(object, updateSingleField(object, field, index));

                } catch (IllegalAccessException e) {
                    throw new UnsupportedOperationException("Could not access inner properties. " + e.getMessage());
                }
            }
        }

        if (clazz.getSuperclass() != null)
            update(clazz.getSuperclass(), object, index);
    }

    /**
     * Updates a Single field's value,
     * or,
     * if the {@param field} is null (handling list items via stream)
     * if {@param obj} is {@link Cloneable}, call {@param obj}'s {@link Cloneable#updateSelf(int)}
     * else return the {@param obj}
     *
     * @param obj   Object that should have it's {@param field} updated
     * @param field {@link Field} to be updated.
     *              <strong>MUST</strong> be null when acting on Collections via Streams.
     * @throws IllegalArgumentException when field to update ios not a String
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object updateSingleField(Object obj, Field field, int index) {
        if (field == null) {  // this handles items inside Collections
            if (Cloneable.class.isAssignableFrom(obj.getClass()))
                ((Cloneable) obj).updateSelf(index);
            return obj;
        }

        // this handles other items
        Object originalProp;
        try {
            originalProp = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Could not access inner properties. " + e.getMessage());
        }

        if (originalProp != null && Cloneable.class.isAssignableFrom(originalProp.getClass())) {
            if (!field.isAnnotationPresent(Cloneable.NotToUpdate.class))
                ((Cloneable) originalProp).updateSelf(index);
            return originalProp;
        }

        if (!field.isAnnotationPresent(Cloneable.ToUpdate.class))
            return originalProp;

        final Cloneable.ToUpdate annotation = field.getAnnotation(Cloneable.ToUpdate.class);

        final Cloneable.UpdateSpecification spec = getSpec(annotation.spec());

        return updateProperty(originalProp, spec, index);
    }

    /**
     * Runs the provided {@link UpdateSpecification} and returns it
     * @return the updated value
     */
    private static <T> T updateProperty(final T property, final UpdateSpecification<T> spec, int index) {
        return spec.update(property, index);
    }

    /**
     * Caches provided {@link UpdateSpecification} so you don't instantiate one for every cloning op
     * @param clazz class of {@link UpdateSpecification}
     * @param <T>   ...
     * @return the cached Spec or a new one if none is cached while caching it
     */
    @SuppressWarnings("unchecked")
    private static <T> UpdateSpecification<?> getSpec(Class<T> clazz) {
        if (!specMapCache.containsKey(clazz)) {
            UpdateSpecification<T> spec;
            try {
                spec = (UpdateSpecification<T>) clazz.newInstance();
                specMapCache.put(clazz, spec);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Could not get Specification");
            }
        }

        return specMapCache.get(clazz);
    }

    /**
     * Method to collect all stream elements and collect them to the proper Implementation of {@link List}
     *
     * @param stream   {@param S} object stream
     * @param listType An implementation of {@link List}'s class
     * @param <S>      any type
     * @return The result of collecting all stream elements into a List<{@param S}> of the provided {@param listType}
     */
    private static <S> List<S> collectToList(Stream<S> stream, Class<?> listType) {
        if (ArrayList.class.isAssignableFrom(listType))
            return stream.collect(toCollection(ArrayList::new));
        if (LinkedList.class.isAssignableFrom(listType))
            return stream.collect(toCollection(LinkedList::new));

        throw new UnsupportedOperationException("Provided ListType " + listType + " not currently supported");
    }
}
