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

package com.chiralbehaviors.layout;

import static com.chiralbehaviors.layout.LayoutProvider.snap;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.fxmisc.flowless.Cell;

import com.chiralbehaviors.layout.control.JsonControl;
import com.chiralbehaviors.layout.control.PrimitiveControl;
import com.chiralbehaviors.layout.schema.Primitive;
import com.chiralbehaviors.layout.schema.SchemaNode;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 *
 * @author halhildebrand
 *
 */
public class PrimitiveLayout extends SchemaNodeLayout {
    protected double        scroll = 0.0;
    private double          maxWidth;
    private final Primitive p;
    @SuppressWarnings("unused")
    private boolean         variableLength;

    public PrimitiveLayout(LayoutProvider layout, Primitive p) {
        super(layout);
        this.p = p;
    }

    @Override
    public Cell<JsonNode, ?> buildColumn(double rendered) {
        Label label = new Label();
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + "         rgba(0,0,0,0.08),"
                       + "        linear-gradient(#9a9a9a, #909090),"
                       + "        white 0%;"
                       + "    -fx-background-insets: 0 0 -1 0,0,1;"
                       + "    -fx-background-radius: 5,5,4;"
                       + "    -fx-padding: 3 30 3 30;"
                       + "    -fx-text-fill: #242d35;"
                       + "    -fx-font-size: 14px;");
        AnchorPane.setLeftAnchor(label, 0d);
        AnchorPane.setRightAnchor(label, 0d);
        AnchorPane.setTopAnchor(label, 0d);
        AnchorPane.setBottomAnchor(label, 0d);
        AnchorPane anchor = new AnchorPane(label);
        anchor.setMinSize(justifiedWidth, rendered);
        anchor.setPrefSize(justifiedWidth, rendered);
        anchor.setMaxSize(justifiedWidth, rendered);
        return new Cell<JsonNode, Node>() {
            @Override
            public Node getNode() {
                return anchor;
            }

            @Override
            public boolean isReusable() {
                return true;
            }

            @Override
            public void updateItem(JsonNode item) {
                label.setText(SchemaNode.asText(item));
            }
        };
    }

    public PrimitiveControl buildControl(int cardinality) {
        return new PrimitiveControl(p.getField());
    }

    public double calculateTableColumnWidth() {
        return columnWidth();
    }

    public double cellHeight(double justified) {
        if (height > 0) {
            return height;
        }
        double rows = Math.ceil((maxWidth / justified) + 0.5);
        height = snap((layout.getTextLineHeight() * rows)
                      + layout.getTextVerticalInset());
        return height;
    }

    @Override
    public Function<Double, Region> columnHeader() {
        return rendered -> {
            double width = getColumnHeaderWidth();
            Control columnHeader = layout.label(width, p.getLabel(), rendered);
            columnHeader.setMinSize(width, rendered);
            columnHeader.setMaxSize(width, rendered);
            return columnHeader;
        };
    }

    @Override
    public double columnWidth() {
        return Math.max(columnWidth, labelWidth);
    }

    @Override
    public void compress(double available) {
        justifiedWidth = snap(available);
    }

    @Override
    public JsonNode extractFrom(JsonNode node) {
        return p.extractFrom(node);
    }

    @Override
    public double getJustifiedColumnWidth() {
        return snap(justifiedWidth);
    }

    @Override
    public double justify(double justified) {
        justifiedWidth = snap(justified);
        return justifiedWidth;
    }

    @Override
    public double layout(double width) {
        clear();
        return width;
    }

    @Override
    public double layoutWidth() {
        return columnWidth();
    }

    @Override
    public double measure(JsonNode data, boolean singular) {
        clear();
        labelWidth = labelWidth(p.getLabel());
        double summedDataWidth = 0;
        maxWidth = 0;
        columnWidth = 0;
        for (JsonNode prim : SchemaNode.asList(data)) {
            List<JsonNode> rows = SchemaNode.asList(prim);
            double summedWidth = 0;
            for (JsonNode row : rows) {
                double w = width(row);
                summedWidth += w;
                maxWidth = Math.max(maxWidth, w);
            }
            summedDataWidth += rows.isEmpty() ? 1 : summedWidth / rows.size();
        }
        double averageWidth = data.size() == 0 ? 0
                                               : (summedDataWidth
                                                  / data.size());

        columnWidth = Math.max(labelWidth,
                               LayoutProvider.snap(Math.max(p.getDefaultWidth(),
                                                            averageWidth)));
        if (maxWidth > averageWidth) {
            variableLength = true;
        }
        return columnWidth;

    }

    @Override
    public double nestTableColumn(Indent indent, double indentation) {
        this.columnHeaderIndentation = indentation;
        return tableColumnWidth();
    }

    public Pair<Consumer<JsonNode>, Parent> outlineElement(int cardinality,
                                                           double labelWidth,
                                                           Function<JsonNode, JsonNode> extractor,
                                                           double justified) {
        HBox box = new HBox();
        box.setPrefSize(justified, height);
        VBox.setVgrow(box, Priority.ALWAYS);

        Control labelControl = label(labelWidth, p.getLabel());
        labelControl.setMinWidth(labelWidth);
        labelControl.setMaxWidth(labelWidth);
        JsonControl control = buildControl(cardinality);
        control.setPrefSize(justified, height);

        box.getChildren()
           .add(labelControl);
        box.getChildren()
           .add(control);

        return new Pair<>(item -> {
            control.setItem(extractor.apply(item)
                                     .get(p.getField()));
        }, box);
    }

    public void apply(Cell<JsonNode, ?> list) {
        layout.getModel()
              .apply(list, p);
    }

    public double tableColumnWidth() {
        return columnWidth();
    }

    @Override
    protected void clear() {
        super.clear();
        columnHeaderIndentation = 0.0;
    }

    protected double getColumnHeaderWidth() {
        return snap(justifiedWidth + columnHeaderIndentation);
    }

    protected double width(JsonNode row) {
        return layout.totalTextWidth(layout.textWidth(LayoutProvider.toString(row)));
    }
}