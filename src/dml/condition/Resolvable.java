package dml.condition;

/**
 * Something is resolvable if it is part of the where clause in a query
 *
 * @author Nicholas Chieppa
 */
public interface Resolvable {

    /**
     * Resolve a proposition against a table segment
     * @param data the data segment to check
     * @return the remaining data
     */
    public Object[][] resolveAgainst(Object[][] data);

}