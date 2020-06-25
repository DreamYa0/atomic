package com.atomic.tools.rollback.db.util;


import com.atomic.tools.rollback.db.lettercase.CaseComparison;

import java.util.List;

public enum NameComparator {

    INSTANCE;

    /**
     * @param namesList  List of names which can contain the {@code name}.
     * @param name       The name to search.
     * @param comparison Case comparison to compare the name.
     * @return The result.
     */
    public boolean contains(List<String> namesList, String name, CaseComparison comparison) {
        for (String nameInTheList : namesList) {
            if (comparison.isEqual(nameInTheList, name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param namesList  List of names which can contain the {@code name}.
     * @param name       The name to search.
     * @param comparison Case comparison to compare the name.
     * @return The result.
     */
    public int indexOf(List<String> namesList, String name, CaseComparison comparison) {
        int index = 0;
        for (String nameInTheList : namesList) {
            if (comparison.isEqual(nameInTheList, name)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
