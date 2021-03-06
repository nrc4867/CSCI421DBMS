package dml;

import ddl.catalog.Attribute;
import ddl.catalog.Table;
import dml.condition.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateFilter {

    private enum MATHOP {
        ADD, SUB, MUL, DIV, NONE
    }

    private final static String mathopRegex = "([+\\-*/])";
    private final static Map<String, MATHOP> mathopMap = new HashMap<>() {{
       put("+", MATHOP.ADD);
       put("-", MATHOP.SUB);
       put("*", MATHOP.MUL);
       put("/", MATHOP.DIV);
    }};

    private enum VALTYPES {
        ATTR, DOUBLE, INTEGER, BOOL, STRING
    }

    private final Table table;

    private final List<MATHOP> operations = new ArrayList<>();
    private final List<Attribute> assigess = new ArrayList<>();

    private final List<List<Object>> inputs = new ArrayList<>();
    private final List<List<VALTYPES>> inputTypes = new ArrayList<>();

    UpdateFilter(Table table, String allColumns) throws DMLParserException {
        this.table = table;
        String[] columns = allColumns.split(",[ ]*");

        for (String column : columns) {
            String[] parts = column.split("[ ]*=[ ]*");
            assigess.add(table.getAttribute(parts[0].trim()));

            String[] operands = parts[1].split(mathopRegex);

            inputs.add(new ArrayList<>());
            inputTypes.add(new ArrayList<>());

            parseOperand(operands[0].trim());
            if (operands.length == 2) {
                parseOperand(operands[1].trim());
                operations.add(mathopMap.get(parts[1].substring(operands[0].length(),
                        operands[0].length() + (parts[1].length() - (operands[0].length() + operands[1].length())))));
            } else {
                operations.add(MATHOP.NONE);
            }


        }
    }

    private void parseOperand(String operand) throws DMLParserException {
        try {
            if (operand.contains("\"")) {
                inputs.get(inputs.size() - 1).add(operand.substring(1, operand.length() - 1));
                inputTypes.get(inputs.size() - 1).add(VALTYPES.STRING);
            } else if(operand.contains("true") || operand.contains("false")) {
                Boolean b = Boolean.valueOf(operand);
                inputs.get(inputs.size() - 1).add(b);
                inputTypes.get(inputs.size() - 1).add(VALTYPES.BOOL);
            } else if (operand.contains(".")) {
                Double d = Double.parseDouble(operand);

                inputs.get(inputs.size() - 1).add(d);
                inputTypes.get(inputs.size() - 1).add(VALTYPES.DOUBLE);
            } else {
                Integer d = Integer.parseInt(operand);

                inputs.get(inputs.size() - 1).add(d);
                inputTypes.get(inputs.size() - 1).add(VALTYPES.INTEGER);
            }
        } catch (NumberFormatException e) {
            Attribute attribute = table.getAttribute(operand);
            if (attribute == null) throw new DMLParserException("");

            inputs.get(inputs.size() - 1).add(attribute);
            inputTypes.get(inputs.size() - 1).add(VALTYPES.ATTR);
        }
    }

    Object[] performUpdate(Object[] oldTuple) throws DMLParserException {
        Object[] newTuple = oldTuple.clone();
        for (int i = 0; i < assigess.size(); i++) {
            Attribute assign = assigess.get(i);

            switch (assign.getDataType().split("[(]")[0]) {
                case "integer":
                    newTuple[table.getIndex(assign)] = intMath(i, oldTuple);
                    break;
                case "double":
                    newTuple[table.getIndex(assign)] = doubleMath(i, oldTuple);
                    break;
                case "varchar":
                case "char":
                case "boolean":
                    Object obj = inputs.get(i).get(0);
                    if (inputTypes.get(i).get(0) == VALTYPES.ATTR) obj = oldTuple[table.getAttributeIndex((String) obj)];
                    newTuple[table.getIndex(assign)] = obj;
                    break;
                default:
                    throw new DMLParserException("The attribute " + assign.getName() + " has type "
                            + assign.getDataType() + " which is unrecognized by this clause.");
            }
        }
        return newTuple;
    }

    private Double doubleMath(int index, Object[] oldTuple) {
        double value1 = (inputTypes.get(index).get(0) == VALTYPES.ATTR)? (double) oldTuple[table.getIndex((Attribute) inputs.get(index).get(0))] : (double) inputs.get(index).get(0);
        double value2 = 0;
        if(operations.get(index) != MATHOP.NONE)
            value2 = (inputTypes.get(index).get(1) == VALTYPES.ATTR)? (double) oldTuple[table.getIndex((Attribute) inputs.get(index).get(1))] : (double) inputs.get(index).get(1);

        switch (operations.get(index)) {
            case ADD:
                return value1 + value2;
            case SUB:
                return value1 - value2;
            case MUL:
                return value1 * value2;
            case DIV:
                return value1 / value2;
            case NONE:
                return value1;
        }

        return 0.0;
    }

    private Integer intMath(int index, Object[] oldTuple) {
        int value1 = (inputTypes.get(index).get(0) == VALTYPES.ATTR)? (int) oldTuple[table.getIndex((Attribute) inputs.get(index).get(0))] : (int) inputs.get(index).get(0);
        int value2 = 0;
        if(operations.get(index) != MATHOP.NONE)
            value2 = (inputTypes.get(index).get(1) == VALTYPES.ATTR)? (int) oldTuple[table.getIndex((Attribute) inputs.get(index).get(1))] : (int) inputs.get(index).get(1);

        switch (operations.get(index)) {
            case ADD:
                return value1 + value2;
            case SUB:
                return value1 - value2;
            case MUL:
                return value1 * value2;
            case DIV:
                return value1 / value2;
            case NONE:
                return value1;
        }

        return 0;
    }

}
