package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.Quoter;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author dxu2
 * @date 2023/12/20
 */
public class SetComponent {

    private String primaryKeyName;

    private List<JsonObject> updateDataList;

    private List<String> columns;

    /**
     * UpdateStatement.assignments
     */
    private List<String> assignments = new ArrayList<>();

    public SetComponent() {
    }

    public void setAssignments(List<String> assignments) {
        this.assignments = assignments;
    }

    public SetComponent primaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    public SetComponent dataList(List<JsonObject> updateDataList) {
        this.updateDataList = updateDataList;
        return this;
    }

    public SetComponent columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public SetComponent build() {
        JsonObject data = updateDataList.get(0);
        columns.forEach(column -> {
            if (Objects.nonNull(data.getValue(column))) {
                assignments.add(column + "=" + buildBatchUpdateSql("case " + primaryKeyName + " ", column));
            }
        });
        return this;
    }

    private StringBuilder buildBatchUpdateSql(String prefix, String column) {
        StringBuilder builder = new StringBuilder(prefix);
        updateDataList.forEach(u -> builder.append("when ").append("'").append(u.getLong(primaryKeyName)).append("'")
                .append(" then ")
                .append(new Quoter(u.getValue(column).toString()))
                .append(" ")
        );
        return builder.append("end");
    }

    @Override
    public String toString() {
        return KeelHelpers.stringHelper().joinStringArray(assignments, ",");
    }
    
}
