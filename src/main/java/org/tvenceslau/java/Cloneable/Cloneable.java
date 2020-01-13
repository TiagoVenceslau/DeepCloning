package org.tvenceslau.java.Cloneable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><h1>Deep Cloning Implementation</h1></p>
 * <p><h2>Cloneable</h2></p>
 * <p>org.tvenceslau.java.Cloneable</p>
 * <br>
 * <h2>Objective:</h2>
 * <p>Provides generic access and default implementation for deep cloning any object that implements {@link Cloneable}<br>
 *     while providing complete control over which properties will also be cloned and/or updated somehow</p>
 * <p>Doing so in a manner that can be used in any project and virtually without any need for extra code</p>
 * <ul>
 *     <lh><h3>Applies to:</h3></lh>
 *     <li>Any objects that implement {@link Cloneable}</li>
 * </ul>
 * <p><h3>How to use:</h3></p>
 * <p><ul>
 *     <li>Classes that implement this must parameterize it to it's own class. Eg: 'class XXX implements Cloneable<XXX>'.<br>
 *         Subclasses will also be cloneable and return an instance of their class.</li>
 *     <li>Any properties</li>
 *     <li>If the class being cloned needs to have parameters updated - name -> name_INDEX_1 -
 *          then one must annotate those properties with {@link Cloneable.ToUpdate}</li>
 * </ul></p>
 *
 * <p><h4>Comments:</h4></p>
 * <p><ul>
 *     <li></li>
 * </ul></p>
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
     * When called in the cosntructor set the 'initialization' flag to true
     * After cloning set the 'initialization' flag to false (Used in {@link SedrSectionComponent} for creating new {@link DataSECTION}s):
     * To reset the properties back to their original value set 'initialization' flag to 'null'
     *
     * Example:
     * <pre>
     *     For a FieldLabel:
     *          - name = "control_label_for_fieldName";
     *          - forFieldName = "fieldName";
     * </pre>
     * <pre>
     *     This method should be such so the resulting clone is:
     *          FieldLabel:
     *          - name = "control_label_for_fieldName" + separator + suffix
     *          - forFieldName = "fieldName" + separator + suffix
     * </pre>
     * @param separator a String to concatenate in the end of the updatable properties
     * @param index a String to concatenate in the end of the updatable properties
     * @param initialization because the initialization of the nested fields happens inside out:
     *                       Example: section A contains Section B<br>
     *                                B will be initialized before A and as such the concatenation<br>
     *                                will be B_INDEX_B1<br>
     *                                Then when A if constructed the concatenation will have to be<br>
     *                                B_INDEX_A1_INDEX_B1 (we have to insert the A index before the B Index)
     *                       When the outside section already exists (when we receive data, we clean all sections and build them again)<br>
     *                                The original section will already be named B_INDEX_A1<br>
     *                                And we just need to add the inner section index resulting in the same<br>
     *                                B_INDEX_A1_INDEX_B1
     *                       So for 'true' the 'insert' method is used<br>
     *                       for 'false' the 'add' method is used
     *                       for 'null' then each field is returns its original name
     *
     * @throws IllegalArgumentException if any annotated property is not a String
     */
    default void updateSelfProperties(String separator, String index, Boolean initialization) throws IllegalArgumentException {
        Cloneables.updateFieldNames(this.getClass(), this , separator, index, initialization);
    }

    /**
     * resets the annotated properties back to their original value
     * Just a wrapper around updateSelfProperties(separator, index, null)
     *
     * @throws IllegalArgumentException if any annotated property is not a String
     */
    default void resetSelfProperties(String separator, String index) throws IllegalArgumentException {
        Cloneables.updateFieldNames(this.getClass(), this, separator, index, null);
    }

    /**
     * Annotation meant to tag <strong>non</strong> {@link Cloneable} object that needs clonings by any other method
     * Meant to clone specific properties that can't implement Cloneable,
     * must still be different objects between clones
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
     * Will throw a {@link UnsupportedOperationException} if the annotated property is not a String
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

    interface UpdateSpecification<T>{
        T update(T originalValue);
        T reset(T updatedValue);
    }
}

