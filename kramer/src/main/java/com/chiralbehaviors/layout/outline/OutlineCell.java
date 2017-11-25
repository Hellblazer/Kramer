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
import java.util.Collection;
import java.util.List;

import com.chiralbehaviors.layout.ColumnSet;
import com.chiralbehaviors.layout.RelationLayout;
import com.chiralbehaviors.layout.cell.VerticalCell;
import com.chiralbehaviors.layout.flowless.Cell;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author halhildebrand
 *
 */
public class OutlineCell extends VerticalCell<OutlineCell> {
    private static final String        DEFAULT_STYLE = "outline-cell";
    private static final String        STYLE_SHEET   = "outline-cell.css";

    private List<Cell<JsonNode, Span>> spans         = new ArrayList<>();

    public OutlineCell() {
        super(STYLE_SHEET);
        initialize(DEFAULT_STYLE);
    }

    public OutlineCell(Collection<ColumnSet> columnSets, int childCardinality,
                       double cellHeight, RelationLayout layout) {
        super(STYLE_SHEET);
        setMinSize(layout.getJustifiedWidth(), cellHeight);
        setPrefSize(layout.getJustifiedWidth(), cellHeight);
        setMaxSize(layout.getJustifiedWidth(), cellHeight);
        columnSets.forEach(cs -> {
            Cell<JsonNode, Span> span = new Span(cs.getWidth(), cs.getColumns(),
                                                 childCardinality,
                                                 cs.getCellHeight(),
                                                 layout.getLabelWidth());
            spans.add(span);
            VBox.setVgrow(span.getNode(), Priority.ALWAYS);
            getChildren().add(span.getNode());
        });
    }

    @Override
    public void updateIndex(int index) {
        boolean active = ((index % 2) == 0);
        pseudoClassStateChanged(PSEUDO_CLASS_EVEN, active);
        pseudoClassStateChanged(PSEUDO_CLASS_ODD, !active);
    }

    @Override
    public void updateItem(JsonNode item) {
        spans.forEach(s -> s.updateItem(item));
    }
}
