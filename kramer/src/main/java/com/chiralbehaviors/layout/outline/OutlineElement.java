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

package com.chiralbehaviors.layout.outline;

import com.chiralbehaviors.layout.SchemaNodeLayout;
import com.chiralbehaviors.layout.cell.HorizontalCell;
import com.chiralbehaviors.layout.cell.LayoutCell;
import com.chiralbehaviors.layout.cell.control.FocusTraversal;
import com.chiralbehaviors.layout.style.Layout;
import com.chiralbehaviors.layout.style.RelationStyle;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * @author halhildebrand
 *
 */
public class OutlineElement extends HorizontalCell<OutlineElement> {

    private static final String                  DEFAULT_STYLE         = "outline-element";
    private static final String                  SCHEMA_CLASS_TEMPLATE = "%s-outline-element";
    private static final String                  STYLE_SHEET           = "outline-element.css";

    private final LayoutCell<?>                  cell;
    private final FocusTraversal<OutlineElement> parentTraversal;

    public OutlineElement(String field) {
        super(STYLE_SHEET);
        initialize(DEFAULT_STYLE);
        getStyleClass().add(String.format(SCHEMA_CLASS_TEMPLATE, field));
        this.cell = null;
        this.parentTraversal = null;
    }

    public OutlineElement(SchemaNodeLayout layout, String field,
                          int cardinality, double labelWidth,
                          double elementHeight,
                          FocusTraversal<OutlineElement> parentTraversal,
                          Layout model, RelationStyle style) {
        super(STYLE_SHEET);
        initialize(DEFAULT_STYLE);
        getStyleClass().add(String.format(SCHEMA_CLASS_TEMPLATE, field));
        this.cell = layout.buildControl(parentTraversal, model);
        this.parentTraversal = parentTraversal;
        OutlineElement node = getNode();
        node.focusedProperty()
            .addListener((InvalidationListener) property -> {
                if (node.isFocused()) {
                    parentTraversal.setCurrent();
                }
            });

        setAlignment(Pos.CENTER);
        cell.getNode()
            .setMinHeight(elementHeight);
        cell.getNode()
            .setPrefHeight(elementHeight);
        cell.getNode()
            .setMaxHeight(elementHeight);
        Label label = layout.label(labelWidth, elementHeight);
        getChildren().addAll(label, cell.getNode());

    }

    @Override
    public void activate() {
        parentTraversal.setCurrent();
    }

    public OutlineElement(String field, SchemaNodeLayout layout,
                          int cardinality, double labelWidth,
                          FocusTraversal<OutlineElement> parentTraversal,
                          Layout model, RelationStyle style) {
        this(layout, field, cardinality, labelWidth, layout.getHeight(),
             parentTraversal, model, style);
    }

    @Override
    public void updateItem(JsonNode item) {
        cell.updateItem(item);
        getNode().pseudoClassStateChanged(PSEUDO_CLASS_FILLED, item != null);
    }
}
