package com.ef.mediaroutingengine.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TestingStringLength {
    @NotNull(message = "name cannot be null")
    private String name;
    @Min(1)
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestingStringLength{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
