package com.atomic.tools.db.util;

import com.atomic.tools.db.Change;
import com.atomic.tools.db.ChangeType;
import com.atomic.tools.db.Request;
import com.atomic.tools.db.Source;
import com.atomic.tools.db.Table;
import com.atomic.tools.db.Value;
import org.assertj.core.api.WritableAssertionInfo;

import java.util.ArrayList;
import java.util.List;

public class Descriptions {

    private Descriptions() {
        // Empty
    }

    public static String getDescription(Table table) {
        return table.getName() + " table";
    }

    public static String getDescription(Request request) {
        String sql = request.getRequest();
        if (sql.length() > 30) {
            sql = sql.substring(0, 30) + "...";
        }
        return "'" + sql + "' request";
    }

    public static String getDescription(com.atomic.tools.db.Changes changes) {
        StringBuilder stringBuilder = new StringBuilder();
        if (changes.getTablesList() != null) {
            List<Table> tablesList = changes.getTablesList();
            if (tablesList.size() == 1) {
                Table table = tablesList.get(0);
                stringBuilder.append("Changes on ").append(table.getName()).append(" table");
            } else {
                stringBuilder.append("Changes on tables");
            }
        } else {
            Request request = changes.getRequest();
            if (request != null) {
                String sql = request.getRequest();
                if (sql.length() > 30) {
                    sql = sql.substring(0, 30) + "...";
                }
                stringBuilder.append("Changes on '").append(sql).append("' request");
            } else {
                stringBuilder.append("Changes");
            }
        }
        if (changes.getSource() != null) {
            Source source = changes.getSource();
            stringBuilder.append(" of '").append(source.getUser()).append("/").append(source.getUrl()).append("' source");
        } else {
            stringBuilder.append(" of a data source");
        }
        return stringBuilder.toString();
    }

    public static String getRowDescription(WritableAssertionInfo info, int index) {
        return "Row at index " + index + " of " + info.descriptionText();
    }

    public static String getRowAtStartPointDescription(WritableAssertionInfo info) {
        return "Row at start point of " + info.descriptionText();
    }

    public static String getRowAtEndPointDescription(WritableAssertionInfo info) {
        return "Row at end point of " + info.descriptionText();
    }

    /**
     * @param info       可写断言信息
     * @param index      列的索引
     * @param columnName Name of column.
     * @return The description
     */
    public static String getColumnDescription(WritableAssertionInfo info, int index, String columnName) {
        return "Column at index " + index + " (column name : " + columnName + ") of " + info.descriptionText();
    }

    /**
     * @param info  可写断言信息
     * @param index 值的索引
     * @return The description
     */
    public static String getColumnValueDescription(WritableAssertionInfo info, int index) {
        return "Value at index " + index + " of " + info.descriptionText();
    }

    /**
     * @param info 可写断言信息
     * @return The description
     */
    public static String getColumnValueAtStartPointDescription(WritableAssertionInfo info) {
        return "Value at start point of " + info.descriptionText();
    }

    /**
     * @param info 可写断言信息
     * @return The description
     */
    public static String getColumnValueAtEndPointDescription(WritableAssertionInfo info) {
        return "Value at end point of " + info.descriptionText();
    }

    /**
     * @param info       可写断言信息
     * @param index      Index of the value.
     * @param columnName Name of column of the value.
     * @return The description
     */
    public static String getRowValueDescription(WritableAssertionInfo info, int index, String columnName) {
        return "Value at index " + index + " (column name : " + columnName + ") of " + info.descriptionText();
    }

    /**
     * @param changeType Type of the change.
     * @param tableName  Name of the table.
     * @return The changes assert implementation.
     */
    private static StringBuilder getStringBuilderAboutChangeTypeAndTableName(ChangeType changeType, String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        if (changeType != null || tableName != null) {
            stringBuilder.append(" (only");
            if (changeType != null) {
                stringBuilder.append(" ");
                stringBuilder.append(changeType.name().toLowerCase());
            }
            stringBuilder.append(" ");
            stringBuilder.append("changes");
            if (tableName != null) {
                stringBuilder.append(" on ");
                stringBuilder.append(tableName);
                stringBuilder.append(" table");
            }
            stringBuilder.append(")");
        }
        return stringBuilder;
    }

    /**
     * @param info       可写断言信息
     * @param changeType Type of the change.
     * @param tableName  Name of the table.
     * @return The description
     */
    public static String getChangesDescription(WritableAssertionInfo info, ChangeType changeType, String tableName) {
        return info.descriptionText() + getStringBuilderAboutChangeTypeAndTableName(changeType, tableName);
    }

    /**
     * @param info       可写断言信息
     * @param changes    The changes
     * @param change     The change
     * @param index      Index of the value.
     * @param changeType Type of the change.
     * @param tableName  Name of the table.
     * @return The description
     */
    public static String getChangeDescription(WritableAssertionInfo info, com.atomic.tools.db.Changes changes, Change change, int index,
                                              ChangeType changeType, String tableName) {
        StringBuilder stringBuilder = new StringBuilder("Change at index " + index);
        List<Value> pksValueList = change.getPksValueList();
        boolean isAChangeOnATableAmongOtherTables = changes.getTablesList() != null && changes.getTablesList().size() > 1;
        boolean havePksValues = pksValueList.size() > 0;
        if (isAChangeOnATableAmongOtherTables || havePksValues) {
            stringBuilder.append(" (");
            if (isAChangeOnATableAmongOtherTables) {
                stringBuilder.append("on table : ").append(change.getDataName());
            }
            if (isAChangeOnATableAmongOtherTables && havePksValues) {
                stringBuilder.append(" and ");
            }
            if (havePksValues) {
                List<Object> objectsList = new ArrayList<>();
                for (Value value : pksValueList) {
                    objectsList.add(value.getValue());
                }
                stringBuilder.append("with primary key : ").append(objectsList);
            }
            stringBuilder.append(")");
        }
        stringBuilder.append(" of ").append(info.descriptionText());
        stringBuilder.append(getStringBuilderAboutChangeTypeAndTableName(changeType, tableName));
        return stringBuilder.toString();
    }
}
