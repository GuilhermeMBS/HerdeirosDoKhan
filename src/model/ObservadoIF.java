package model;

/**
 * Observed object of the Observer pattern (as in Chapter 17).
 * It keeps a list of observers and notifies them when its state changes.
 * The observers then call {@link #get(int)} back to read the data they need.
 *
 * @author Vinicius
 */
public interface ObservadoIF {

    /** Registers an observer, which will be notified of every change. */
    void add(ObservadorIF o);

    /** Removes an observer, which will not be notified anymore. */
    void remove(ObservadorIF o);

    /**
     * Gives the observers a piece of the observed state.
     * @param i which piece of the state (see the constants of the implementing class)
     */
    int get(int i);
}
