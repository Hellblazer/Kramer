/**
 * Copyright (c) 2016 Chiral Behaviors, LLC, all rights reserved.
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

package com.chiralbehaviors.layout.schema;

import java.util.function.Function;

import com.chiralbehaviors.layout.LayoutProvider;
import com.chiralbehaviors.layout.PrimitiveLayout;
import com.chiralbehaviors.layout.SchemaNodeLayout.Indent;
import com.chiralbehaviors.layout.impl.ColumnHeader;
import com.chiralbehaviors.layout.impl.LayoutCell;
import com.chiralbehaviors.layout.impl.OutlineElement;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.layout.Region;

/**
 * @author hhildebrand
 *
 */
public class Primitive extends SchemaNode {

    private double          defaultWidth = 0;
    private PrimitiveLayout layout;

    public Primitive() {
        super();
    }

    public Primitive(String label) {
        super(label);
    }

    @Override
    public LayoutCell<? extends Region> buildColumn(double rendered) {
        return layout.buildColumn(rendered);
    }

    @Override
    public Function<Double, ColumnHeader> buildColumnHeader() {
        return layout.columnHeader();
    }

    @Override
    public double calculateTableColumnWidth() {
        return layout.calculateTableColumnWidth();
    }

    @Override
    public double cellHeight(int cardinality, double justified) {
        return layout.cellHeight(justified);
    }

    @Override
    public void compress(double available) {
        layout.compress(available);
    }

    public double getDefaultWidth() {
        return defaultWidth;
    }

    @Override
    public PrimitiveLayout getLayout() {
        return layout;
    }

    @Override
    public double justify(double available) {
        return layout.justify(available);
    }

    @Override
    public double layout(double width) {
        return layout.layout(width);
    }

    @Override
    public double layoutWidth() {
        return layout.layoutWidth();
    }

    @Override
    public double measure(JsonNode data, boolean singular,
                          LayoutProvider provider) {
        layout = provider.layout(this);
        return layout.measure(data, singular);
    }

    @Override
    public double nestTableColumn(Indent indent, double indentation) {
        return layout.nestTableColumn(indent, indentation);
    }

    @Override
    public OutlineElement outlineElement(int cardinality, double labelWidth,
                                         Function<JsonNode, JsonNode> extractor,
                                         double justified) {
        return layout.outlineElement(cardinality, labelWidth, extractor,
                                     justified);
    }

    @Override
    public double rowHeight(int cardinality, double width) {
        return cellHeight(cardinality, width);
    }

    @Override
    public double tableColumnWidth() {
        return layout.tableColumnWidth();
    }

    @Override
    public String toString() {
        return String.format("Primitive [%s]", label);
    }

    @Override
    public String toString(int indent) {
        return toString();
    }
}
