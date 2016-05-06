package com.ge.predix.solsvc.dataseed.asset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import com.ge.predix.solsvc.bootstrap.ams.dto.Attribute;

public abstract class AttributableObjectDataHandler extends BaseDataHandler {
    protected Attribute putAttributeInMap(Attribute previousAttrib, LinkedHashMap<String, Attribute> attributeMap,
                                          String[] row)
        throws DataFormatException
    {
        if ( row[getAttributeNameIndex()] == null || row[getAttributeNameIndex()].isEmpty() )
        {
            if ( row[getAttributeValueIndex()] != null && !row[getAttributeValueIndex()].isEmpty() )
            {
                if ( previousAttrib == null || previousAttrib.getValue() == null || previousAttrib.getValue().isEmpty() )
                {
                    throw new DataFormatException("Attribute value found without attribute in spreadsheet");
                }
                List<Object> valueList = previousAttrib.getValue();
                valueList.add(row[getAttributeValueIndex()]);
            }
            if ( row[getAttributeEnumerationIndex()] != null && !row[getAttributeEnumerationIndex()].isEmpty() )
            {
                if ( previousAttrib == null || previousAttrib.getEnumeration() == null
                    || previousAttrib.getEnumeration().isEmpty() )
                {
                    throw new DataFormatException("Attribute enum found without attribute in spreadsheet");
                }
                List<Object> enumList = previousAttrib.getEnumeration();
                enumList.add(row[getAttributeEnumerationIndex()]);
            }
            return previousAttrib;
        }
        Attribute attribute = new Attribute();
        List<Object> valueList = new ArrayList<>();
        if ( row[getAttributeValueIndex()] != null && !row[getAttributeValueIndex()].isEmpty() )
        {
            valueList.add(row[getAttributeValueIndex()]);
        }
        attribute.setValue(valueList);
        List<Object> enumList = new ArrayList<>();
        if ( row[getAttributeEnumerationIndex()] != null && !row[getAttributeEnumerationIndex()].isEmpty() )
        {
            enumList.add(row[getAttributeEnumerationIndex()]);
        }
        attribute.setEnumeration(enumList);
        attribute.setUom(row[getAttributeUomIndex()]);
        attribute.setType(row[getAttributeTypeIndex()]);
        attributeMap.put(row[getAttributeNameIndex()], attribute);
        return attribute;
    }

    protected abstract int getAttributeNameIndex();

    protected abstract int getAttributeValueIndex();

    protected abstract int getAttributeEnumerationIndex();

    protected abstract int getAttributeTypeIndex();

    protected abstract int getAttributeUomIndex();
}
