package org.tvenceslau.java.Cloneable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><h1>Deep Cloning Implementation</h1></p>
 * <p><h2>with user defined property update logic</h2></p>
 * <p>org.tvenceslau.java.Cloneable</p>
 * <br>
 * <h2>Objective:</h2>
 * <p>Provides generic access and default implementation for deep cloning any object that implements {@link Cloneable}<br>
 *     while providing control over which properties will also be cloned and/or updated somehow</p>
 * <p>Doing so in a manner that can easily be inserted in any project with minimum impact</p>
 * <ul>
 *     <lh><h3>Applies to:</h3></lh>
 *     <li>Any objects that implement {@link Cloneable}</li>
 * </ul>
 * <p><h3>How to use:</h3></p>
 * <p><ul>
 *     <li>Classes that implement this must parameterize it to it's own class. Eg: 'class XXX implements Cloneable<XXX>'.</li>
 *     <li>If the class being cloned needs to have parameters updated
 *          then one must annotate those properties with {@link Cloneable.ToUpdate} providing a {@link UpdateSpecification}</li>
 * </ul></p>
 *
 * <p><h4>Comments:</h4></p>
 * <p>Had to do quite a bit of custom cloning logic for work, after a lot of classes were already in production, so I
 * wanted to be able to custom deep clone objects and selectively update some of their properties as I cloned them.</p>
 * <p>This was the result</p>
 *
 * @see Cloneable.ToClone
 * @see Cloneable.NotToClone
 * @see Cloneable.ToUpdate
 * @see Cloneable.NotToUpdate
 *
 * @param <S> Should be the the Class that is implementing it
 *
 * @author Tiago Venceslau
 *
 * Date of creation:    29/05/2019
 * Created in:          IntelliJ IDEA
 */
public interface Cloneable<S> {

    /**
     * Provides a default implementation to all {@link Cloneable} objects
     * Objects that need to have non {@link Cloneable} fields, that require cloning,
     *
     * @param <T> Any subclass of {@param S} or a {@param S} itself
     * @return Returns a deep'ish clone according to {@link Cloneables#deepClone(Class, Object, Object)}
     */
    @SuppressWarnings(value="unchecked")
    default <T extends S> T cloneSelf(){
        final Class<?> clazz = this.getClass();

        T newObj;
        try{
            newObj = (T) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new UnsupportedOperationException("Could not create a copy of " + this);
        }

        Cloneables.deepClone(clazz, this, newObj);
        return newObj;
    }

    /**
     * Method to be used to update a {@link Cloneable}'s properties annotated with {@link ToUpdate}
     * It was designed for use in bulk cloning operations so
     * @param index     is the index on thos bulk operations
     */
    default void updateSelf(int index) {
        Cloneables.update(this.getClass(), this, index);
    }

    /**
     * Annotation meant to tag a <strong>non</strong> {@link Cloneable} object that needs cloning by any other method
     * Will call the method called by the provided param 'method'
     * that method must return an object of the same class and must take no arguments
     */
    @Retention(value= RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ToClone {
        String method() default "clone";
    }

    /**
     * Annotation meant to tag {@link Cloneable} fields that <strong>mustn't</strong> be Cloned
     */
    @Retention(value= RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotToClone {}

    /**
     * Annotation to tag fields that need their values updated after cloning
     * More appropriate for bulk operations
     * The Field will have to be annotated with a properly parameterized  {@link UpdateSpecification}
     */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ToUpdate {
        Class<? extends UpdateSpecification<?>> spec();
    }

    /**
     * Annotation to tag {@link Cloneable} fields that <strong>mustn't</strong> be upgraded after cloning<br>
     */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotToUpdate {}

    /**
     * Provides a simple way to perform bulk property updates via {@link Cloneables#update(Class, Cloneable, int)}
     * @param <T> The type of the field to update
     */
    interface UpdateSpecification<T>{
        /**
         *
         * @param originalValue the original property's value
         * @param index         the index of the current clone
         * @return  the updated value as per the custom logic
         */
        T update(T originalValue, int index);
    }
}

