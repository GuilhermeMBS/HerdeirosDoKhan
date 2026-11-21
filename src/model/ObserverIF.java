package model;

/**
 * Observer of the Observer pattern (as in Chapter 17).
 * The screens implement it to be told when the state of the match changes.
 *
 * @author Vinicius
 */
public interface ObserverIF {

    /**
     * Called by the observed object after its state changed.
     * @param o the observed object, used to read the new state with {@link ObservableIF#get(ObserverEnum)}
     */
    void notify(ObservableIF o);
}
