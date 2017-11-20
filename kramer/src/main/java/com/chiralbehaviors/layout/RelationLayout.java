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

import static com.chiralbehaviors.layout.LayoutProvider.relax;
import static com.chiralbehaviors.layout.LayoutProvider.snap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.chiralbehaviors.layout.flowless.Cell;
import com.chiralbehaviors.layout.flowless.VirtualFlow;
import com.chiralbehaviors.layout.impl.ColumnHeader;
import com.chiralbehaviors.layout.impl.LayoutCell;
import com.chiralbehaviors.layout.impl.NestedRow;
import com.chiralbehaviors.layout.impl.NestedTable;
import com.chiralbehaviors.layout.impl.Outline;
import com.chiralbehaviors.layout.impl.OutlineElement;
import com.chiralbehaviors.layout.impl.TableHeader;
import com.chiralbehaviors.layout.schema.Relation;
import com.chiralbehaviors.layout.schema.SchemaNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import javafx.scene.control.Control;
import javafx.scene.layout.Region;

/**
 *
 * @author halhildebrand
 *
 */
public class RelationLayout extends SchemaNodeLayout {
    boolean                       useTable         = false;
    private int                   averageCardinality;
    private double                columnHeaderHeight;
    private final List<ColumnSet> columnSets       = new ArrayList<>();
    private int                   maxCardinality;
    private final Relation        r;
    private double                rowHeight        = -1;
    private boolean               singular;
    private double                tableColumnWidth = 0;

    public RelationLayout(LayoutProvider layout, Relation r) {
        super(layout);
        this.r = r;
    }

    @Override
    public void adjustHeight(double delta) {
        super.adjustHeight(delta);
        if (useTable) {
            List<SchemaNode> children = r.getChildren();
            double subDelta = delta / children.size();
            if (delta >= 1.0) {
                children.forEach(f -> f.adjustHeight(subDelta));
            }
            return;
        }
        double subDelta = delta / columnSets.size();
        if (subDelta >= 1.0) {
            columnSets.forEach(c -> c.adjustHeight(subDelta));
        }
    }

    public void apply(VirtualFlow<JsonNode, Cell<JsonNode, ?>> list) {
        layout.getModel()
              .apply(list, r);
    }

    public double baseOutlineCellHeight(double cellHeight) {
        return snap(cellHeight - layout.getCellVerticalInset());
    }

    public double baseRowCellHeight(double extended) {
        return snap(extended - layout.getCellVerticalInset());
    }

    @Override
    public LayoutCell<? extends Region> buildColumn(double rendered) {
        return new NestedRow(rendered, this);
    }

    public TableHeader buildColumnHeader() {
        return new TableHeader(snap(justifiedWidth
                                    + layout.getNestedCellInset()),
                               columnHeaderHeight, r.getChildren());
    }

    public LayoutCell<?> buildControl(int cardinality,
                                      Function<JsonNode, JsonNode> extractor) {
        return useTable ? r.buildNestedTable(extractor, cardinality)
                        : r.buildOutline(extractor, cardinality);
    }

    public LayoutCell<NestedTable> buildNestedTable(int cardinality) {
        return new NestedTable(resolveCardinality(cardinality), this);
    }

    public Outline buildOutline(Function<JsonNode, JsonNode> extractor,
                                int cardinality) {
        Outline outline = new Outline(height, columnSets, extractor,
                                      resolveCardinality(cardinality), this);
        outline.getNode()
               .setMinWidth(columnWidth());
        outline.getNode()
               .setPrefWidth(columnWidth());
        outline.getNode()
               .setMaxWidth(columnWidth());

        outline.getNode()
               .setMinHeight(height);
        outline.getNode()
               .setPrefHeight(height);
        outline.getNode()
               .setMaxHeight(height);
        return outline;
    }

    public double calculateTableColumnWidth() {
        return r.getChildren()
                .stream()
                .mapToDouble(c -> c.calculateTableColumnWidth())
                .sum()
               + layout.getNestedInset();
    }

    public double cellHeight(int card, double width) {
        if (height > 0) {
            return height;
        }
        int cardinality = resolveCardinality(card);
        if (!useTable) {
            height = outlineHeight(cardinality);
        } else {
            columnHeaderHeight = r.getChildren()
                                  .stream()
                                  .mapToDouble(c -> c.columnHeaderHeight())
                                  .max()
                                  .orElse(0.0);
            double elementHeight = elementHeight();
            rowHeight = rowHeight(elementHeight);
            height = snap((cardinality * snap(elementHeight
                                              + layout.getCellVerticalInset()))
                          + layout.getVerticalInset() + columnHeaderHeight);
        }
        return height;
    }

