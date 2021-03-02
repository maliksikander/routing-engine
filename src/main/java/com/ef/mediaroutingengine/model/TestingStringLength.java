package com.ef.mediaroutingengine.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.stereotype.Service;

@Service
public class TestingStringLength {
    @NotNull
    @Size(min = 3, max = 5)
    private String name;
    @Min(1)
    @Max(5)
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
        return "TestingStringLength{"
                + "name='" + name + '\''
                + ", age=" + age
                + '}';
    }
}
