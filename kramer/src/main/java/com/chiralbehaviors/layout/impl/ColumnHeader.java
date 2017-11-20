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

package com.chiralbehaviors.layout.impl;

import static com.chiralbehaviors.layout.LayoutProvider.snap;

import java.util.List;
import java.util.function.Function;

import com.chiralbehaviors.layout.PrimitiveLayout;
import com.chiralbehaviors.layout.RelationLayout;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author halhildebrand
 *
 */
public class ColumnHeader extends VBox implements LayoutCell<ColumnHeader> {
    private static final String DEFAULT_STYLE = "column-header";

    {
        styleFocus(this, DEFAULT_STYLE);
    }

    public ColumnHeader(double width, double height, RelationLayout layout,
                        List<Function<Double, ColumnHeader>> nestedHeaders) {
        HBox nested = new HBox();
        setMinSize(width, height);
        setMaxSize(width, height);
        double half = snap(height / 2.0);
        getChildren().addAll(layout.label(width, half), nested);

        nestedHeaders.forEach(n -> {
            nested.getChildren()
                  .add(n.apply(half));
        });
    }

    public ColumnHeader(double width, double height, PrimitiveLayout layout) {
        setMinSize(width, height);
        setMaxSize(width, height);
        getChildren().add(layout.label(width, height));
    }

    @Override
    public ColumnHeader getNode() {
        return this;
    }
}
