package ddl.catalog;

import database.Database;
import ddl.DDLParserException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// class for storing foreign key data
public class ForeignKey implements Serializable {
    // <rname>
    private String referenceTable;
    // (<r1>... <rN>):
    private List<String> references;
    // (<a1>...<aN>)
    private List<String> attributes;

    /**
     * Create a foreign key relationship
     * @param table the source table
     * @param attributes the attributes from the source table
     * @param referenceTable the reference table
     * @param references the attributes in the reference table
     * @throws DDLParserException Reference table dne, or types dont match
     */
    public ForeignKey(Table table, String[] attributes, String referenceTable, String[] references) throws DDLParserException {
        if (references.length != attributes.length) throw new DDLParserException("insignificat attributes");
        if (Database.catalog.getTable(referenceTable) == null) throw new DDLParserException("table dne");

        this.referenceTable = referenceTable;
        this.references = new ArrayList<String>(Arrays.asList(references));
        this.attributes = new ArrayList<String>(Arrays.asList(attributes));

        table.typeMatch(this.attributes, Database.catalog.getTable(referenceTable), this.references);

        table.addForeignKey(this);
    }

    public boolean isReferencingTable(String name) {
        return referenceTable.equals(name);
    }

    public boolean containsAttribute(String name) {
        return attributes.contains(name);
    }

    public boolean containsReference(String name) {
        return attributes.contains(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(references, attributes, referenceTable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForeignKey)
            return ((ForeignKey) obj).referenceTable.equals(referenceTable) &&  references.equals(((ForeignKey) obj).references) && attributes.equals(((ForeignKey) obj).attributes);
        return false;
    }
}
