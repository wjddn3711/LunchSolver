package com.app.lunchsolver.dto;

import com.app.lunchsolver.entity.menu.Menu;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MenuDTO {

    private String name;
    private int price;
    private String[] images;
    private String description;
    private String id;
    private int priority;

    public MenuDTO(Menu entity) {
        this.name = entity.getName();
        this.price = entity.getPrice();
        this.images = entity.getImages().toArray(String[]::new);
        this.description = entity.getDescription();
        this.id = entity.getId();
        this.priority = entity.getPriority();
    }
}
