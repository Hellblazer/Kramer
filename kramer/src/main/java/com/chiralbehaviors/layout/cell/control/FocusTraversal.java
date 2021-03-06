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

package com.chiralbehaviors.layout.cell.control;

import com.chiralbehaviors.layout.cell.LayoutCell;
import com.chiralbehaviors.layout.cell.LayoutContainer;

/**
 * @author halhildebrand
 *
 * @param <C>
 */
public interface FocusTraversal<C extends LayoutCell<?>> {

    void activate();

    void edit();

    boolean isCurrent();

    boolean isCurrent(FocusTraversalNode<?> node);

    boolean propagate(SelectionEvent event);

    void select(LayoutContainer<?, ?, ?> child);

    void selectNext();

    void selectNoFocus(LayoutContainer<?, ?, ?> container);

    void selectPrevious();

    void setCurrent();

    void setCurrent(FocusTraversalNode<?> focused);

    void traverseNext();

    void traversePrevious();

    default void unbind() {
    }

}