    @Override
    public Function<Double, ColumnHeader> columnHeader() {
        List<Function<Double, ColumnHeader>> nestedHeaders = r.getChildren()
                                                              .stream()
                                                              .map(c -> c.buildColumnHeader())
                                                              .collect(Collectors.toList());
        return rendered -> {
            double width = snap(justifiedWidth + columnHeaderIndentation);
            return new ColumnHeader(width, rendered, this, nestedHeaders);
        };
    }

    @Override
    public double columnWidth() {
        return snap(columnWidth + layout.getNestedInset());
    }

    @Override
    public void compress(double justified) {
        if (useTable) {
            justify(justified);
            return;
        }
        columnSets.clear();
        justifiedWidth = snap(baseColumnWidth(justified));
        List<SchemaNode> children = r.getChildren();
        columnSets.clear();
        ColumnSet current = null;
        double halfWidth = snap(justifiedWidth / 2d);
        for (SchemaNode child : children) {
            double childWidth = labelWidth + child.layoutWidth();
            if (childWidth > halfWidth || current == null) {
                current = new ColumnSet();
                columnSets.add(current);
                current.add(child);
                if (childWidth > halfWidth) {
                    current = null;
                }
            } else {
                current.add(child);
            }
        }
        columnSets.forEach(cs -> cs.compress(averageCardinality, justifiedWidth,
                                             labelWidth));
    }

    @Override
    public JsonNode extractFrom(JsonNode node) {
        return r.extractFrom(node);
    }

    public void forEach(Consumer<? super SchemaNode> action) {
        r.getChildren()
         .forEach(c -> action.accept(c));
    }

    public int getAverageCardinality() {
        return averageCardinality;
    }

    public double getColumnHeaderHeight() {
        if (columnHeaderHeight <= 0) {
            columnHeaderHeight = snap((layout.getTextLineHeight()
                                       + layout.getTextVerticalInset())
                                      + r.getChildren()
                                         .stream()
                                         .mapToDouble(c -> c.columnHeaderHeight())
                                         .max()
                                         .orElse(0.0));
        }
        return columnHeaderHeight;
    }

    @Override
    public String getField() {
        return r.getField();
    }

    public double getJustifiedCellWidth() {
        return snap(justifiedWidth + layout.getCellHorizontalInset());
    }

    @Override
    public double getJustifiedColumnWidth() {
        return snap(justifiedWidth + layout.getNestedInset());
    }

    public String getLabel() {
        return r.getLabel();
    }

    public double getRowHeight() {
        return rowHeight;
    }

    public String getStyleClass() {
        return r.getField();
    }

    @Override
    public double justify(double justifed) {
        double available = snap(justifed - layout.getNestedCellInset());
        double[] remaining = new double[] { available };
        List<SchemaNode> children = r.getChildren();
        SchemaNode last = children.get(children.size() - 1);
        justifiedWidth = snap(children.stream()
                                      .mapToDouble(child -> {
                                          double childJustified = relax(available
                                                                        * (child.tableColumnWidth()
                                                                           / tableColumnWidth));

                                          if (child.equals(last)) {
                                              childJustified = remaining[0];
                                          } else {
                                              remaining[0] -= childJustified;
                                          }
                                          return child.justify(childJustified);
                                      })
                                      .sum());
        return justifed;
    }

    @Override
    public Control label(double labelWidth) {
        return label(labelWidth, r.getLabel());
    }

    @Override
    public double layout(double width) {
        clear();
        double available = baseColumnWidth(width - labelWidth);
        assert available > 0;
        columnWidth = r.getChildren()
                       .stream()
                       .mapToDouble(c -> c.layout(available))
                       .max()
                       .orElse(0.0)
                      + labelWidth;
        double tableWidth = calculateTableColumnWidth();
        if (tableWidth <= columnWidth()) {
            return nestTableColumn(Indent.TOP, 0);
        }
        return columnWidth();
    }

    @Override
    public double layoutWidth() {
        return useTable ? tableColumnWidth() : columnWidth();
    }

