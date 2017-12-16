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

import java.util.ArrayList;
import java.util.List;

import com.chiralbehaviors.layout.Column;
import com.chiralbehaviors.layout.cell.FocusTraversal;
import com.chiralbehaviors.layout.cell.FocusTraversal.Bias;
import com.chiralbehaviors.layout.cell.HorizontalCell;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.Node;

/**
 * @author halhildebrand
 *
 */
public class Span extends HorizontalCell<Span> {

    private static final String       DEFAULT_STYLE = "span";
    private static final String       S_SPAN        = "%s-span";
    private static final String       STYLE_SHEET   = "span.css";
    private final List<OutlineColumn> columns       = new ArrayList<>();
    private final FocusTraversal      focus;
    private int                       selected;

    public Span(String field) {
        this(field, null);
    }

    public Span(String field, double justified, List<Column> columns,
                int cardinality, double cellHeight, double labelWidth,
                FocusTraversal parentTraversal) {
        this(field, parentTraversal);
        setMinSize(justified, cellHeight);
        setPrefSize(justified, cellHeight);
        setMaxSize(justified, cellHeight);

        columns.forEach(c -> {
            OutlineColumn cell = new OutlineColumn(field, c, cardinality,
                                                   labelWidth, cellHeight,
                                                   focus);
            this.columns.add(cell);
            getChildren().add(cell.getNode());
        });
    }

    public Span(String field, FocusTraversal parentTraversal) {
        super(STYLE_SHEET);
        initialize(DEFAULT_STYLE);
        getStyleClass().add(String.format(S_SPAN, field));
        focus = new FocusTraversal(parentTraversal, Bias.HORIZONTAL) {

            @Override
            public void selectNext() {
                selected = selected + 1;
                if (selected == columns.size()) {
                    selected = selected - 1;
                    traverseNext();
                } else {
                    columns.get(selected)
                           .setFocus();
                }
            }

            @Override
            public void selectPrevious() {
                selected = selected - 1;
                if (selected < 0) {
                    selected = -1;
                    traversePrevious();
                } else {
                    columns.get(selected)
                           .setFocus();
                }
            }

            @Override
            protected Node getNode() {
                return Span.this;
            }
        };
    }

    @Override
    public void dispose() {
        focus.unbind();
    }

    @Override
    public void updateItem(JsonNode item) {
        columns.forEach(c -> c.updateItem(item));
        getNode().pseudoClassStateChanged(PSEUDO_CLASS_FILLED, item != null);
    }
}
