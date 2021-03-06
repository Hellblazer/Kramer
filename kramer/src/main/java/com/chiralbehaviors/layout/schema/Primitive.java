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

/**
 * @author hhildebrand
 *
 */
public class Primitive extends SchemaNode {

    private double defaultWidth = 0;

    public Primitive() {
        super();
    }

    public Primitive(String field) {
        super(field);
    }

    public double getDefaultWidth() {
        return defaultWidth;
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