    @Override
    public double measure(JsonNode datum, boolean isSingular) {
        clear();
        double sum = 0;
        columnWidth = 0;
        singular = isSingular;
        int singularChildren = 0;
        maxCardinality = 0;

        List<SchemaNode> children = r.getChildren();
        for (SchemaNode child : children) {
            ArrayNode aggregate = JsonNodeFactory.instance.arrayNode();
            int cardSum = 0;
            boolean childSingular = false;
            List<JsonNode> data = datum.isArray() ? new ArrayList<>(datum.size())
                                                  : Arrays.asList(datum);
            if (datum.isArray()) {
                datum.forEach(n -> data.add(n));
            }
            for (JsonNode node : data) {
                JsonNode sub = node.get(child.getField());
                if (sub instanceof ArrayNode) {
                    childSingular = false;
                    aggregate.addAll((ArrayNode) sub);
                    cardSum += sub.size();
                } else {
                    childSingular = true;
                    aggregate.add(sub);
                }
            }
            if (childSingular) {
                singularChildren += 1;
            } else {
                sum += data.size() == 0 ? 1 : Math.round(cardSum / data.size());
            }
            columnWidth = snap(Math.max(columnWidth,
                                        child.measure(aggregate, childSingular,
                                                      layout)));
            maxCardinality = Math.max(maxCardinality, data.size());
        }
        int effectiveChildren = children.size() - singularChildren;
        averageCardinality = Math.max(1,
                                      Math.min(4,
                                               effectiveChildren == 0 ? 1
                                                                      : (int) Math.ceil(sum
                                                                                        / effectiveChildren)));

        labelWidth = children.stream()
                             .mapToDouble(child -> child.getLabelWidth())
                             .max()
                             .getAsDouble();
        columnWidth = snap(labelWidth + columnWidth);
        maxCardinality = Math.max(1, maxCardinality);
        return columnWidth();
    }

    @Override
    public double nestTableColumn(Indent indent, double indentation) {
        columnHeaderIndentation = snap(indentation
                                       + layout.getNestedCellInset());
        useTable = true;
        rowHeight = -1.0;
        columnHeaderHeight = -1.0;
        height = -1.0;
        tableColumnWidth = snap(r.getChildren()
                                 .stream()
                                 .mapToDouble(c -> {
                                     Indent child = indent(indent, c);
                                     return c.nestTableColumn(child,
                                                              indent.indent(child,
                                                                            layout,
                                                                            indentation,
                                                                            c.isRelation()));
                                 })
                                 .sum());
        return tableColumnWidth();
    }

    public double outlineCellHeight(double baseHeight) {
        return baseHeight + layout.getCellVerticalInset();
    }

    @Override
    public OutlineElement outlineElement(int cardinality, double labelWidth,
                                         Function<JsonNode, JsonNode> extractor,
                                         double justified) {

        return new OutlineElement(this, cardinality, labelWidth, extractor,
                                  justified);
    }

    public int resolvedCardinality() {
        return resolveCardinality(averageCardinality);
    }

    public double rowHeight(int cardinality, double justified) {
        rowHeight = rowHeight(elementHeight());
        height = snap((cardinality * rowHeight) + layout.getVerticalInset());
        return height;
    }

    public double tableColumnWidth() {
        assert tableColumnWidth > 0.0 : String.format("%s tcw <= 0: %s",
                                                      r.getLabel(),
                                                      tableColumnWidth);
        return snap(tableColumnWidth + layout.getNestedInset());
    }

    @Override
    public String toString() {
        return String.format("RelationLayout [%s %s x %s, {%s, %s, %s} ]",
                             r.getField(), height, averageCardinality,
                             columnWidth, tableColumnWidth, justifiedWidth);
    }

    protected double baseColumnWidth(double width) {
        return snap(width - layout.getNestedInset());
    }

    @Override
    protected void clear() {
        super.clear();
        useTable = false;
        tableColumnWidth = -1.0;
    }

    protected double elementHeight() {
        return snap(r.getChildren()
                     .stream()
                     .mapToDouble(child -> child.rowHeight(averageCardinality,
                                                           justifiedWidth))
                     .max()
                     .getAsDouble());
    }

    protected Indent indent(Indent parent, SchemaNode child) {
        List<SchemaNode> children = r.getChildren();

        boolean isFirst = isFirst(child, children);
        boolean isLast = isLast(child, children);
        if (isFirst && isLast) {
            return Indent.SINGULAR;
        }
        if (isFirst) {
            return Indent.LEFT;
        } else if (isLast) {
            return Indent.RIGHT;
        } else {
            return Indent.NONE;
        }
    }

    protected boolean isFirst(SchemaNode child, List<SchemaNode> children) {
        return child.equals(children.get(0));
    }

    protected boolean isLast(SchemaNode child, List<SchemaNode> children) {
        return child.equals(children.get(children.size() - 1));
    }

    protected double outlineHeight(int cardinality) {
        return outlineHeight(cardinality, columnSets.stream()
                                                    .mapToDouble(cs -> cs.getCellHeight())
                                                    .sum());
    }

    protected double outlineHeight(int cardinality, double elementHeight) {
        return snap((cardinality
                     * (elementHeight + layout.getCellVerticalInset()))
                    + layout.getVerticalInset());
    }

    protected int resolveCardinality(int cardinality) {
        return singular ? 1 : Math.min(cardinality, maxCardinality);
    }

    protected double rowHeight(double elementHeight) {
        return snap(elementHeight + layout.getCellVerticalInset());
    }

}