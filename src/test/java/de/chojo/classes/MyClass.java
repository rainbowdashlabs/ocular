/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.classes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class MyClass {
    private String name;
    private int age;

    public MyClass() {
    }

    @JsonCreator
    public MyClass(@JsonProperty("name") String name,
                   @JsonProperty("age") int age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

    public void name(String name) {
        this.name = name;
    }

    public void age(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MyClass) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    @Override
    public String toString() {
        return "SerializableRecord[" +
                "name=" + name + ", " +
                "age=" + age + ']';
    }


}
