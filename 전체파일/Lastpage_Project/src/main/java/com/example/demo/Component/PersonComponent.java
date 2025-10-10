package com.example.demo.Component;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data

public class PersonComponent {
    private String name;
    private int age;
    private String addr;

    PersonComponent(){
        this.name = "손보금";
        this.age=28;
        this.addr="진주";
    }
}
