package dev.chojo.classes;

import java.util.Objects;

public final class SerializableRecord {
    private String name;
    private Integer age;

    public SerializableRecord() {
    }

    public SerializableRecord(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public Integer age() {
        return age;
    }

    public void name(String name) {
        this.name = name;
    }

    public void age(Integer age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SerializableRecord) obj;
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
