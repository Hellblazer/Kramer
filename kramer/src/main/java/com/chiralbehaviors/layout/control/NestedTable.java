/**
 * Copyright (c) 2017 Chiral Behaviors, LLC, all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chiralbehaviors.layout.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.chiralbehaviors.layout.LayoutProvider;
import com.chiralbehaviors.layout.PrimitiveLayout;
import com.chiralbehaviors.layout.RelationLayout;
import com.chiralbehaviors.layout.schema.SchemaNode;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 * @author halhildebrand
 *
 */
public class NestedTable extends JsonControl {
    private double             rowHeight;
    private ListView<JsonNode> rows;

    public NestedTable(RelationLayout layout) {
        getStyleClass().add(layout.getStyleClass());
    }

    public JsonControl build(int cardinality, RelationLayout layout) {
        Region header = layout.buildColumnHeader();
        VBox frame = new VBox(header, buildRows(cardinality, layout));
        //        frame.setMinWidth(layout.getJustifiedWidth());
        //        frame.setMaxWidth(layout.getJustifiedWidth()); 

        AnchorPane.setLeftAnchor(frame, 0d);
        AnchorPane.setRightAnchor(frame, 0d);
        AnchorPane.setTopAnchor(frame, 0d);
        AnchorPane.setBottomAnchor(frame, 0d);

        getChildren().add(new AnchorPane(frame));
        return this;
    }

    public Pair<Consumer<JsonNode>, Region> buildPrimitive(double rendered,
                                                           PrimitiveLayout layout) {
        JsonControl control = layout.buildControl(1);
        double width = layout.getJustifiedWidth();
        control.setMinWidth(width);
        control.setMaxWidth(width);
        return new Pair<>(node -> control.setItem(layout.extractFrom(node)),
                          control);
    }

    public Pair<Consumer<JsonNode>, Region> buildRelation(double rendered,
                                                          RelationLayout layout) {
        ListView<JsonNode> column = buildNestedRow(rendered, layout);
        return new Pair<>(node -> column.getItems()
                                        .setAll(itemsAsArray(layout.extractFrom(node))),
                          column);
    }

    public double getRowHeight() {
        return rowHeight;
    }

    @Override
    public void setItem(JsonNode items) {
        rows.getItems()
            .setAll(itemsAsArray(items));
    }

    public void setRowHeight(double rowHeight) {
        this.rowHeight = rowHeight;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NestedTableSkin(this);
    }

    private Pair<Consumer<JsonNode>, Region> buildColumn(double rendered,
                                                         RelationLayout layout) {
        HBox cell = new HBox();
        cell.getStyleClass()
            .add(layout.getStyleClass());
        List<Consumer<JsonNode>> consumers = new ArrayList<>();
        layout.forEach(child -> {
            Pair<Consumer<JsonNode>, Region> column = child.buildColumn(this,
                                                                        rendered);
            consumers.add(column.getKey());
            Region control = column.getValue();
            cell.getChildren()
                .add(control);
        });
        return new Pair<>(node -> consumers.forEach(c -> {
            c.accept(node);
        }), cell);
    }

    private ListView<JsonNode> buildNestedRow(double rendered,
                                              RelationLayout layout) {
        int cardinality = layout.resolvedCardinality();
        double calculatedHeight = layout.getHeight();
        double deficit = Math.max(0, rendered - calculatedHeight);
        double childDeficit = LayoutProvider.snap(Math.max(0, deficit
                                                              / cardinality));
        double extended = LayoutProvider.snap(layout.getRowHeight()
                                              + childDeficit);

        ListView<JsonNode> row = new ListView<>();
        layout.apply(row);

        row.setFixedCellSize(extended);

        row.setMinHeight(rendered);
        row.setMaxHeight(rendered);

        double width = layout.getJustifiedTableColumnWidth();
        row.setMinSize(width, rendered);
        row.setMaxSize(width, rendered);

        row.setCellFactory(listView -> {
            ListCell<JsonNode> cell = buildRowCell(buildColumn(layout.baseRowCellHeight(extended),
                                                               layout));
            double justified = layout.getJustifiedCellWidth();
            cell.setMinWidth(justified);
            cell.setMaxWidth(justified);
            return cell;
        });
        return row;
    }

    // Eventually will be controlled by CSS
    private ListCell<JsonNode> buildRowCell(Pair<Consumer<JsonNode>, Region> cell) {
        return new ListCell<JsonNode>() {
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem()) {
                    return;
                }
                super.updateItem(item, empty);
                if (empty) {
                    return;
                }
                if (item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(cell.getValue());
                cell.getKey()
                    .accept(item);
            }
        };
    }

    private AnchorPane buildRows(int card, RelationLayout layout) {
        rows = new ListView<>();
        layout.apply(rows);

        double rowHeight = layout.getRowHeight();
        rows.setFixedCellSize(rowHeight);

        double width = layout.getJustifiedTableColumnWidth();
        double height = layout.getHeight() - layout.getColumnHeaderHeight();

        rows.setMinSize(width, height);
        rows.setMaxSize(width, height);

        rows.setCellFactory(listView -> {
            ListCell<JsonNode> cell = buildRowCell(buildColumn(layout.baseRowCellHeight(rowHeight),
                                                               layout));
            double justified = layout.getJustifiedCellWidth();
            cell.setMinWidth(justified);
            cell.setMaxWidth(justified);
            return cell;
        });
        AnchorPane.setLeftAnchor(rows, 0d);
        AnchorPane.setRightAnchor(rows, 0d);
        AnchorPane.setTopAnchor(rows, 0d);
        AnchorPane.setBottomAnchor(rows, 0d);
        return new AnchorPane(rows);
    }

    private List<JsonNode> itemsAsArray(JsonNode items) {
        List<JsonNode> itemArray = new ArrayList<>();
        SchemaNode.asArray(items)
                  .forEach(n -> itemArray.add(n));
        return itemArray;
    }
}
