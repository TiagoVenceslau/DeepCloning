package org.tvenceslau.java.Cloneable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class Cloneables {

    private static final Pattern CLASS_COMPARE_PAT = Pattern.compile("^(.*?)(?:\\$\\d+)?$");

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

        for (java.lang.reflect.Field field : fields)
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isPrivate(field.getModifiers())) //TODO: handle finals/privates? do I need to?
                try {

                    // If field is tagged as @NotToClone, then set it to the same obj
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
     * @param obj An Object to be cloned via its {@link Cloneable.ToClone} annotated method
     * @return the clone of the object
     */
    private static Object customCloneField(Object obj, Cloneable.ToClone annotation){
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
     * even if they're the same, they'll always test false
     * @param clazz1 class
     * @param clazz2 class
     * @return true if they're the same class
     */
    private static boolean compareClass(Class<?> clazz1, Class<?> clazz2){
        if (clazz1.equals(clazz2)) return true;

        final Matcher m1 = CLASS_COMPARE_PAT.matcher(clazz1.getName());
        final Matcher m2 = CLASS_COMPARE_PAT.matcher(clazz2.getName());
        if (m1.matches() != m2.matches()) return false;

        return m1.group(1).equals(m2.group(1));
    }

    /**
     * Updates {@link Field#name} according to it's provided index
     * To the format: name -> name{@param separator}{@param index}
     * <p>
     * Does the same for {@link Field#forFieldName} if it's not null
     *
     * @param clazz     The entry class in the method. Should be originally called with {@param object}'s class
     * @param object    The {@link Cloneable} component that contains the Fields to have their name changed
     * @param separator A string to concatenate the index
     * @param index     ...
     * @param initialization    true when initializing components (the index will be applied between the name and the already existing ones)
     *                          false when updating components (the index will be concatenated will the previous ones
     *                          null when resetting (will return the property to its 'un-indexed' value)
     *
     * @param <T>       Any subclass of {@link SedrComponent} or a {@link SedrComponent} itself
     * @throws IllegalArgumentException for a non-String {@link Cloneable.ToUpdate} annotated field
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void updateFieldNames(Class<?> clazz, Cloneable<T> object, final String separator, final String index, final Boolean initialization) throws IllegalArgumentException {
        final Field[] fields = clazz.getDeclaredFields();

        Class<?> classType;
        Object obj;
        for (java.lang.reflect.Field field : fields)
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isPrivate(field.getModifiers())
                    && !field.isAnnotationPresent(Cloneable.NotToUpdate.class)) { // TODO: same as above
                classType = field.getType();
                try {
                    if (((obj = field.get(object)) != null) && Collection.class.isAssignableFrom(classType))
                        field.set(object, collectToList(((Collection) obj).stream()
                                .map(f -> updateSingleField(f, null, separator, index, initialization)), obj.getClass()));
                    else
                        field.set(object, updateSingleField(object, field, separator, index, initialization));

                } catch (IllegalAccessException e) {
                    throw new UnsupportedOperationException("Could not access inner properties. " + e.getMessage());
                }
            }

        if (clazz.getSuperclass() != null)
            updateFieldNames(clazz.getSuperclass(), object, separator, index, initialization);
    }

    /**
     * Updates a Single field's value,
     * or,
     * if the {@param field} is null (handling list items via stream)
     * if {@param obj} is {@link Cloneable}, call {@param obj}'s {@link Cloneable#updateSelfProperties(String, String, Boolean)}
     * else return the {@param obj}
     *
     * @param obj               Object that should have it's {@param field} updated
     * @param field             {@link java.lang.reflect.Field} to be updated.
     *                          <strong>MUST</strong> be null when acting on list items via Stream.
     * @param separator         A string to concatenate the index
     * @param index             ...
     * @param initialization    true when initializing components (the index will be applied between the name and the already existing ones)
     *                          false when updating components (the index will be concatenated will the previous ones
     *                          null when resetting (will return the property to its 'un-indexed' value)
     * @throws IllegalArgumentException when field to update ios not a String
     */
    @SuppressWarnings("rawtypes")
    private static Object updateSingleField(Object obj, Field field, final String separator, final String index, final Boolean initialization) throws IllegalArgumentException {
        if (field == null) {  // this handles items inside Collections
            if (Cloneable.class.isAssignableFrom(obj.getClass()))
                ((Cloneable) obj).updateSelfProperties(separator, index, initialization);
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
                ((Cloneable) originalProp).updateSelfProperties(separator, index, initialization);
            return originalProp;
        }

        if (!field.isAnnotationPresent(Cloneable.ToUpdate.class))
            return originalProp;

        if (!String.class.isAssignableFrom(field.getType()))
            throw new IllegalArgumentException("Non-String objects can't be updated!\n" +
                    "Source Object: " + obj + "\nfield: " + field);

        final String originalValue = (String) originalProp;

        if (originalValue == null)
            return null;

        return updateIndex(originalValue,separator,index, initialization);
    }

    /**
     * Updates the provided {@param property} according to:<br>
     *     Example: for field named "S_II_1_2", separator "_INDEX_" and index "1", "2" and "3" respectively
     *     <ul>
     *         <li>"S_II_1_2" -> "S_II_1_2_INDEX_1"</li>
     *         <li>"S_II_1_2_INDEX_1" -> "S_II_1_2_INDEX_2_INDEX_1"</li>
     *         <li>"S_II_1_2_INDEX_2_INDEX_1" -> "S_II_1_2_INDEX_3_INDEX_2_INDEX_1"</li>
     *     </ul>
     *
     * @param property          the String to be updated
     * @param separator         the separator String to be used
     *                          can be null when resetting values
     * @param index             the index to be applied
     *                          can be null when resetting values
     * @param initialization    true when initializing a component
     *                          false when updating values
     *                          <strong>must be null when resetting values</strong>
     *
     * @return  the updated {@param property}
     *
     * @throws IllegalAccessError if the provided {@param property} doesn't contain the provided {@param separator}
     *      and doesn't match the {@link SedrSectionComponent#SEPARATOR_PAT}
     */
    private static String updateIndex(final String property, final String separator, final String index, final Boolean initialization) throws IllegalAccessError{
        if (!property.contains(separator) && !property.contains(";")) {
            if (initialization == null)
                throw new IllegalArgumentException("Calling a reset on non previously updated properties");
            return property + separator + index;
        }

        if (property.contains(";")){
            // for meta_parents and meta_child on FieldOptions
            String[] values = property.split(";");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < values.length; i++) {
                sb.append(updateIndex(values[i],separator,index,initialization));
                if (i < values.length-1)
                    sb.append(";");
            }
            return sb.toString();
        }

        final Matcher m = SedrSectionComponent.SEPARATOR_PAT.matcher(property);

        if (m.find())
            if (initialization == null) {
                final Matcher m2 = SedrSectionComponent.ALL_BUT_LAST_SEPARATOR_PAT.matcher(property);
                if (m2.find())
                    return m2.group(1) + separator + index;
            } else {
                if (initialization) {
                    return m.group(1) + separator + index + m.group(2);
                } else {
                    return property + separator + index;
                }
            }

        throw new IllegalAccessError("Could not find groups");
    }

    /**
     * Method to collect all stream elements and collect them to the proper Implementation of {@link List}
     * @param stream {@param S} object stream
     * @param listType An implementation of {@link List}'s class
     * @param <S> any type
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
