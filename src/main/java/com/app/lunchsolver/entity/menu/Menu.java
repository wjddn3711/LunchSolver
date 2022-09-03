package com.app.lunchsolver.entity.menu;

import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.util.StringListConverter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Menu {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private int priority;

    @ElementCollection(targetClass=String.class)
    @Column(name = "images")
    private List<String> images;

    @Builder
    public Menu(String id, Restaurant restaurant, String name, String description, int priority, List<String> images) {
        this.id = id;
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.images = images;
    }
}
